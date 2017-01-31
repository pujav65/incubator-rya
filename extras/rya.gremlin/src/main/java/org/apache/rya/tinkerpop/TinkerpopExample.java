package org.apache.rya.tinkerpop;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.mock.MockInstance;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.apache.rya.accumulo.AccumuloRdfConfiguration;
import org.apache.rya.accumulo.AccumuloRyaDAO;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;

public class TinkerpopExample {

    private static String username = "root";
    private static PasswordToken password = new PasswordToken("");

    private static Instance instance;
    private static AccumuloRyaDAO apiImpl;

   public static void init() throws Exception {
        instance = new MockInstance("mock_instance");
        Connector connector = instance.getConnector(username, password);

        AccumuloRdfConfiguration conf = new AccumuloRdfConfiguration();
        conf.setTablePrefix("rya_");
        conf.setDisplayQueryPlan(false);

        apiImpl = new AccumuloRyaDAO();
        apiImpl.setConf(conf);
        apiImpl.setConnector(connector);
        apiImpl.init();
    }
   
    public static void main(String[] args) throws Exception{
        
        // create a mock Rya instance
        init();
        RyaGraph graph = new RyaGraph(apiImpl);
        Vertex matthew = graph.addVertex(T.id, "urn:Matthew");
        Vertex height = graph.addVertex(T.label, "6.1 ft");
        Vertex weight = graph.addVertex(T.label, "190 lbs");
        Vertex age = graph.addVertex(T.label, "31 years");
       
        System.out.println("\n\nAdding edges and vertices...");
        System.out.println("\nMatthew --> height --> 6.1 ft");
        System.out.println("Matthew --> weight --> 190 lbs");
        System.out.println("Matthew --> age --> 31 years");
        Edge matthewHeight = matthew.addEdge("urn:height", height);
        Edge matthewWeight = matthew.addEdge("urn:weight", weight);
        Edge matthewAge = matthew.addEdge("urn:age", age, "urn:uncertainty", ".75", "urn:edgeWeight", "31");
        System.out.println("\n\nDone...");

        
        // now check to see if we can iterate over those edges
        System.out.println("\n\nIterating over edges...");
        Iterator<Edge> edges = graph.edges();
        while (edges.hasNext()){
            Edge edge = edges.next();
            System.out.println(edge.outVertex().id() + " --> " + edge.label() + "--> " + edge.inVertex().id());
            Iterator<Property<Object>> propties = edge.properties();
            while(propties.hasNext()){
                Property prop = propties.next();
                System.out.println("\t" + prop.key() + ": " + prop.value());
            }
           
        }
        
        System.out.println("\n\nRemoving edge...");
        matthewHeight.remove();
        // now check to see if the edge has been removed
        edges = graph.edges();
        while (edges.hasNext()){
            Edge edge = edges.next();
            System.out.println(edge.outVertex().id() + " --> " + edge.label() + "--> " + edge.inVertex().id());
            Iterator<Property<Object>> propties = edge.properties();
            while(propties.hasNext()){
                Property prop = propties.next();
                System.out.println("\t" + prop.key() + ": " + prop.value());
            }
        }
        
        // now remove matthew, verify that there are no edges anymore
        System.out.println("\n\nRemoving vertex...");
        matthew.remove();
        edges = graph.edges();
        while (edges.hasNext()){
            Edge edge = edges.next();
            System.out.println(edge.outVertex().id() + " --> " + edge.label() + "--> " + edge.inVertex().id());
            Iterator<Property<Object>> propties = edge.properties();
            while(propties.hasNext()){
                Property prop = propties.next();
                System.out.println("\t" + prop.key() + ": " + prop.value());
            }
        }
    }
}
