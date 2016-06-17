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

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Arrays;

import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.security.Authorizations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;

import mvm.rya.shell.command.RyaCommands;
import mvm.rya.shell.util.ConnectorFactory;
import mvm.rya.shell.util.PasswordPrompt;

/**
 * Spring Shell commands that manage the connection that is used by the shell.
 */
@Component
public class RyaConnectionCommands implements CommandMarker {

    // Command line commands.
    public static final String PRINT_CONNECTION_DETAILS_CMD = "print-connection-details";
    public static final String CONNECT_ACCUMULO_CMD = "connect-accumulo";
    public static final String DISCONNECT_COMMAND_NAME_CMD = "disconnect";

    private Optional<AccumuloConnectionDetails> accumuloDetails = Optional.absent();

    private final SharedShellState sharedState;
    private final PasswordPrompt passwordPrompt;

    /**
     * Constructs an instance of {@link RyaConnectionCommands}.
     *
     * @param state - Holds shared state between all of the command classes. (not null)
     * @param passwordPrompt - Prompts the user for their password when connecting to a Rya store. (not null)
     */
    @Autowired
    public RyaConnectionCommands(final SharedShellState state, final PasswordPrompt passwordPrompt) {
        this.sharedState = requireNonNull( state );
        this.passwordPrompt = requireNonNull(passwordPrompt);
    }

    @CliAvailabilityIndicator({PRINT_CONNECTION_DETAILS_CMD})
    public boolean isPrintConnectionDetailsAvailable() {
        return true;
    }

    @CliAvailabilityIndicator({CONNECT_ACCUMULO_CMD})
    public boolean areConnectCommandsAvailable() {
        return !sharedState.isConnected();
    }

    @CliAvailabilityIndicator({DISCONNECT_COMMAND_NAME_CMD})
    public boolean isDisconnectAvailable() {
        return sharedState.isConnected();
    }

    @CliCommand(value = PRINT_CONNECTION_DETAILS_CMD, help = "Print information about the Shell's Rya storage connection.")
    public String printConnectionDetails() {
        String report = null;

        if(accumuloDetails.isPresent()) {
            final AccumuloConnectionDetails details = accumuloDetails.get();
            report = "The shell is connected to an instance of Accumulo using the following parameters:\n" +
                    "    Username: " + details.getUsername() + "\n" +
                    "    Instance Name: " + details.getInstanceName() + "\n" +
                    "    Zookeepers: " + details.getZookeepers() + "\n" +
                    "    Authorizations: " + details.getAuthorizations();
        } else {
            report = "The shell is not connected to anything.";
        }

        return report;
    }

    @CliCommand(value = CONNECT_ACCUMULO_CMD, help = "Connect the shell to an instance of Accumulo.")
    public String connectToAccumulo(
            @CliOption(key = {"username"}, mandatory = true, help = "The username that will be used to connect to Accummulo.")
            final String username,
            @CliOption(key = {"instanceName"}, mandatory = true, help = "The name of the Accumulo instance that will be connected to.")
            final String instanceName,
            @CliOption(key = {"zookeepers"}, mandatory = true, help = "A comma delimited list of zookeeper server hostnames.")
            final String zookeepers,
            @CliOption(key = {"auths"}, mandatory = false, help = "The Accumulo authorizations that will be used by the shell.")
            final String authString
            ) throws AccumuloException, AccumuloSecurityException {

        try {
            // Prompt the user for their password.
            final char[] password = passwordPrompt.getPassword();

            // Connect to Accumulo.
            final Connector connector = new ConnectorFactory().connect(username, CharBuffer.wrap(password), instanceName, zookeepers);

            // Clear the password.
            Arrays.fill(password, '\u0000');

            // Build the RyaCommands that will be used by the shell and put them in the shared state.
            Authorizations auths;
            if(authString == null || authString.isEmpty()) {
                auths = new Authorizations();
            } else {
                auths = new Authorizations( authString.split(",") );
            }

            final RyaCommands commands = RyaCommands.buildAccumuloCommands(connector, auths);
            sharedState.setConnectedCommands( commands );

            // Store the connection information so that it may be reported back.
            accumuloDetails = Optional.of( new AccumuloConnectionDetails(username, instanceName, zookeepers, auths.toString()) );

        } catch (AccumuloException | AccumuloSecurityException | IOException e) {
            // TODO Should probably log this info as well so that you can see the whole stack trace.
            return e.getMessage();
        }

        return null;
    }

    @CliCommand(value = DISCONNECT_COMMAND_NAME_CMD, help = "Disconnect the shell from the Rya storage it is connect to.")
    public void disconnect() {
        sharedState.clearConnectedCommands();
        accumuloDetails = Optional.absent();
    }

    /**
     * The information that the shell used to connect to Accumulo.
     */
    @ParametersAreNonnullByDefault
    private static final class AccumuloConnectionDetails {
        private final String username;
        private final String instanceName;
        private final String zookeepers;
        private final String authorizations;

        /**
         * Constructs an instance of {@link AccumuloConnectionDetails}.
         *
         * @param username - The username that was used to establish the connection. (not null)
         * @param instanceName - The Accumulo instance name that was used to establish the connection. (not null)
         * @param zookeepers - The list of zookeeper hostname that were used to establish the connection. (not null)
         * @param authorizations - The authorizations that will be used by the shell when issuing commands. (not null)
         */
        public AccumuloConnectionDetails(
                final String username,
                final String instanceName,
                final String zookeepers,
                final String authorizations) {
            this.username = requireNonNull(username);
            this.instanceName = requireNonNull(instanceName);
            this.zookeepers = requireNonNull(zookeepers);
            this.authorizations = requireNonNull(authorizations);
        }

        /**
         * @return The username that was used to establish the connection.
         */
        public String getUsername() {
            return username;
        }

        /**
         * @return The Accumulo instance name that was used to establish the connection.
         */
        public String getInstanceName() {
            return instanceName;
        }

        /**
         * @return The list of zookeeper hostname that were used to establish the connection.
         */
        public String getZookeepers() {
            return zookeepers;
        }

        /**
         * @return The authorizations that will be used by the shell when issuing commands.
         */
        public String getAuthorizations() {
            return authorizations;
        }
    }
}