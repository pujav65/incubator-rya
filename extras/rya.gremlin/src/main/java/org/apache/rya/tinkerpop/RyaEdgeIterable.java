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
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.calrissian.mango.collect.CloseableIterable;

public class RyaEdgeIterable implements Iterable<Edge> {
    
    

    private CloseableIterable<RyaStatement> statementIt;
    private RyaGraph graph;

    public RyaEdgeIterable(CloseableIterable<RyaStatement> statementIt, RyaGraph graph) {
        this.statementIt = statementIt;
    }

    @Override
    public Iterator<Edge> iterator() {
        return new RyaEdgeIterator(statementIt.iterator(), graph);
    }
    
    private static class RyaEdgeIterator<Edge> implements Iterator<Edge> {
        
        private Iterator<RyaStatement> statementIt;
        private RyaGraph graph;

        public RyaEdgeIterator(Iterator<RyaStatement> iterator, RyaGraph graph) {
            this.statementIt = iterator;
            this.graph = graph;
        }

        @Override
        public boolean hasNext() {
            return statementIt.hasNext();
        }

        @Override
        public Edge next() {
            return (Edge) new RyaEdge(statementIt.next(), graph);
        }
        
    }

}
