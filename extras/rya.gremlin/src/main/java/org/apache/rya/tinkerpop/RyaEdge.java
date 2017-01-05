package org.apache.rya.tinkerpop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.rya.api.RdfCloudTripleStoreConstants;
import org.apache.rya.api.domain.RyaStatement;
import org.apache.rya.api.persist.RyaDAO;
import org.apache.rya.api.persist.RyaDAOException;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class RyaEdge implements Edge {
    private RyaStatement statement;
    private RyaGraph graph;

    public RyaEdge(RyaStatement statement, RyaGraph graph) {
        this.statement = statement;
        this.graph = graph;
    }

    @Override
    public Object id() {
        return statement.getSubject().getData() + RdfCloudTripleStoreConstants.DELIM + 
                statement.getPredicate() + RdfCloudTripleStoreConstants.DELIM + 
                statement.getObject().getData() +
                RdfCloudTripleStoreConstants.DELIM + statement.getObject().getDataType().toString();
    }

    @Override
    public String label() {
        return statement.getPredicate().getData();
    }

    @Override
    public Graph graph() {
        return graph;
    }

    @Override
    public <V> Property<V> property(String key, V value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void remove() {
        RyaDAO dao = graph.getDAO();
        try {
            dao.delete(statement, dao.getConf());
        } catch (RyaDAOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public Iterator<Vertex> vertices(Direction direction) {
        List<Vertex> vertices = new ArrayList<Vertex>();
        if (Direction.IN == direction){
            vertices.add(new RyaVertex(statement.getObject(), graph));
        }
        else if (Direction.OUT == direction){
            vertices.add(new RyaVertex(statement.getSubject(), graph));
        }
        else {
            vertices.add(new RyaVertex(statement.getSubject(), graph));
            vertices.add(new RyaVertex(statement.getObject(), graph));
        }
        return vertices.iterator();
    }

    @Override
    public <V> Iterator<Property<V>> properties(String... propertyKeys) {
        // TODO Auto-generated method stub
        return null;
    }

}
