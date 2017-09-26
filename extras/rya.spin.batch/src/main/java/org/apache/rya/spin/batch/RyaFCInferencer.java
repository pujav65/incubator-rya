package org.apache.rya.spin.batch;

import org.openrdf.model.Statement;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.sail.memory.MemoryStore;

public class RyaFCInferencer {
	
	public class RyaInferenceException extends Exception {

		public RyaInferenceException(Exception ex) {
			super(ex);
		}

		
		
	}

	private SailRepositoryConnection conn;
	
	public RyaFCInferencer(SailRepositoryConnection conn) throws RepositoryException{
		this.conn = conn;		
	}

	public boolean executeConstructQuery(String query) throws RyaInferenceException {
	
		try {
	        GraphQuery graphQuery= conn.prepareGraphQuery(QueryLanguage.SPARQL, query);
	        // add the constructed statements
	        GraphQueryResult result = graphQuery.evaluate();
	        while (result.hasNext()){
	        	Statement statement = result.next();
	        	conn.add(statement);
	        }
	        return true;
		}
		catch (Exception ex){
			throw new RyaInferenceException(ex);
		}

	}
	
	
}
