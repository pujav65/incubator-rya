package rya.tinkerpop;

import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class RyaGraph implements Graph {

	@Override
	public Vertex addVertex(Object... keyValues) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public GraphComputer compute() throws IllegalArgumentException {
		throw new IllegalArgumentException("Not supported.");
	}

	@Override
	public <C extends GraphComputer> C compute(Class<C> graphComputerClass) throws IllegalArgumentException {
		throw new IllegalArgumentException("Not supported.");
	}

	@Override
	public Configuration configuration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Edge> edges(Object... edgeIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Transaction tx() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Variables variables() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Vertex> vertices(Object... vertexIds) {
		// TODO Auto-generated method stub
		return null;
	}

}
