package mvm.rya.shell;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.minicluster.MiniAccumuloCluster;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.shell.Bootstrap;
import org.springframework.shell.core.JLineShellComponent;

/**
 * All Rya Shell integration tests should extend this one. It provides startup
 * and shutdown hooks for a Mini Accumulo Cluster when you start and stop testing.
 * It also creates a new shell to test with between each test.
 */
public class RyaShellITBase {

    /**
     * A mini Accumulo cluster that can be used to test the commands against.
     */
    private static MiniAccumuloCluster cluster = null;

    @BeforeClass
    public static void startMiniAccumulo() throws IOException, InterruptedException, AccumuloException, AccumuloSecurityException {
        // Setup the mini cluster.
        final File tempDirectory = Files.createTempDirectory("testDir").toFile();
        cluster = new MiniAccumuloCluster(tempDirectory, "password");
        cluster.start();
    }

    @AfterClass
    public static void stopMiniAccumulo() throws IOException, InterruptedException {
        cluster.stop();
    }

    /**
     * The bootstrap that was used to initialize the Shell that will be tested.
     */
    private Bootstrap bootstrap;

    /**
     * The shell that will be tested.
     */
    private JLineShellComponent shell;

    @Before
    public void startShell() {
        // Bootstrap the shell with the test bean configuration.
        bootstrap = new Bootstrap(new String[]{}, new String[]{"file:src/test/resources/RyaShellTest-context.xml"});
        shell = bootstrap.getJLineShellComponent();
    }

    @After
    public void stopShell() {
        shell.stop();
    }

    /**
     * @return A mini Accumulo cluster that can be used to test the commands against.
     */
    public MiniAccumuloCluster getTestCluster() {
        return cluster;
    }

    /**
     * @return The bootstrap that was used to initialize the Shell that will be tested.
     */
    public Bootstrap getTestBootstrap() {
        return bootstrap;
    }

    /**
     * @return The shell that will be tested.
     */
    public JLineShellComponent getTestShell() {
        return shell;
    }
}