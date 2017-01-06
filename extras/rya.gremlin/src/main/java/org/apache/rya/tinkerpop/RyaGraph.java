package org.apache.rya.tinkerpop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.rya.api.domain.RyaStatement;
import org.apache.rya.api.persist.RyaDAO;
import org.apache.rya.api.persist.query.RyaQuery;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class RyaGraph implements Graph {
    
    private RyaDAO dao;
    
    public RyaDAO getDAO() {
        return dao;
    }

    @Override
    public Vertex addVertex(Object... keyValues) {
        // TODO this isn't persisted -- this is probably not the intent here.
        return RyaTinkerpopUtils.getVertex(this, keyValues);
    }
    

    @Override
    public void close() throws Exception {
        dao.destroy(); //TODO not sure if this is right
    }

    @Override
    public GraphComputer compute() throws IllegalArgumentException {
        throw Graph.Exceptions.graphComputerNotSupported();
    }

    @Override
    public <C extends GraphComputer> C compute(Class<C> arg0) throws IllegalArgumentException {
        throw Graph.Exceptions.graphComputerNotSupported();
    }

    @Override
    public Configuration configuration() {
        // TODO Auto-generated method stub
        return null;
//        return dao.getConf();
    }

    @Override
    public Iterator<Edge> edges(Object... edgeIDs) {
        // TODO ignoring edge ids right now
        dao.getQueryEngine().query(new RyaQuery(new RyaStatement()));
        return null;
    }

    @Override
    public Transaction tx() {
        throw Graph.Exceptions.transactionsNotSupported();
    }

    @Override
    public Variables variables() {
        throw Graph.Exceptions.variablesNotSupported();
    }

    @Override
    public Iterator<Vertex> vertices(Object... vertexIDs) {
        List<Vertex> vertices = new ArrayList<Vertex>();
        for (Object id : vertexIDs){
            Object[] idKeyPair = new Object[]{T.id, id};
            this.addVertex(idKeyPair);
        }
        
        return vertices.iterator();
    }

}
