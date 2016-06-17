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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import mvm.rya.shell.command.RyaCommands;

// TODO test

/**
 * Holds values that are shared between the various Rya command classes.
 */
@ParametersAreNonnullByDefault
public class SharedShellState {

    private final AtomicReference<RyaCommands> connectedCommands = new AtomicReference<>();

    /**
     * Checks if the shell is connected to a Rya storage.
     *
     * @return {@code true} if the shell has a set of commands it may use; otherwise {@code false}.
     */
    public boolean isConnected() {
        return connectedCommands.get() != null;
    }

    /**
     * Set the {@link RyaCommands} to use when a command on the shell is issued.
     * To clear the commands, you may provide this method with {@code null} or
     * call the {@link #clearConnectedCommands()} method instead.
     *
     * @param commands - The {@link RyaCommands} to use when a command on the shell is issued.
     */
    public void setConnectedCommands(@Nullable final RyaCommands commands) {
        connectedCommands.set( commands );
    }

    /**
     * Clears the connected commands so that they will not be used by commands
     * that are issued after this point.
     */
    public void clearConnectedCommands() {
        setConnectedCommands(null);
    }

    /**
     * @return The {@link RyaCommands} to use when a command on the shell is issued.
     */
    public Optional<RyaCommands> getConnectedCommands() {
        return Optional.ofNullable( connectedCommands.get() );
    }
}