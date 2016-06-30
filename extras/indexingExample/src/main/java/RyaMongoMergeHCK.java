

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.calrissian.mango.collect.CloseableIterable;
import org.openrdf.model.Statement;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import mvm.rya.accumulo.AccumuloRdfConfiguration;
import mvm.rya.accumulo.AccumuloRyaDAO;
import mvm.rya.api.RdfCloudTripleStoreConfiguration;
import mvm.rya.api.domain.RyaStatement;
import mvm.rya.api.domain.RyaURI;
import mvm.rya.api.persist.RyaDAOException;
import mvm.rya.api.persist.query.RyaQuery;
import mvm.rya.api.resolver.RdfToRyaConversions;
import mvm.rya.indexing.accumulo.ConfigUtils;
import mvm.rya.mongodb.MongoDBRdfConfiguration;
import mvm.rya.mongodb.MongoDBRyaDAO;

public class RyaMongoMergeHCK {
	
	public final static boolean useMock = true;
	public final static boolean useMockMongo = false;
	public final static String ACC_USER = "root";
	public final static String ACC_PWD = "";
	public final static String ACC_INSTANCE = "test";
	public final static String ACC_ZOO = "zoo1";
	public final static String ACC_TABLE_PREFIX = "rya_";
	private static final String MONGO_DB = "rya";
	private static final String MONGO_COLL_PREFIX = "rya_";
	private static final String MONGO_INSTANCE = "apts16-mongo.sil.arl.psu.edu";
	private static final String MONGO_INSTANCE_PORT = "27017";
	private static final String MONGO_USER = "";
	private static final String MONGO_USER_PASSWORD = "";
	

	public static void main(String[] args) throws Exception {
		if (args.length != 3){
			System.err.println("Usage: fileToUpload namedGraph queryFile");
		}
//		File accumuloConfig = new File(args[0]);
//		File mongoConfigFile = new File(args[1]);
		File inputFile = new File(args[0]);
		String namedGraph = args[1];
		File queryFile = new File(args[2]);
		String sparqlQuery = IOUtils.toString(new FileInputStream(queryFile));
        final SPARQLParser parser = new SPARQLParser();
        final ParsedQuery parsedQuery = parser.parseQuery(sparqlQuery, null);
        StatementPatternCollector collector = new StatementPatternCollector();
        parsedQuery.getTupleExpr().visit(collector);

        List<StatementPattern> patterns = collector.getStatementPatterns();
        
		Configuration config = getConfigFromFile(false);
		AccumuloRdfConfiguration rdfConfig = new AccumuloRdfConfiguration(config);
		rdfConfig.setBoolean(ConfigUtils.DISPLAY_QUERY_PLAN, true);
		
		Configuration mongoConfig = getConfigFromFile(true);
		MongoDBRdfConfiguration mongoRyaConfig = new MongoDBRdfConfiguration(config);
		mongoRyaConfig.setBoolean(ConfigUtils.DISPLAY_QUERY_PLAN, true);
		
		// get the Accumulo Rya instance
		System.out.println("UPLOADING DATA TO ACCUMULO INSTANCE....");
		AccumuloRyaDAO accumuloDAO = getAccumuloDAO(rdfConfig);
		
		// load in the data
		addData(inputFile, accumuloDAO);
		
		// get the Mongo Rya instance
		MongoDBRyaDAO mongoDAO = getMongoDAO(mongoRyaConfig);
		
		// scan the RyaStatements in the accumulo instance
		System.out.println("BEGINNING CLONE OF ACCUMULO INSTANCE....");
		RyaQuery query = new RyaQuery(new RyaStatement());
		RyaURI context  = new RyaURI(namedGraph);
		CloseableIterable<RyaStatement> statementIterable = accumuloDAO.getQueryEngine().query(query);
		Iterator<RyaStatement> statementIter = statementIterable.iterator();
		while (statementIter.hasNext()){
			RyaStatement ryaStatement = statementIter.next();
			if (matchesQuery(ryaStatement, patterns)){
				ryaStatement.setContext(context);
				try {
					mongoDAO.add(ryaStatement);
					
				}
				catch (Exception ex){
					System.err.println("Error adding!" + ryaStatement);
				}
			}
		}
		
		
		// 
		System.out.println("SUCCESS!");
	}
	
	private static boolean matchesQuery(RyaStatement ryaStatement, List<StatementPattern> patterns) {
		// check if the statement matches the statement pattern
		for (StatementPattern pattern : patterns){
			boolean matchesSubj = true;
			boolean matchesPred = true;
			boolean matchesObj = true;
			if (pattern.getSubjectVar().isConstant()){
				RyaURI uri = new RyaURI(pattern.getSubjectVar().getValue().stringValue());
				if (!ryaStatement.getSubject().equals(uri)){
					matchesSubj = false;
				}
			}
			if (pattern.getPredicateVar().isConstant()){
				RyaURI uri = new RyaURI(pattern.getPredicateVar().getValue().stringValue());
				if (!ryaStatement.getPredicate().equals(uri)){
					matchesPred = false;
				}
			}
			if (pattern.getObjectVar().isConstant()){
				String objValue = pattern.getObjectVar().getValue().stringValue();
				if (!ryaStatement.getObject().getData().equalsIgnoreCase(objValue)){
					matchesObj = false;
				}
			}
			if (matchesSubj && matchesPred && matchesObj) return true;
		}
		
		
		// see if the statement matches any of the statement patterns
		return false;
	}

	private static void addData(File statementsFile, AccumuloRyaDAO dao) throws RDFParseException, RDFHandlerException, IOException, RyaDAOException {
        RDFFormat format = Rio.getParserFormatForFileName(statementsFile.getName(), RDFFormat.NTRIPLES);
        RDFParser parser = Rio.createParser(format);
        InputStream in = new FileInputStream(statementsFile);
        StatementHandler handler = new StatementHandler();
        parser.setRDFHandler(handler);
        parser.parse(in, "");
        Set<Statement> statements = handler.getStatements();
        Set<RyaStatement> ryaStatements = new HashSet<RyaStatement>();
        for (Statement statement : statements){
			System.err.println("Adding:" + statement);
        	// TODO not adding visibilities
        	ryaStatements.add(RdfToRyaConversions.convertStatement(statement));
        }
        dao.add(ryaStatements.iterator());
        dao.flush();
	}

	private static AccumuloRyaDAO getAccumuloDAO(AccumuloRdfConfiguration accumuloConfig) throws RyaDAOException, AccumuloException, AccumuloSecurityException {
		Connector conn = ConfigUtils.getConnector(accumuloConfig);
		AccumuloRyaDAO accumuloDAO = new AccumuloRyaDAO();
		accumuloDAO.setConf(accumuloConfig);
		accumuloDAO.setConnector(conn);
		accumuloDAO.init();
		return accumuloDAO;
	}

	private static MongoDBRyaDAO getMongoDAO(MongoDBRdfConfiguration mongoConfig) throws RyaDAOException, NumberFormatException, IOException {
		MongoClient client;
    	if (mongoConfig.getBoolean(MongoDBRdfConfiguration.USE_TEST_MONGO, false)){
    		MongodForTestsFactory tests = new MongodForTestsFactory();
			client = tests.newMongo();
    	}
    	else {
            final String host = mongoConfig.get(MongoDBRdfConfiguration.MONGO_INSTANCE);
            final int port = Integer.parseInt(mongoConfig.get(MongoDBRdfConfiguration.MONGO_INSTANCE_PORT));
            final ServerAddress server = new ServerAddress(host, port);
            //check for authentication credentials
            if (mongoConfig.get(MongoDBRdfConfiguration.MONGO_USER) != null) {
                final String username = mongoConfig.get(MongoDBRdfConfiguration.MONGO_USER);
                final String dbName = mongoConfig.get(MongoDBRdfConfiguration.MONGO_DB_NAME);
                final char[] pswd = mongoConfig.get(MongoDBRdfConfiguration.MONGO_USER_PASSWORD).toCharArray();
                final MongoCredential cred = MongoCredential.createCredential(username, dbName, pswd);
                client = new MongoClient(server, Arrays.asList(cred));
            } else {
            	client = new MongoClient(server);
            }
  		
    	}

		MongoDBRyaDAO mongoDAO = new MongoDBRyaDAO(mongoConfig, client);
		mongoDAO.setConf(mongoConfig);
		mongoDAO.init();
		return mongoDAO;
	}

	private static Configuration getConfigFromFile(boolean useMongo) throws FileNotFoundException{
		Configuration config = new Configuration();
		config.set(RdfCloudTripleStoreConfiguration.CONF_TBL_PREFIX,
				ACC_TABLE_PREFIX);
		config.set(ConfigUtils.CLOUDBASE_USER, ACC_USER);
		config.set(ConfigUtils.CLOUDBASE_PASSWORD, ACC_PWD);
		config.set(ConfigUtils.CLOUDBASE_INSTANCE, ACC_INSTANCE);
		config.set(ConfigUtils.CLOUDBASE_ZOOKEEPERS, ACC_ZOO);
		config.setBoolean(ConfigUtils.USE_MOCK_INSTANCE, useMock);
		config.setBoolean(ConfigUtils.USE_MONGO, useMongo);
		config.setBoolean(MongoDBRdfConfiguration.USE_TEST_MONGO, useMockMongo);
		config.set(MongoDBRdfConfiguration.MONGO_DB_NAME, MONGO_DB);
		config.set(MongoDBRdfConfiguration.MONGO_INSTANCE, MONGO_INSTANCE);
		config.set(MongoDBRdfConfiguration.MONGO_INSTANCE_PORT, MONGO_INSTANCE_PORT);
		if (!MONGO_USER.isEmpty()){
			config.set(MongoDBRdfConfiguration.MONGO_USER, MONGO_USER);
			config.set(MongoDBRdfConfiguration.MONGO_USER_PASSWORD, MONGO_USER_PASSWORD);
		}
		config.set(MongoDBRdfConfiguration.MONGO_COLLECTION_PREFIX, MONGO_COLL_PREFIX);
		return config;
	}

}
