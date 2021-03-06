/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rya.indexing.pcj.fluo.app.query;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.rya.indexing.pcj.fluo.app.IncrementalUpdateConstants.AGGREGATION_PREFIX;
import static org.apache.rya.indexing.pcj.fluo.app.IncrementalUpdateConstants.CONSTRUCT_PREFIX;
import static org.apache.rya.indexing.pcj.fluo.app.IncrementalUpdateConstants.FILTER_PREFIX;
import static org.apache.rya.indexing.pcj.fluo.app.IncrementalUpdateConstants.JOIN_PREFIX;
import static org.apache.rya.indexing.pcj.fluo.app.IncrementalUpdateConstants.QUERY_PREFIX;
import static org.apache.rya.indexing.pcj.fluo.app.IncrementalUpdateConstants.SP_PREFIX;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.rya.indexing.pcj.fluo.app.ConstructGraph;
import org.apache.rya.indexing.pcj.fluo.app.ConstructProjection;
import org.apache.rya.indexing.pcj.fluo.app.FilterResultUpdater;
import org.apache.rya.indexing.pcj.fluo.app.FluoStringConverter;
import org.apache.rya.indexing.pcj.fluo.app.NodeType;
import org.apache.rya.indexing.pcj.fluo.app.query.AggregationMetadata.AggregationElement;
import org.apache.rya.indexing.pcj.fluo.app.query.AggregationMetadata.AggregationType;
import org.apache.rya.indexing.pcj.fluo.app.query.JoinMetadata.JoinType;
import org.apache.rya.indexing.pcj.storage.accumulo.VariableOrder;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.query.algebra.AggregateOperator;
import org.openrdf.query.algebra.BNodeGenerator;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.Group;
import org.openrdf.query.algebra.GroupElem;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.MultiProjection;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.parser.ParsedQuery;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import net.jcip.annotations.Immutable;

/**
 * Creates the {@link FluoQuery} metadata that is required by the Fluo
 * application to process a SPARQL query.
 */
public class SparqlFluoQueryBuilder {

    /**
     * Creates the {@link FluoQuery} metadata that is required by the Fluo
     * application to process a SPARQL query.
     *
     * @param parsedQuery - The query metadata will be derived from. (not null)
     * @param nodeIds - The NodeIds object is passed in so that other parts
     *   of the application may look up which ID is associated with each
     *   node of the query.
     * @return A {@link FluoQuery} object loaded with metadata built from the
     *   {@link ParsedQuery}.
     */
    public FluoQuery make(final ParsedQuery parsedQuery, final NodeIds nodeIds) {
        checkNotNull(parsedQuery);

        final String sparql = parsedQuery.getSourceString();
        final FluoQuery.Builder fluoQueryBuilder = FluoQuery.builder();

        final NewQueryVisitor visitor = new NewQueryVisitor(sparql, fluoQueryBuilder, nodeIds);
        parsedQuery.getTupleExpr().visit( visitor );

        final FluoQuery fluoQuery = fluoQueryBuilder.build();
        return fluoQuery;
    }

    /**
     * A data structure that creates and keeps track of Node IDs for the nodes
     * of a {@link ParsedQuery}. This structure should only be used while creating
     * a new PCJ in Fluo and disposed of afterwards.
     */
    @DefaultAnnotation(NonNull.class)
    public static final class NodeIds {

        /**
         * Maps from a parsed SPARQL query's node to the Node ID that has been assigned to it.
         */
        private final Map<QueryModelNode, String> nodeIds = new HashMap<>();

        /**
         * Checks if a SPARQL query's node has had a Node ID assigned to it yet.
         *
         * @param node - The node to check. (not null)
         * @return {@code true} if the {@code node} has had a Node ID assigned to
         *   it; otherwise {@code false}.
         */
        public boolean hasId(final QueryModelNode node) {
            checkNotNull(node);
            return nodeIds.containsKey(node);
        }

        /**
         * Get the Node ID that has been assigned a specific node of a SPARQL query.
         *
         * @param node - The node whose ID will be fetched. (not null)
         * @return The Node ID that has been assigned to {@code node} if one
         *   has been assigned to it; otherwise {@code absent}.
         */
        public Optional<String> getId(final QueryModelNode node) {
            checkNotNull(node);
            return Optional.ofNullable( nodeIds.get(node) );
        }

        /**
         * Get the Node ID that has been assigned to a specific node of a SPARQL
         * query. If one hasn't been assigned yet, then one will be generated
         * and assigned to it.
         *
         * @param node - The node whose ID will be fetched or generated. (not null)
         * @return The Node ID that is assigned to {@code node}.
         */
        public String getOrMakeId(final QueryModelNode node) {
            checkNotNull(node);

            // If a Node ID has already been assigned, return it.
            if(nodeIds.containsKey(node)){
                return nodeIds.get(node);
            }

            // Otherwise create a new ID and store it for later.
            final String id = makeId(node);
            nodeIds.put(node, id);
            return id;
        }

        private String makeId(final QueryModelNode node) {
            checkNotNull(node);

            // Create the prefix of the id. This makes it a little bit more human readable.
            String prefix;
            if (node instanceof StatementPattern) {
                prefix = SP_PREFIX;
            } else if (node instanceof Filter) {
                prefix = FILTER_PREFIX;
            } else if (node instanceof Join || node instanceof LeftJoin) {
                prefix = JOIN_PREFIX;
            } else if (node instanceof Projection) {
                prefix = QUERY_PREFIX;
            } else if(node instanceof Extension) {
                prefix = AGGREGATION_PREFIX;
            }  else if (node instanceof Reduced) {
                prefix = CONSTRUCT_PREFIX;
            } else {
                throw new IllegalArgumentException("Node must be of type {StatementPattern, Join, Filter, Extension, Projection} but was " + node.getClass());
            }

            // Create the unique portion of the id.
            final String unique = UUID.randomUUID().toString().replaceAll("-", "");

            // Put them together to create the Node ID.
            return prefix + "_" + unique;
        }
    }

    /**
     * Visits each node of a {@link ParsedQuery} and adds information about
     * the node to a {@link FluoQuery.Builder}. This information is used by the
     * application's observers to incrementally update a PCJ.
     */
    private static class NewQueryVisitor extends QueryModelVisitorBase<RuntimeException> {

        private final NodeIds nodeIds;
        private final FluoQuery.Builder fluoQueryBuilder;
        private final String sparql;

        /**
         * Stored with each Filter node so that we can figure out how to evaluate it within
         * {@link FilterResultUpdater}. Incremented each time a filter has been stored.
         */
        private int filterIndexWithinQuery = 0;

        /**
         * Constructs an instance of {@link NewQueryVisitor}.
         *
         * @param sparql - The SPARQL query whose structure will be represented
         *   within a Fluo application. (not null)
         * @param fluoQueryBuilder - The builder that will be updated by this
         *   vistior to include metadata about each of the query nodes. (not null)
         * @param nodeIds - The NodeIds object is passed in so that other parts
         *   of the application may look up which ID is associated with each
         *   node of the query.
         */
        public NewQueryVisitor(final String sparql, final FluoQuery.Builder fluoQueryBuilder, final NodeIds nodeIds) {
            this.sparql = checkNotNull(sparql);
            this.fluoQueryBuilder = checkNotNull(fluoQueryBuilder);
            this.nodeIds = checkNotNull(nodeIds);
        }

        /**
         * If we encounter an Extension node that contains a Group, then we've found an aggregation.
         */
        @Override
        public void meet(final Extension node) {
            final TupleExpr arg = node.getArg();
            if(arg instanceof Group) {
                final Group group = (Group) arg;

                // Get the Aggregation Node's id.
                final String aggregationId = nodeIds.getOrMakeId(node);

                // Get the group's child node id. This call forces it to be a supported child type.
                final TupleExpr child = group.getArg();
                final String childNodeId = nodeIds.getOrMakeId( child );

                // Get the list of group by binding names.
                VariableOrder groupByVariableOrder = null;
                if(!group.getGroupBindingNames().isEmpty()) {
                    groupByVariableOrder = new VariableOrder(group.getGroupBindingNames());
                } else {
                    groupByVariableOrder = new VariableOrder();
                }

                // The aggregations that need to be performed are the Group Elements.
                final List<AggregationElement> aggregations = new ArrayList<>();
                for(final GroupElem groupElem : group.getGroupElements()) {
                    // Figure out the type of the aggregation.
                    final AggregateOperator operator = groupElem.getOperator();
                    final Optional<AggregationType> type = AggregationType.byOperatorClass( operator.getClass() );

                    // If the type is one we support, create the AggregationElement.
                    if(type.isPresent()) {
                        final String resultBindingName = groupElem.getName();

                        final AtomicReference<String> aggregatedBindingName = new AtomicReference<>();
                        groupElem.visitChildren(new QueryModelVisitorBase<RuntimeException>() {
                            @Override
                            public void meet(final Var node) {
                                aggregatedBindingName.set( node.getName() );
                            }
                        });

                        aggregations.add( new AggregationElement(type.get(), aggregatedBindingName.get(), resultBindingName) );
                    }
                }

                // Update the aggregation's metadata.
                AggregationMetadata.Builder aggregationBuilder = fluoQueryBuilder.getAggregateBuilder(aggregationId).orNull();
                if(aggregationBuilder == null) {
                    aggregationBuilder = AggregationMetadata.builder(aggregationId);
                    fluoQueryBuilder.addAggregateMetadata(aggregationBuilder);
                }

                aggregationBuilder.setChildNodeId(childNodeId);
                aggregationBuilder.setGroupByVariableOrder(groupByVariableOrder);
                for(final AggregationElement aggregation : aggregations) {
                    aggregationBuilder.addAggregation(aggregation);
                }

                // Update the child node's metadata.
                final Set<String> childVars = getVars(child);
                final VariableOrder childVarOrder = new VariableOrder(childVars);

                setChildMetadata(childNodeId, childVarOrder, aggregationId);
            }

            // Walk to the next node.
            super.meet(node);
        }

        @Override
        public void meet(final StatementPattern node) {
            // Extract metadata that will be stored from the node.
            final String spNodeId = nodeIds.getOrMakeId(node);
            final String pattern = FluoStringConverter.toStatementPatternString(node);

            // Get or create a builder for this node populated with the known metadata.
            StatementPatternMetadata.Builder spBuilder = fluoQueryBuilder.getStatementPatternBuilder(spNodeId).orNull();
            if(spBuilder == null) {
                spBuilder = StatementPatternMetadata.builder(spNodeId);
                fluoQueryBuilder.addStatementPatternBuilder(spBuilder);
            }
            spBuilder.setStatementPattern(pattern);
        }

        @Override
        public void meet(final LeftJoin node) {
            // Extract the metadata that will be stored for the node.
            final String leftJoinNodeId = nodeIds.getOrMakeId(node);
            final QueryModelNode left = node.getLeftArg();
            final QueryModelNode right = node.getRightArg();

            // Update the metadata for the JoinMetadata.Builder.
            makeJoinMetadata(leftJoinNodeId, JoinType.LEFT_OUTER_JOIN, left, right);

            // Walk to the next node.
            super.meet(node);
        }

        @Override
        public void meet(final Join node) {
            // Extract the metadata that will be stored from the node.
            final String joinNodeId = nodeIds.getOrMakeId(node);
            final QueryModelNode left = node.getLeftArg();
            final QueryModelNode right = node.getRightArg();

            // Update the metadata for the JoinMetadata.Builder.
            makeJoinMetadata(joinNodeId, JoinType.NATURAL_JOIN, left, right);

            // Walk to the next node.
            super.meet(node);
        }

        private void makeJoinMetadata(final String joinNodeId, final JoinType joinType, final QueryModelNode left, final QueryModelNode right) {
            final String leftChildNodeId = nodeIds.getOrMakeId(left);
            final String rightChildNodeId = nodeIds.getOrMakeId(right);

            // Get or create a builder for this node populated with the known metadata.
            JoinMetadata.Builder joinBuilder = fluoQueryBuilder.getJoinBuilder(joinNodeId).orNull();
            if(joinBuilder == null) {
                joinBuilder = JoinMetadata.builder(joinNodeId);
                fluoQueryBuilder.addJoinMetadata(joinBuilder);
            }
            joinBuilder.setJoinType(joinType);
            joinBuilder.setLeftChildNodeId( leftChildNodeId );
            joinBuilder.setRightChildNodeId( rightChildNodeId );

            // Figure out the variable order for each child node's binding set and
            // store it. Also store that each child node's parent is this join.
            final Set<String> leftVars = getVars((TupleExpr)left);
            final Set<String> rightVars = getVars((TupleExpr) right);
            final JoinVarOrders varOrders = getJoinArgVarOrders(leftVars, rightVars);

            // Create or update the left child's variable order and parent node id.
            final VariableOrder leftVarOrder = varOrders.getLeftVarOrder();
            setChildMetadata(leftChildNodeId, leftVarOrder, joinNodeId);

            // Create or update the right child's variable order and parent node id.
            final VariableOrder rightVarOrder = varOrders.getRightVarOrder();
            setChildMetadata(rightChildNodeId, rightVarOrder, joinNodeId);
        }

        @Override
        public void meet(final Filter node) {
            // Get or create a builder for this node populated with the known metadata.
            final String filterId = nodeIds.getOrMakeId(node);

            FilterMetadata.Builder filterBuilder = fluoQueryBuilder.getFilterBuilder(filterId).orNull();
            if(filterBuilder == null) {
                filterBuilder = FilterMetadata.builder(filterId);
                fluoQueryBuilder.addFilterMetadata(filterBuilder);
            }

            filterBuilder.setOriginalSparql(sparql);
            filterBuilder.setFilterIndexWithinSparql(filterIndexWithinQuery++);

            final QueryModelNode child = node.getArg();
            if(child == null) {
                throw new IllegalArgumentException("Filter arg connot be null.");
            }

            final String childNodeId = nodeIds.getOrMakeId(child);
            filterBuilder.setChildNodeId(childNodeId);

            // Update the child node's metadata.
            final Set<String> childVars = getVars((TupleExpr)child);
            final VariableOrder childVarOrder = new VariableOrder(childVars);
            setChildMetadata(childNodeId, childVarOrder, filterId);

            // Walk to the next node.
            super.meet(node);
        }

        @Override
        public void meet(final Projection node) {
            // Create a builder for this node populated with the metadata.
            final String queryId = nodeIds.getOrMakeId(node);
            final VariableOrder queryVarOrder = new VariableOrder(node.getBindingNames());

            final QueryMetadata.Builder queryBuilder = QueryMetadata.builder(queryId);
            fluoQueryBuilder.setQueryMetadata(queryBuilder);

            queryBuilder.setSparql(sparql);
            queryBuilder.setVariableOrder(queryVarOrder);

            final QueryModelNode child = node.getArg();
            if(child == null) {
                throw new IllegalArgumentException("Projection arg connot be null.");
            }

            final String childNodeId = nodeIds.getOrMakeId(child);
            queryBuilder.setChildNodeId(childNodeId);

            // Update the child node's metadata.
            final Set<String> childVars = getVars((TupleExpr)child);
            final VariableOrder childVarOrder = new VariableOrder(childVars);

            setChildMetadata(childNodeId, childVarOrder, queryId);

            // Walk to the next node.
            super.meet(node);
        }
        
        
        public void meet(Reduced node) {
            //create id, initialize ConstructQueryMetadata builder, register ConstructQueryMetadata 
            //builder with FluoQueryBuilder, and add metadata that we currently have
            final String constructId = nodeIds.getOrMakeId(node);
            final ConstructQueryMetadata.Builder constructBuilder = ConstructQueryMetadata.builder();
            constructBuilder.setNodeId(constructId);
            fluoQueryBuilder.setConstructQueryMetadata(constructBuilder);
            constructBuilder.setSparql(sparql);
            
            //get child node
            QueryModelNode child = node.getArg();
            Preconditions.checkArgument(child instanceof Projection || child instanceof MultiProjection);
            UnaryTupleOperator unary = (UnaryTupleOperator) child;
            
            //get ProjectionElemList to build ConstructGraph
            final List<ProjectionElemList> projections = new ArrayList<>();
            if(unary instanceof Projection) {
                projections.add(((Projection) unary).getProjectionElemList());
            } else {
                projections.addAll(((MultiProjection)unary).getProjections());
            }
            
            //get ExtensionElems to build ConstructGraph
            QueryModelNode grandChild = unary.getArg();
            Preconditions.checkArgument(grandChild instanceof Extension);
            Extension extension = (Extension) grandChild;
            final List<ExtensionElem> extensionElems = extension.getElements();
            final ConstructGraph graph = getConstructGraph(projections, extensionElems);
            constructBuilder.setConstructGraph(graph);
            
            //set child to the next node we care about in Fluo
            //if Extension's arg is a Group node, then it is an Aggregation, so set child to Extension
            //otherwise set child to Extension's child (only care about Extensions if they are Aggregations)
            if(extension.getArg() instanceof Group) {
                child = extension;
            } else {
                child = extension.getArg();
            }
            
            //Set the child node in the ConstructQueryMetadataBuilder
            String childNodeId = nodeIds.getOrMakeId(child);
            constructBuilder.setChildNodeId(childNodeId);
            
            // Update the child node's metadata.
            final Set<String> childVars = getVars((TupleExpr)child);
            final VariableOrder childVarOrder = new VariableOrder(childVars);
            setChildMetadata(childNodeId, childVarOrder, constructId);
            
            //fast forward visitor to next node we care about
            child.visit(this);
        }
        

        /**
         * Update a query node's metadata to include it's binding set variable order
         * and it's parent node id. This information is only known when handling
         * the parent node.
         *
         * @param childNodeId - The node ID of the child node.
         * @param childVarOrder - The variable order of the child node's binding sets.
         * @param parentNodeId - The node ID that consumes the child's binding sets.
         */
        private void setChildMetadata(final String childNodeId, final VariableOrder childVarOrder, final String parentNodeId) {
            checkNotNull(childNodeId);
            checkNotNull(childVarOrder);
            checkNotNull(parentNodeId);

            final NodeType childType = NodeType.fromNodeId(childNodeId).get();
            switch (childType) {
            case STATEMENT_PATTERN:
                StatementPatternMetadata.Builder spBuilder = fluoQueryBuilder.getStatementPatternBuilder(childNodeId).orNull();
                if (spBuilder == null) {
                    spBuilder = StatementPatternMetadata.builder(childNodeId);
                    fluoQueryBuilder.addStatementPatternBuilder(spBuilder);
                }

                spBuilder.setVarOrder(childVarOrder);
                spBuilder.setParentNodeId(parentNodeId);
                break;

            case JOIN:
                JoinMetadata.Builder joinBuilder = fluoQueryBuilder.getJoinBuilder(childNodeId).orNull();
                if (joinBuilder == null) {
                    joinBuilder = JoinMetadata.builder(childNodeId);
                    fluoQueryBuilder.addJoinMetadata(joinBuilder);
                }

                joinBuilder.setVariableOrder(childVarOrder);
                joinBuilder.setParentNodeId(parentNodeId);
                break;

            case FILTER:
                FilterMetadata.Builder filterBuilder = fluoQueryBuilder.getFilterBuilder(childNodeId).orNull();
                if (filterBuilder == null) {
                    filterBuilder = FilterMetadata.builder(childNodeId);
                    fluoQueryBuilder.addFilterMetadata(filterBuilder);
                }

                filterBuilder.setVarOrder(childVarOrder);
                filterBuilder.setParentNodeId(parentNodeId);
                break;

            case AGGREGATION:
                AggregationMetadata.Builder aggregationBuilder = fluoQueryBuilder.getAggregateBuilder(childNodeId).orNull();
                if (aggregationBuilder == null) {
                    aggregationBuilder = AggregationMetadata.builder(childNodeId);
                    fluoQueryBuilder.addAggregateMetadata(aggregationBuilder);
                }

                aggregationBuilder.setVariableOrder(childVarOrder);
                aggregationBuilder.setParentNodeId(parentNodeId);
                break;

            case QUERY:
                throw new IllegalArgumentException("A QUERY node cannot be the child of another node.");
            case CONSTRUCT:
                throw new IllegalArgumentException("A CONSTRUCT node cannot be the child of another node.");
            default:
                throw new IllegalArgumentException("Unsupported NodeType: " + childType);
            }
        }
        
        private ConstructGraph getConstructGraph(List<ProjectionElemList> projections, List<ExtensionElem> extensionElems) {
            Map<String, Value> valueMap = new HashMap<>();
            //create valueMap to associate source names with Values
            for(ExtensionElem elem: extensionElems) {
                String name = elem.getName();
                ValueExpr expr = elem.getExpr();
                if(expr instanceof ValueConstant) {
                    Value value = ((ValueConstant) expr).getValue();
                    valueMap.put(name, value);
                } else if(expr instanceof BNodeGenerator) {
                    valueMap.put(name, new BNodeImpl(UUID.randomUUID().toString()));
                }
            }
            
            Set<ConstructProjection> constructProj = new HashSet<>();
            //build ConstructProjection for each ProjectionElemList
            for(ProjectionElemList list: projections) {
                validateProjectionElemList(list);
                List<Var> vars = new ArrayList<>();
                for(ProjectionElem elem: list.getElements()) {
                    String sourceName = elem.getSourceName();
                    Var var = new Var(sourceName);
                    if(valueMap.containsKey(sourceName)) {
                        var.setValue(valueMap.get(sourceName));
                    }
                    vars.add(var);
                }
                constructProj.add(new ConstructProjection(vars.get(0), vars.get(1), vars.get(2)));
            }
            
            return new ConstructGraph(constructProj);
        }
        
        private void validateProjectionElemList(ProjectionElemList list) {
            List<ProjectionElem> elements = list.getElements();
            checkArgument(elements.size() == 3);
            checkArgument(elements.get(0).getTargetName().equals("subject"));
            checkArgument(elements.get(1).getTargetName().equals("predicate"));
            checkArgument(elements.get(2).getTargetName().equals("object"));
        }
        
        

        /**
         * Get the non-constant variables from a {@link TupleExpr}.
         *
         * @param node - The node to inspect for variables. (not null)
         * @return The non-constant variables that were part of the node.
         */
        private Set<String> getVars(final TupleExpr node) {
            checkNotNull(node);

            final Set<String> vars = Sets.newHashSet();

            for(final String bindingName : node.getBindingNames()) {
                if(!bindingName.startsWith("-const-")) {
                    vars.add(bindingName);
                }
            }

            return vars;
        }

        /**
         * Holds the Variable Order of the binding sets for the children of a join node.
         */
        @Immutable
        @DefaultAnnotation(NonNull.class)
        private static final class JoinVarOrders {
            private final VariableOrder leftVarOrder;
            private final VariableOrder rightVarOrder;

            /**
             * Constructs an instance of {@link }.
             *
             * @param leftVarOrder - The left child's Variable Order. (not null)
             * @param rightVarOrder - The right child's Variable Order. (not null)
             */
            public JoinVarOrders(final VariableOrder leftVarOrder, final VariableOrder rightVarOrder) {
                this.leftVarOrder = checkNotNull(leftVarOrder);
                this.rightVarOrder = checkNotNull(rightVarOrder);
            }

            /**
             * @return The left child's Variable Order.
             */
            public VariableOrder getLeftVarOrder() {
                return leftVarOrder;
            }

            /**
             * @return The right child's Variable Order.
             */
            public VariableOrder getRightVarOrder() {
                return rightVarOrder;
            }
        }

        /**
         * Shifts the common variables between the two children to the left so
         * that Accumulo scans when performing the join are efficient.
         *
         * @param leftVars - The left child's variables. (not null)
         * @param rightVars - The right child's variables. (not null)
         * @return An object holding the left and right children's variable orders.
         */
        private JoinVarOrders getJoinArgVarOrders(final Set<String> leftVars, final Set<String> rightVars) {
            checkNotNull(leftVars);
            checkNotNull(rightVars);

            // Find the common variables between the left and right children. The common vars
            // are stored in a list to ensure iteration order is always the same.
            final List<String> commonVars = new ArrayList<>( Sets.intersection(leftVars, rightVars) );

            // Push all of the common variables to the left for each child's vars.
            final List<String> leftVarOrder = leftShiftCommonVars(commonVars, leftVars);
            final List<String> rightVarOrder = leftShiftCommonVars(commonVars, rightVars);
            return new JoinVarOrders(new VariableOrder(leftVarOrder), new VariableOrder(rightVarOrder));
        }

        /**
         * Orders the set of common variables so that all of the common ones
         * are on the left in the same order they have been provided. The rest
         * of the variables are iterated over and added to the end of the list
         * in no particular order.
         *
         * @param commonVars - An ordered list of variables that must appear on the left. (not null)
         * @param allVars - The variables that need to be ordered. (not null)
         * @return A list of variables ordered as described above.
         */
        private List<String> leftShiftCommonVars(final List<String> commonVars, final Collection<String> allVars) {
            checkNotNull(commonVars);
            checkNotNull(allVars);

            final List<String> shifted = Lists.newArrayList(commonVars);
            for(final String var : allVars) {
                if(!shifted.contains(var)) {
                    shifted.add(var);
                }
            }
            return shifted;
        }
    }
}