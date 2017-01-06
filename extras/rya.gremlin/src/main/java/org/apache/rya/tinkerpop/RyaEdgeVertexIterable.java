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

import java.util.Iterator;

import org.apache.rya.api.domain.RyaStatement;
import org.apache.rya.api.domain.RyaType;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.calrissian.mango.collect.CloseableIterable;

public class RyaEdgeVertexIterable implements Iterable<Vertex> {
    
    private CloseableIterable<RyaStatement> statementIt;
    private RyaGraph graph;
    private Direction direction;

    public RyaEdgeVertexIterable(CloseableIterable<RyaStatement> statementIt, RyaGraph graph, 
            Direction direction) {
        this.statementIt = statementIt;
        this.graph = graph;
        this.direction = direction;
    }

    @Override
    public Iterator<Vertex> iterator() {
        return new RyaVertexEdgeIterator(statementIt.iterator(), graph, direction);
    }
    
    private static class RyaVertexEdgeIterator<Vertex> implements Iterator<Vertex> {
        
        private Iterator<RyaStatement> statementIt;
        private RyaStatement currentStatement = null;
        private RyaGraph graph;
        private Direction direction;
        boolean returnedSubject = false;

        public RyaVertexEdgeIterator(Iterator<RyaStatement> iterator, RyaGraph graph, Direction direction) {
            this.statementIt = iterator;
            this.graph = graph;
            this.direction = direction;
        }

        @Override
        public boolean hasNext() {
            return (currentStatement != null) || statementIt.hasNext();
        }

        @Override
        public Vertex next() {
            if (currentStatement == null){
                currentStatement = statementIt.next();
            }
            RyaType type = currentStatement.getObject();
            if (direction == Direction.IN){
                type = currentStatement.getObject();
                currentStatement = null;
            }
            else if (direction == Direction.OUT){
                type = currentStatement.getSubject();
                currentStatement = null;
            }
            else {
                if (!returnedSubject){
                    type = currentStatement.getSubject();
                    returnedSubject = true;
                }
                else {
                    type = currentStatement.getObject();
                    currentStatement = null;
                    returnedSubject = false;
                }
            }
            return (Vertex) new RyaVertex(type, graph);
        }
        
    }

}
