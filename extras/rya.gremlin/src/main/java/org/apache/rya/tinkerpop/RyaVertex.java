package org.apache.rya.tinkerpop;

import java.io.IOException;
import java.util.Iterator;

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
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality;
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
    public Edge addEdge(String predicate, Vertex outVertex, Object... arg2) {
        if (RyaTinkerpopUtils.isValidURI(predicate) && isValidSubjectVertex(type)){
            RyaVertex objectVertex = null;
            // create a rya type from the outVertex
            if (!(outVertex instanceof RyaVertex)) {
                // create a RyaVertex from the Vertex
                objectVertex = RyaVertex.from(outVertex);
            }
            else {
                objectVertex = (RyaVertex) outVertex;
            }
            // TODO persist the metadata
            StatementMetadata statementMetadata = new StatementMetadata();
            
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
            return new RyaEdge(statementToPersist);
            
        }
        return null;
    }

    private static RyaVertex from(Vertex outVertex) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<Edge> edges(Direction direction, String... arg1) {
        
        return null;
    }

    @Override
    public <V> Iterator<VertexProperty<V>> properties(String... keys) {
        return null;
    }

    @Override
    public <V> VertexProperty<V> property(Cardinality arg0, String arg1, V arg2, Object... arg3) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<Vertex> vertices(Direction arg0, String... arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public RyaType getType() {
        return type;
    }

}
