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

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.rya.indexing.accumulo.ConfigUtils;
import org.apache.rya.indexing.mongodb.MongoIndexingConfiguration;
import org.apache.rya.indexing.mongodb.MongoIndexingConfiguration.MongoDBIndexingConfigBuilder;
import org.apache.rya.mongodb.MockMongoFactory;
import org.apache.rya.mongodb.MongoConnectorFactory;
import org.apache.rya.sail.config.RyaSailFactory;
import org.apache.rya.spin.batch.RyaFCInferencer;
import org.apache.zookeeper.ClientCnxn;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.sail.Sail;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;


public class MongoRyaFCRuleExample {
    private static final Logger log = Logger.getLogger(MongoRyaFCRuleExample.class);

    private static final boolean IS_DETAILED_LOGGING_ENABLED = false;

    //
    // Connection configuration parameters
    //

    private static final boolean PRINT_QUERIES = true;
    private static final String MONGO_DB = "rya";
    private static final String MONGO_COLL_PREFIX = "rya_";
    private static final boolean USE_MOCK = true;
    private static final boolean USE_INFER = true;
    private static final String MONGO_INSTANCE_URL = "localhost";
    private static final String MONGO_INSTANCE_PORT = "27017";

    public static void setupLogging() {
        final Logger rootLogger = LogManager.getRootLogger();
        rootLogger.setLevel(Level.OFF);
        final ConsoleAppender ca = (ConsoleAppender) rootLogger.getAppender("stdout");
        ca.setLayout(new PatternLayout("%d{MMM dd yyyy HH:mm:ss} %5p [%t] (%F:%L) - %m%n"));
        rootLogger.setLevel(Level.INFO);
        // Filter out noisy messages from the following classes.
        Logger.getLogger(ClientCnxn.class).setLevel(Level.OFF);
        Logger.getLogger(MockMongoFactory.class).setLevel(Level.OFF);
    }

    public static void main(final String[] args) throws Exception {
        if (IS_DETAILED_LOGGING_ENABLED) {
            setupLogging();
        }
        final Configuration conf = getConf();
        conf.setBoolean(ConfigUtils.DISPLAY_QUERY_PLAN, PRINT_QUERIES);

        SailRepository repository = null;
        SailRepositoryConnection conn = null;
        try {
            log.info("Connecting to Indexing Sail Repository.");
            final Sail sail = RyaSailFactory.getInstance(conf);
            repository = new SailRepository(sail);
            conn = repository.getConnection();

            demonstrateOneRuleReasoning(conn);
            
            demonstrateTwoRuleReasoning(conn);
            
            
        } finally {
            log.info("Shutting down");
            closeQuietly(conn);
            closeQuietly(repository);
            MongoConnectorFactory.closeMongoClient();
        }
    }

    
    public static void demonstrateOneRuleReasoning(final SailRepositoryConnection conn) throws Exception {
        final long start = System.currentTimeMillis();
        log.info("Adding initial data...");
        addData(conn);
        testInference(conn);
        RyaFCInferencer inferencer = new RyaFCInferencer(conn);
        
        log.info("Executing construct query...");
        String query = "CONSTRUCT {?b <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://acme.com/avocadoLovers>.  } WHERE {?b <http://acme.com/actions/likes> \"Avocados\"}";
        inferencer.registerConstructQuery(query);
        
        inferencer.executeReasoning();
        testInference(conn);
        deleteData(conn);
        testInference(conn);
        log.info("TIME: " + (System.currentTimeMillis() - start) / 1000.);
    }

    
    public static void demonstrateTwoRuleReasoning(final SailRepositoryConnection conn) throws Exception {
        final long start = System.currentTimeMillis();
        log.info("Adding initial data...");
        addData(conn);
        testInference(conn);
        RyaFCInferencer inferencer = new RyaFCInferencer(conn);
        
        log.info("Executing construct query...");
        String query = "CONSTRUCT {?b <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://acme.com/avocadoLovers>.  } WHERE {?b <http://acme.com/actions/likes> \"Avocados\"}";
        String query2 = "CONSTRUCT {?b <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://acme.com/NutLovers>.  } WHERE {?b <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://acme.com/avocadoLovers> }";
        inferencer.registerConstructQuery(query);
        inferencer.registerConstructQuery(query2);
        
        inferencer.executeReasoning();
        testInference(conn);
        deleteData(conn);
        testInference(conn);
        log.info("TIME: " + (System.currentTimeMillis() - start) / 1000.);
    }

    private static void closeQuietly(final SailRepository repository) {
        if (repository != null) {
            try {
                repository.shutDown();
            } catch (final RepositoryException e) {
                // quietly absorb this exception
            }
        }
    }

    private static void closeQuietly(final SailRepositoryConnection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (final RepositoryException e) {
                // quietly absorb this exception
            }
        }
    }

 
    private static Configuration getConf() throws IOException {

        MongoDBIndexingConfigBuilder builder = MongoIndexingConfiguration.builder()
            .setUseMockMongo(USE_MOCK).setUseInference(USE_INFER).setAuths("U");

        if (USE_MOCK) {
            final MongoClient c = MockMongoFactory.newFactory().newMongoClient();
            final ServerAddress address = c.getAddress();
            final String url = address.getHost();
            final String port = Integer.toString(address.getPort());
            c.close();
            builder.setMongoHost(url).setMongoPort(port);
        } else {
            // User name and password must be filled in:
            builder = builder.setMongoUser("fill this in")
                             .setMongoPassword("fill this in")
                             .setMongoHost(MONGO_INSTANCE_URL)
                             .setMongoPort(MONGO_INSTANCE_PORT);
        }

        return builder.setMongoDBName(MONGO_DB)
               .setMongoCollectionPrefix(MONGO_COLL_PREFIX)
               .setUseMongoFreetextIndex(true)
               .setMongoFreeTextPredicates(RDFS.LABEL.stringValue()).build();

    }


    public static void addData(final SailRepositoryConnection conn) throws MalformedQueryException, RepositoryException,
    UpdateExecutionException, QueryEvaluationException, TupleQueryResultHandlerException {

        // Add data
        String query = "INSERT DATA\n"//
                + "{\n"//
                + "  <http://acme.com/people/Mike> " //
                + "       <http://acme.com/actions/likes> \"A new book\" ;\n"//
                + "       <http://acme.com/actions/likes> \"Avocados\" .\n" + "}";

        log.info("Performing Query");

        Update update = conn.prepareUpdate(QueryLanguage.SPARQL, query);
        update.execute();
        
    }

    public static void deleteData(final SailRepositoryConnection conn) throws MalformedQueryException, RepositoryException,
    UpdateExecutionException, QueryEvaluationException, TupleQueryResultHandlerException {

        // Add data
        String query = "DELETE WHERE \n"//
                + "{\n"//
                + "  <http://acme.com/people/Mike> " //
                + "       ?p ?o .}";

        log.info("Performing Query");

        Update update = conn.prepareUpdate(QueryLanguage.SPARQL, query);
        update.execute();
        
    }

    public static void testInference(final SailRepositoryConnection conn) throws MalformedQueryException, RepositoryException,
    UpdateExecutionException, QueryEvaluationException, TupleQueryResultHandlerException {
        String query = "select ?types {<http://acme.com/people/Mike> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?types . }";
        TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult queryResult = tupleQuery.evaluate();
        while (queryResult.hasNext()){
        	BindingSet bs = queryResult.next();
        	log.info("Returned result --" + bs);
        }
        
    }


 




    private static class CountingResultHandler implements TupleQueryResultHandler {
        private int count = 0;

        public int getCount() {
            return count;
        }

        public void resetCount() {
            count = 0;
        }

        @Override
        public void startQueryResult(final List<String> arg0) throws TupleQueryResultHandlerException {
        }

        @Override
        public void handleSolution(final BindingSet arg0) throws TupleQueryResultHandlerException {
            count++;
            System.out.println(arg0);
        }

        @Override
        public void endQueryResult() throws TupleQueryResultHandlerException {
        }

        @Override
        public void handleBoolean(final boolean arg0) throws QueryResultHandlerException {
        }

        @Override
        public void handleLinks(final List<String> arg0) throws QueryResultHandlerException {
        }
    }
}
