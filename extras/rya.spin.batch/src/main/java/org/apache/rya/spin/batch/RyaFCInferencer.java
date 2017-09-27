package org.apache.rya.spin.batch;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepositoryConnection;

public class RyaFCInferencer {
	
	public class RyaInferenceException extends Exception {

		public RyaInferenceException(Exception ex) {
			super(ex);
		}

		
		
	}

	private SailRepositoryConnection conn;
	
	private List<String> sparqlGraphQueries = new ArrayList<String>();
	
	public RyaFCInferencer(SailRepositoryConnection conn) throws RepositoryException{
		this.conn = conn;		
	}
	
	public void registerConstructQuery(String query) throws RyaInferenceException {
		sparqlGraphQueries.add(query);
	}

	public boolean executeReasoning() throws RyaInferenceException {
	
		try {
			for (String query: sparqlGraphQueries){
		        GraphQuery graphQuery= conn.prepareGraphQuery(QueryLanguage.SPARQL, query);
		        // add the constructed statements
		        GraphQueryResult result = graphQuery.evaluate();
		        while (result.hasNext()){
		        	Statement statement = result.next();
		        	conn.add(statement);
		        }
			}
	        return true;
		}
		catch (Exception ex){
			throw new RyaInferenceException(ex);
		}

	}
	
	
}
