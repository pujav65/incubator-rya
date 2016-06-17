/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package mvm.rya.shell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

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
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.Bootstrap;
import org.springframework.shell.core.CommandResult;
import org.springframework.shell.core.JLineShellComponent;

import mvm.rya.shell.util.PasswordPrompt;

/**
 * Integration tests the methods of {@link RyaConnectionCommands}.
 */
public class RyaConnectionCommandsIT {

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

    @Test
    public void connectAccumulo() throws IOException {
        // Mock the user entering the correct password.
        final ApplicationContext context = bootstrap.getApplicationContext();
        final PasswordPrompt mockPrompt = context.getBean( PasswordPrompt.class );
        when(mockPrompt.getPassword()).thenReturn("password".toCharArray());

        // Execute the connect command.
        final String cmd =
                RyaConnectionCommands.CONNECT_ACCUMULO_CMD + " " +
                        "--username root " +
                        "--instanceName " + cluster.getInstanceName() + " "+
                        "--zookeepers " + cluster.getZooKeepers() + " " +
                        "--auths u";

        final CommandResult connectResult = shell.executeCommand(cmd);

        // Ensure the connection was successful.
        assertTrue( connectResult.isSuccess() );
    }

    @Test
    public void connectAccumulo_noAuths() throws IOException {
        // Mock the user entering the correct password.
        final ApplicationContext context = bootstrap.getApplicationContext();
        final PasswordPrompt mockPrompt = context.getBean( PasswordPrompt.class );
        when(mockPrompt.getPassword()).thenReturn("password".toCharArray());

        // Execute the command
        final String cmd =
                RyaConnectionCommands.CONNECT_ACCUMULO_CMD + " " +
                        "--username root " +
                        "--instanceName " + cluster.getInstanceName() + " "+
                        "--zookeepers " + cluster.getZooKeepers();

        final CommandResult connectResult = shell.executeCommand(cmd);

        // Ensure the connection was successful.
        assertTrue( connectResult.isSuccess() );
    }

    @Test
    public void connectAccumulo_wrongCredentials() throws IOException {
        // Mock the user entering the wrong password.
        final ApplicationContext context = bootstrap.getApplicationContext();
        final PasswordPrompt mockPrompt = context.getBean( PasswordPrompt.class );
        when(mockPrompt.getPassword()).thenReturn("asjifo[ijwa".toCharArray());

        // Execute the command
        final String cmd =
                RyaConnectionCommands.CONNECT_ACCUMULO_CMD + " " +
                        "--username root " +
                        "--instanceName " + cluster.getInstanceName() + " "+
                        "--zookeepers " + cluster.getZooKeepers() + " " +
                        "--auths u";

        final CommandResult connectResult = shell.executeCommand(cmd);

        // Ensure the command didn't throw an exception, so it's marked as successful.
        // We print a message indicating what went wrong to the console.
        assertTrue( connectResult.isSuccess() );
    }

    @Test
    public void printConnectionDetails_notConnected() {
        // Run the print connection details command.
        final CommandResult printResult = shell.executeCommand( RyaConnectionCommands.PRINT_CONNECTION_DETAILS_CMD );
        final String msg = (String) printResult.getResult();

        final String expected = "The shell is not connected to anything.";
        assertEquals(expected, msg);
    }

    @Test
    public void printConnectionDetails_connectedToAccumulo() throws IOException {
        // Mock the user entering the correct password.
        final ApplicationContext context = bootstrap.getApplicationContext();
        final PasswordPrompt mockPrompt = context.getBean( PasswordPrompt.class );
        when(mockPrompt.getPassword()).thenReturn("password".toCharArray());

        // Connect to the mini accumulo instance.
        final String cmd =
                RyaConnectionCommands.CONNECT_ACCUMULO_CMD + " " +
                        "--username root " +
                        "--instanceName " + cluster.getInstanceName() + " "+
                        "--zookeepers " + cluster.getZooKeepers() + " " +
                        "--auths u";
        shell.executeCommand(cmd);

        // Run the print connection details command.
        final CommandResult printResult = shell.executeCommand( RyaConnectionCommands.PRINT_CONNECTION_DETAILS_CMD );
        final String msg = (String) printResult.getResult();

        final String expected =
                "The shell is connected to an instance of Accumulo using the following parameters:\n" +
                "    Username: root\n" +
                "    Instance Name: " + cluster.getInstanceName() + "\n" +
                "    Zookeepers: " + cluster.getZooKeepers() + "\n" +
                "    Authorizations: u";
        assertEquals(expected, msg);
    }

    @Test
    public void disconnect() throws IOException {
        // Mock the user entering the correct password.
        final ApplicationContext context = bootstrap.getApplicationContext();
        final PasswordPrompt mockPrompt = context.getBean( PasswordPrompt.class );
        when(mockPrompt.getPassword()).thenReturn("password".toCharArray());

        // Connect to the mini accumulo instance.
        final String cmd =
                RyaConnectionCommands.CONNECT_ACCUMULO_CMD + " " +
                        "--username root " +
                        "--instanceName " + cluster.getInstanceName() + " "+
                        "--zookeepers " + cluster.getZooKeepers() + " " +
                        "--auths u";
        shell.executeCommand(cmd);

        // Disconnect from it.
        final CommandResult disconnectResult = shell.executeCommand( RyaConnectionCommands.DISCONNECT_COMMAND_NAME_CMD );
        assertTrue( disconnectResult.isSuccess() );
    }
}