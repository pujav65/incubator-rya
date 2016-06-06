package mvm.rya.shell.command.accumulo.administrative;

import javax.annotation.ParametersAreNonnullByDefault;

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

import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.security.Authorizations;

import mvm.rya.shell.command.accumulo.AccumuloCommand;
import mvm.rya.shell.command.administrative.Install;

/**
 * TODO doc
 */
@ParametersAreNonnullByDefault
public class AccumuloInstall extends AccumuloCommand implements Install {

    /**
     * TODO doc
     *
     * @param connector
     * @param auths
     */
    public AccumuloInstall(final Connector connector, final Authorizations auths) {
        super(connector, auths);
    }

    @Override
    public void install(final String instanceName, final InstallConfiguration installConfig) {
        // TODO Auto-generated method stub

    }
}