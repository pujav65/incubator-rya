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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.rya.api.domain.RyaStatement;
import org.apache.rya.api.persist.RyaDAO;
import org.apache.rya.api.persist.RyaDAOException;
import org.apache.rya.api.persist.query.RyaQuery;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.calrissian.mango.collect.CloseableIterable;

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
    }

    @Override
    public Iterator<Edge> edges(Object... edgeIDs) {
        // TODO ignoring edge ids right now
        try {
            RyaStatement statement = new RyaStatement();
            CloseableIterable<RyaStatement> statementIt = dao.getQueryEngine().query(new RyaQuery(statement));
            return new RyaEdgeIterable(statementIt, this).iterator();
        } catch (RyaDAOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new ArrayList<Edge>().iterator();
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
            addVertex(idKeyPair);
        }       
        return vertices.iterator();
    }

}
