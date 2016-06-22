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
package mvm.rya.shell.command.accumulo.administrative;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.security.Authorizations;

import mvm.rya.shell.command.CommandException;
import mvm.rya.shell.command.accumulo.AccumuloCommand;
import mvm.rya.shell.command.administrative.ListInstances;

// TODO impl, test

/**
 * An Accumulo implementation of the {@link ListInstances} command.
 */
@ParametersAreNonnullByDefault
public class AccumuloListInstances extends AccumuloCommand implements ListInstances {

    /**
     * Constructs an instance of {@link AccumuloListInstances}.
     *
     * @param connector - Provides programatic access to the instance of Accumulo
     *   that hosts Rya instance. (not null)
     * @param auths - The authorizations that will be used when interacting with
     *   the instance of Accumulo. (not null)
     */
    public AccumuloListInstances(final Connector connector, final Authorizations auths) {
        super(connector, auths);
    }

    @Override
    public List<String> listInstances() throws CommandException {
        // TODO Auto-generated method stub

        // Iterate through all of the SPO tables that are in accumulo and return their prefixes.
        // If all of them have been installed correctly, we could iterate through the instance details tables.

        throw new UnsupportedOperationException("Not implemented yet.");
    }
}