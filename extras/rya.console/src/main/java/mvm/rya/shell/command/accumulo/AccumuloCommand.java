package mvm.rya.shell.command.accumulo;

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

import static java.util.Objects.requireNonNull;

import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.security.Authorizations;

/**
 * TODO doc
 */
@ParametersAreNonnullByDefault
public abstract class AccumuloCommand {

    private final Connector connector;
    private final Authorizations auths;

    /**
     * TODO doc
     *
     * @param connector
     * @param auths
     */
    public AccumuloCommand(final Connector connector, final Authorizations auths) {
        this.connector = requireNonNull(connector);
        this.auths = requireNonNull(auths);
    }

    public Connector getConnector() {
        return connector;
    }

    public Authorizations getAuths() {
        return auths;
    }
}