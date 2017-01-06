package org.apache.rya.tinkerpop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

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
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
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
        return null;
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
    public Iterator<Edge> edges(Direction direction, String... arg1) {
        // TODO ignoring pred labels so far
        if (Direction.IN == direction){    
            try {
                RyaStatement statement = new RyaStatement();
                statement.setObject(type);
                CloseableIterable<RyaStatement> statementIt = graph.getDAO().getQueryEngine().query(new RyaQuery(statement));
                return new RyaEdgeIterable(statementIt, graph).iterator();
            } catch (RyaDAOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if ((Direction.OUT == direction) && isValidSubjectVertex(type)){    
            try {
                RyaStatement statement = new RyaStatement();
                statement.setSubject(new RyaURI(type.getData()));
                CloseableIterable<RyaStatement> statementIt = graph.getDAO().getQueryEngine().query(new RyaQuery(statement));
                return new RyaEdgeIterable(statementIt, graph).iterator();
            } catch (RyaDAOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // TODO not supporting both right now
        List<Edge> edges = new ArrayList<Edge>();
        return edges.iterator();
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
    public Iterator<Vertex> vertices(Direction direction, String... arg1) {
     // TODO ignoring pred labels so far
        if (Direction.IN == direction){    
            try {
                RyaStatement statement = new RyaStatement();
                statement.setObject(type);
                CloseableIterable<RyaStatement> statementIt = graph.getDAO().getQueryEngine().query(new RyaQuery(statement));
                return new RyaEdgeVertexIterable(statementIt, graph, direction).iterator();
            } catch (RyaDAOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if ((Direction.OUT == direction) && isValidSubjectVertex(type)){    
            try {
                RyaStatement statement = new RyaStatement();
                statement.setSubject(new RyaURI(type.getData()));
                CloseableIterable<RyaStatement> statementIt = graph.getDAO().getQueryEngine().query(new RyaQuery(statement));
                return new RyaEdgeVertexIterable(statementIt, graph, direction).iterator();
            } catch (RyaDAOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // TODO not supporting both right now
        List<Vertex> edges = new ArrayList<Vertex>();
        return edges.iterator();
    }

    public RyaType getType() {
        return type;
    }

}
