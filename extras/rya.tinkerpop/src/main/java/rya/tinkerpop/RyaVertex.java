package rya.tinkerpop;

import java.util.Iterator;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality;

class RyaVertex implements Vertex{

	@Override
	public Object id() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String label() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Graph graph() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <V> VertexProperty<V> property(String key, V value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Edge addEdge(String label, Vertex inVertex, Object... keyValues) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <V> VertexProperty<V> property(Cardinality cardinality, String key, V value, Object... keyValues) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Edge> edges(Direction direction, String... edgeLabels) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Vertex> vertices(Direction direction, String... edgeLabels) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <V> Iterator<VertexProperty<V>> properties(String... propertyKeys) {
		// TODO Auto-generated method stub
		return null;
	}

}
