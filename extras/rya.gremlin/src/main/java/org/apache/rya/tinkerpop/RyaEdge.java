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
import java.util.Map;

import org.apache.rya.api.RdfCloudTripleStoreConstants;
import org.apache.rya.api.domain.RyaStatement;
import org.apache.rya.api.domain.StatementMetadata;
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
        statement.getMetadata().addMetadata(key, value.toString());
        return (Property<V>) new RyaProperty(key, value.toString(), this);
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
        StatementMetadata metadata = statement.getMetadata();
        List<Property<V>> properties = new ArrayList<Property<V>>();
        for (String key : propertyKeys){
            if (metadata.getValue(key) != null){
                properties.add((Property<V>) new RyaProperty(key, metadata.getValue(key), this));
            }
        }
        if (propertyKeys.length == 0){
            Map<String, String> metadataMap = metadata.asMap();
            for (String key : metadataMap.keySet()){
                properties.add((Property<V>) new RyaProperty(key, metadataMap.get(key), this));
            }
        }
        return  properties.iterator();
    }

}
