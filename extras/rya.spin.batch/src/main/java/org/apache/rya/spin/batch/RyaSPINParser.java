package org.apache.rya.spin.batch;

import java.io.InputStream;

import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

public class RyaSPINParser {

	public static TupleQueryResult getSPINConstructQueries(InputStream rdfStream, RDFFormat format) throws Exception {
		Repository repo = new SailRepository(new MemoryStore());
		repo.initialize();
		
		repo.getConnection().add(rdfStream, null, format);
		
		// now query for the query text
		String queryString = "SELECT ?ruleText WHERE {"
				+ " ?ruleVar <http://www.w3.org/1999/02/22-rdf-syntax-ns#> <http://spinrdf.org/sp#Construct>."
				+ " ?ruleVar <http://spinrdf.org/sp#text> ?ruleText."
				+ "}";
		TupleQuery tupleQuery = repo.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		return tupleQuery.evaluate();
	}
}
