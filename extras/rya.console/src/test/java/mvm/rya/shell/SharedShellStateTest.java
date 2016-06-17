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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import mvm.rya.shell.SharedShellState;
import mvm.rya.shell.command.RyaCommands;

/**
 * Tests the methods of {@link SharedShellState}.
 */
public class SharedShellStateTest {

    @Test
    public void isConnected_true() {
        final SharedShellState state = new SharedShellState();

        final RyaCommands commands = mock(RyaCommands.class);
        state.setConnectedCommands( commands );

        assertTrue( state.isConnected() );
    }

    @Test
    public void isConnected_false() {
        final SharedShellState state = new SharedShellState();
        assertFalse( state.isConnected() );
    }

    @Test
    public void setConnectedCommands() {
        final SharedShellState state = new SharedShellState();

        final RyaCommands commands = mock(RyaCommands.class);
        state.setConnectedCommands( commands );

        assertTrue( state.getConnectedCommands().isPresent() );
    }

    @Test
    public void clearConnectedCommands() {
        final SharedShellState state = new SharedShellState();

        final RyaCommands commands = mock(RyaCommands.class);
        state.setConnectedCommands( commands );
        state.clearConnectedCommands();

        assertFalse( state.getConnectedCommands().isPresent() );
    }
}