package org.apache.rya.tinkerpop;

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
        // TODO Auto-generated method stub
        return new RyaVertexEdgeIterator(statementIt.iterator(), graph, direction);
    }
    
    private static class RyaVertexEdgeIterator<Vertex> implements Iterator<Vertex> {
        
        private Iterator<RyaStatement> statementIt;
        private RyaGraph graph;
        private Direction direction;

        public RyaVertexEdgeIterator(Iterator<RyaStatement> iterator, RyaGraph graph, Direction direction) {
            this.statementIt = iterator;
            this.graph = graph;
            this.direction = direction;
        }

        @Override
        public boolean hasNext() {
            return statementIt.hasNext();
        }

        @Override
        public Vertex next() {
            RyaStatement statement = statementIt.next();
            RyaType type = statement.getObject();
            if (direction == Direction.IN){
                type = statement.getObject();
            }
            else if (direction == Direction.OUT){
                type = statement.getSubject();
            }
            // TODO no support for both
            return (Vertex) new RyaVertex(type, graph);
        }
        
    }

}
