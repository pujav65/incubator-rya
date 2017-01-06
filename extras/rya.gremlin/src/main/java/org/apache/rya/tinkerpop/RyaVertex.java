package org.apache.rya.tinkerpop;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.rya.api.RdfCloudTripleStoreConfiguration;
import org.apache.rya.api.RdfCloudTripleStoreConstants;
import org.apache.rya.api.domain.RyaStatement;
import org.apache.rya.api.domain.RyaType;
import org.apache.rya.api.domain.RyaURI;
import org.apache.rya.api.domain.StatementMetadata;
import org.apache.rya.api.persist.RyaDAO;
import org.apache.rya.api.persist.RyaDAOException;
import org.apache.rya.api.persist.query.RyaQuery;
import org.apache.rya.api.persist.query.RyaQueryEngine;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality;
import org.apache.tinkerpop.gremlin.util.iterator.MultiIterator;
import org.calrissian.mango.collect.CloseableIterable;
import org.openrdf.model.vocabulary.XMLSchema;

public class RyaVertex implements Vertex {
    
    private RyaType type;
    private RyaGraph graph;

    public RyaVertex(RyaType ryaType, RyaGraph ryaGraph) {
       this.type = ryaType;
       this.graph = ryaGraph;
    }

    @Override
    public Graph graph() {
        return graph;
    }

    @Override
    public Object id() {
        return type.getData() + RdfCloudTripleStoreConstants.DELIM + type.getDataType().toString();
    }

    @Override
    public String label() {
        return type.getData();
    }

    @Override
    public <V> VertexProperty<V> property(String arg0, V arg1) {
        // TODO not implemented
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove() {   
        // first query for all of the statements referencing this vertex as either subject/object
        if (isValidSubjectVertex(type)) {
            RyaStatement subjStatement = new RyaStatement();
            subjStatement.setSubject(new RyaURI(type.getData()));
            deleteQuery(new RyaQuery(subjStatement));
         }
        RyaStatement objStatement = new RyaStatement();
        objStatement.setObject(type);
        deleteQuery(new RyaQuery(objStatement));
    }
    
    private void deleteQuery(RyaQuery query){
        RyaDAO<RdfCloudTripleStoreConfiguration> dao = graph.getDAO();
        RyaQueryEngine queryEngine = dao.getQueryEngine();
        try {
            CloseableIterable<RyaStatement> statementIterable = queryEngine.query(query);
            for (RyaStatement statement : statementIterable){
                dao.delete(statement, dao.getConf());
            }
            statementIterable.close();
        } catch (RyaDAOException | IOException e) {
            
        } 
    }
    
    private static boolean isValidSubjectVertex(RyaType type){
        return (type instanceof RyaURI) || type.getDataType().equals(XMLSchema.ANYURI);
    }

    @Override
    public Edge addEdge(String predicate, Vertex outVertex, Object... keyValues) {
        if (RyaTinkerpopUtils.isValidURI(predicate) && isValidSubjectVertex(type)){
            RyaVertex objectVertex = null;
            // create a rya type from the outVertex
            if (!(outVertex instanceof RyaVertex)) {
                // create a RyaVertex from the Vertex
                objectVertex = RyaVertex.from(outVertex, graph);
            }
            else {
                objectVertex = (RyaVertex) outVertex;
            }
            StatementMetadata statementMetadata = new StatementMetadata();
            for (int i = 0; i < keyValues.length; i = i + 2) {
                RyaType type = null;
                if (keyValues[i] instanceof RyaType){
                    type = (RyaType) keyValues[i];
                }
                else if (keyValues[i] instanceof String){
                    String id = (String) keyValues[i];
                    if (RyaTinkerpopUtils.isValidURI(id)) {
                        type = new RyaURI(id);
                    }
                }
                if (type != null){
                    RyaType value = null;
                    if (keyValues[i+1] instanceof RyaType){
                        value = (RyaType) keyValues[i+1];
                    }
                    else {
                        value = new RyaType(keyValues[i+1].toString());
                    }
                    statementMetadata.addMetadata(type.getData(), value.getData());
                }
            }
            
            RyaStatement statementToPersist = new RyaStatement();
            statementToPersist.setSubject(new RyaURI(type.getData()));
            statementToPersist.setPredicate(new RyaURI(predicate));
            statementToPersist.setObject(objectVertex.type);
            statementToPersist.setStatementMetadata(statementMetadata);
            try {
                graph.getDAO().add(statementToPersist);
            } catch (RyaDAOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return new RyaEdge(statementToPersist, graph);
            
        }
        throw new IllegalArgumentException("Input arguments to add Edge are invalid!");
    }

    private static RyaVertex from(Vertex outVertex, RyaGraph graph) {
        Object[] id = new Object[]{T.id, outVertex.id()};
        Object[] label = new Object[]{T.label, outVertex.label()};
        return RyaTinkerpopUtils.getVertex(graph, id, label);
    }
    

    @Override
    public Iterator<Edge> edges(Direction direction, String... labels) {
        MultiIterator<Edge> it = new MultiIterator<Edge>(); 
        try {
            // if we are either doing an incoming edge or both directions
            if (Direction.OUT != direction){    
                for (String pred : labels){
                    if (RyaTinkerpopUtils.isValidURI(pred)){
                        RyaStatement statement = new RyaStatement();
                        statement.setObject(type);
                        statement.setPredicate(new RyaURI(pred));
                        CloseableIterable<RyaStatement> statementIt = graph.getDAO().getQueryEngine().query(new RyaQuery(statement));
                        it.addIterator(new RyaEdgeIterable(statementIt, graph).iterator());
                    }
                }
            }
            // if we are either doing an outgoing edge or both directions and it is a valid subject
            if ((Direction.IN != direction) && isValidSubjectVertex(type)){    
                for (String pred : labels){
                    if (RyaTinkerpopUtils.isValidURI(pred)){
                        RyaStatement statement = new RyaStatement();
                        statement.setSubject(new RyaURI(type.getData()));
                        statement.setPredicate(new RyaURI(pred));
                        CloseableIterable<RyaStatement> statementIt = graph.getDAO().getQueryEngine().query(new RyaQuery(statement));
                        it.addIterator(new RyaEdgeIterable(statementIt, graph).iterator());
                    }
                }
            }
        }catch (RyaDAOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return it;
    }

    @Override
    public <V> Iterator<VertexProperty<V>> properties(String... keys) {
        // TODO not implemented
        return new ArrayList<VertexProperty<V>>().iterator();
    }

    @Override
    public <V> VertexProperty<V> property(Cardinality arg0, String arg1, V arg2, Object... arg3) {
        // TODO not implemented
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Vertex> vertices(Direction direction, String... labels) {
        MultiIterator<Vertex> it = new MultiIterator<Vertex>(); 
        try {
            // if we are either doing an incoming edge or both directions
            if (Direction.OUT != direction){    
                for (String pred : labels){
                    if (RyaTinkerpopUtils.isValidURI(pred)){
                        RyaStatement statement = new RyaStatement();
                        statement.setObject(type);
                        statement.setPredicate(new RyaURI(pred));
                        CloseableIterable<RyaStatement> statementIt = graph.getDAO().getQueryEngine().query(new RyaQuery(statement));
                        it.addIterator(new RyaEdgeVertexIterable(statementIt, graph, direction).iterator());
                    }
                }
            }
            // if we are either doing an outgoing edge or both directions and it is a valid subject
            if ((Direction.IN != direction) && isValidSubjectVertex(type)){    
                for (String pred : labels){
                    if (RyaTinkerpopUtils.isValidURI(pred)){
                        RyaStatement statement = new RyaStatement();
                        statement.setSubject(new RyaURI(type.getData()));
                        statement.setPredicate(new RyaURI(pred));
                        CloseableIterable<RyaStatement> statementIt = graph.getDAO().getQueryEngine().query(new RyaQuery(statement));
                        it.addIterator(new RyaEdgeVertexIterable(statementIt, graph, direction).iterator());
                    }
                }
            }
        }catch (RyaDAOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return it;
    }

    public RyaType getType() {
        return type;
    }

}
