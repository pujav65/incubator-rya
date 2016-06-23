package mvm.rya.shell;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.client.admin.TableOperations;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.apache.accumulo.minicluster.MiniAccumuloCluster;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import mvm.rya.shell.command.accumulo.AccumuloConnectionDetails;

/**
 * The base class for any integration test that needs to use a {@link MiniAccumuloCluster}.
 */
public class AccumuloITBase {

    /**
     * A mini Accumulo cluster that can be used to test the commands against.
     */
    private static MiniAccumuloCluster cluster = null;

    /**
     * The tables that exist in a newly created cluster.
     */
    private static List<String> originalTableNames = new ArrayList<>();

    /**
     * Details about the values that were used to create the connector to the cluster.
     */
    private static AccumuloConnectionDetails connectionDetails = null;

    @BeforeClass
    public static void startMiniAccumulo() throws IOException, InterruptedException, AccumuloException, AccumuloSecurityException {
        // Setup the mini cluster.
        final File tempDirectory = Files.createTempDirectory("testDir").toFile();
        cluster = new MiniAccumuloCluster(tempDirectory, "password");
        cluster.start();

        // Store a list of the original table names.
        final String username = "root";
        final char[] password = "password".toCharArray();
        final String instanceName = cluster.getInstanceName();
        final String zookeepers = cluster.getZooKeepers();
        final String auths = "";
        connectionDetails = new AccumuloConnectionDetails(username, password, instanceName, zookeepers, auths);

        final Instance instance = new ZooKeeperInstance(instanceName, zookeepers);
        final Connector connector = instance.getConnector(username, new PasswordToken( new String(password) ));
        originalTableNames.addAll( connector.tableOperations().list() );
    }

    @Before
    public void clearLastTest() throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
        // Get a list of the tables that have been added by the test.
        final Instance instance = new ZooKeeperInstance(cluster.getInstanceName(), cluster.getZooKeepers());
        final Connector connector = instance.getConnector("root", new PasswordToken("password"));
        final TableOperations tableOps = connector.tableOperations();

        final List<String> newTables = new ArrayList<>();
        newTables.addAll( tableOps.list() );
        newTables.removeAll( originalTableNames );

        // Delete all the new tables.
        for(final String newTable : newTables) {
            tableOps.delete( newTable );
        }
    }

    @AfterClass
    public static void stopMiniAccumulo() throws IOException, InterruptedException {
        cluster.stop();
    }

    /**
     * @return A mini Accumulo cluster that can be used to test the commands against.
     */
    public MiniAccumuloCluster getTestCluster() {
        return cluster;
    }

    /**
     * @return An accumulo connector that is connected to the mini cluster.
     */
    public Connector getConnector() throws AccumuloException, AccumuloSecurityException {
        return cluster.getConnector("root", "password");
    }

    /**
     * @return Details about the values that were used to create the connector to the cluster.
     */
    public AccumuloConnectionDetails getConnectionDetails() {
        return connectionDetails;
    }
}