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
package org.apache.rya.api.client;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import net.jcip.annotations.Immutable;

/**
 * Provides access to a set of Rya functions.
 */
@Immutable
@DefaultAnnotation(NonNull.class)
public class RyaClient {
    // Administrative functions.
    private final Install install;
    private final CreatePCJ createPcj;
    private final DeletePCJ deletePcj;
    private final BatchUpdatePCJ bactchUpdatePCJ;
    private final GetInstanceDetails getInstanceDetails;
    private final InstanceExists instanceExists;
    private final ListInstances listInstances;
    private final AddUser addUser;
    private final RemoveUser removeUser;

    /**
     * Constructs an instance of {@link RyaClient}.
     */
    public RyaClient(
            final Install install,
            final CreatePCJ createPcj,
            final DeletePCJ deletePcj,
            final BatchUpdatePCJ batchUpdatePcj,
            final GetInstanceDetails getInstanceDetails,
            final InstanceExists instanceExists,
            final ListInstances listInstances,
            final AddUser addUser,
            final RemoveUser removeUser) {
        this.install = requireNonNull(install);
        this.createPcj = requireNonNull(createPcj);
        this.deletePcj = requireNonNull(deletePcj);
        bactchUpdatePCJ = requireNonNull(batchUpdatePcj);
        this.getInstanceDetails = requireNonNull(getInstanceDetails);
        this.instanceExists = requireNonNull(instanceExists);
        this.listInstances = requireNonNull(listInstances);
        this.addUser = requireNonNull(addUser);
        this.removeUser = requireNonNull(removeUser);
    }

    /**
     * @return An instance of {@link Install} that is connected to a Rya storage.
     */
    public Install getInstall() {
        return install;
    }

    /**
     * @return An instance of {@link CreatePCJ} that is connected to a Rya storage
     *   if the Rya instance supports PCJ indexing.
     */
    public CreatePCJ getCreatePCJ() {
        return createPcj;
    }

    /**
     * @return An instance of {@link DeletePCJ} that is connected to a Rya storage
     *   if the Rya instance supports PCJ indexing.
     */
    public DeletePCJ getDeletePCJ() {
        return deletePcj;
    }

    /**
     * @return An instance of {@link BatchUpdatePCJ} that is connect to a Rya storage
     *   if the Rya instance supports PCJ indexing.
     */
    public BatchUpdatePCJ getBatchUpdatePCJ() {
        return bactchUpdatePCJ;
    }

    /**
     * @return An instance of {@link GetInstanceDetails} that is connected to a Rya storage.
     */
    public GetInstanceDetails getGetInstanceDetails() {
        return getInstanceDetails;
    }

    /**
     * @return An instance of {@link ListInstances} that is connected to a Rya storage.
     */
    public ListInstances getListInstances() {
        return listInstances;
    }

    /**
     * @return An instance of {@link InstanceExists} that is connected to a Rya storage.
     */
    public InstanceExists getInstanceExists() {
        return instanceExists;
    }

    /**
     * @return An instance of {@link AddUser} that is connected to a Rya storage.
     */
    public AddUser getAddUser() {
        return addUser;
    }

    /**
     * @return An instance of {@link DeleteUser} that is connected to a Rya storage.
     */
    public RemoveUser getRemoveUser() {
        return removeUser;
    }
}