package org.apache.rya.tinkerpop;

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
