package org.apache.rya.tinkerpop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.configuration.Configuration;
import org.apache.rya.api.RdfCloudTripleStoreConstants;
import org.apache.rya.api.domain.RyaType;
import org.apache.rya.api.domain.RyaURI;
import org.apache.rya.api.persist.RyaDAO;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;

public class RyaGraph implements Graph {
    
    private RyaDAO dao;
    private static ValueFactory valueFactory = new ValueFactoryImpl();
    private static RyaURI vertexDeclPred = new RyaURI(RDF.TYPE.toString());
    private static RyaURI vertexDeclObj = new RyaURI("urn:rya:gremlin:Vertex");
    
    public RyaDAO getDAO() {
        return dao;
    }

    @Override
    public Vertex addVertex(Object... keyValues) {
        // TODO this isn't persisted -- this is probably not the intent here.
        return getVertex(keyValues);
    }

    public RyaVertex getVertex(Object... keyValues) {
        // look for the label, otherwise look for the id
        Optional<Object> nodeID = ElementHelper.getIdValue(keyValues);
        RyaType ryaType;
        if (nodeID.isPresent() && RyaTinkerpopUtils.validID(nodeID.get().toString())){
            String id = nodeID.get().toString();
            String[] delimId = id.split(RdfCloudTripleStoreConstants.DELIM);
            if (delimId.length == 2){
               String value = delimId[0];
               String type = delimId[1];
               ryaType = new RyaType(valueFactory.createURI(type), value);
            }
            else {
                // TODO should we assume it is a uri if it doesn't have a type?
                // right now assuming it is a uri, but this may be poorly formed
                // maybe we should check to see if it is a well formed URI before accepting?
                if (RyaTinkerpopUtils.isValidURI(id)){
                    ryaType = new RyaURI(id);                 
                }
                else {
                    ryaType = new RyaType(id);
                }
            }      
        }
        else {
            Optional<String> label = ElementHelper.getLabelValue(keyValues);
            if (label.isPresent() && label.get().isEmpty()){
                String value = label.get();
                ryaType = new RyaType(value);
            }
            else {
                // TODO better exception handling
                throw Graph.Exceptions.vertexAdditionsNotSupported();
             }
        }     
        return new RyaVertex(ryaType, this);
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
        // TODO Auto-generated method stub
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
