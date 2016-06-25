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

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.security.Authorizations;
import org.junit.Test;

import com.google.common.base.Optional;

import mvm.rya.accumulo.instance.AccumuloRyaInstanceDetailsRepository;
import mvm.rya.api.instance.RyaDetails;
import mvm.rya.api.instance.RyaDetails.EntityCentricIndexDetails;
import mvm.rya.api.instance.RyaDetails.FreeTextIndexDetails;
import mvm.rya.api.instance.RyaDetails.GeoIndexDetails;
import mvm.rya.api.instance.RyaDetails.JoinSelectivityDetails;
import mvm.rya.api.instance.RyaDetails.PCJIndexDetails;
import mvm.rya.api.instance.RyaDetails.PCJIndexDetails.FluoDetails;
import mvm.rya.api.instance.RyaDetails.ProspectorDetails;
import mvm.rya.api.instance.RyaDetails.TemporalIndexDetails;
import mvm.rya.api.instance.RyaDetailsRepository;
import mvm.rya.api.instance.RyaDetailsRepository.NotInitializedException;
import mvm.rya.api.instance.RyaDetailsRepository.RyaDetailsRepositoryException;
import mvm.rya.shell.AccumuloITBase;
import mvm.rya.shell.command.CommandException;
import mvm.rya.shell.command.administrative.Install;
import mvm.rya.shell.command.administrative.Install.DuplicateInstanceNameException;
import mvm.rya.shell.command.administrative.Install.InstallConfiguration;
import mvm.rya.shell.command.administrative.InstanceExists;

/**
 * Integration tests the methods of {@link AccumuloInstall}.
 */
public class AccumuloInstallIT extends AccumuloITBase {

    @Test
    public void install() throws AccumuloException, AccumuloSecurityException, DuplicateInstanceNameException, CommandException, NotInitializedException, RyaDetailsRepositoryException {
        // Install an instance of Rya.
        final String instanceName = "testInstance_";
        final InstallConfiguration installConfig = InstallConfiguration.builder()
                .setEnableTableHashPrefix(true)
                .setEnableEntityCentricIndex(true)
                .setEnableFreeTextIndex(true)
                .setEnableTemporalIndex(true)
                .setEnablePcjIndex(true)
                .setEnableGeoIndex(true)
                .setFluoPcjAppName("fluo_app_name")
                .build();

        final Install install = new AccumuloInstall(getConnectionDetails(), getConnector(), new Authorizations());
        install.install(instanceName, installConfig);

        // Check that the instance exists.
        final InstanceExists instanceExists = new AccumuloInstanceExists(getConnectionDetails(), getConnector(), new Authorizations());
        instanceExists.exists(instanceName);

        // Verify the correct details were persisted.
        final RyaDetailsRepository detailsRepo = new AccumuloRyaInstanceDetailsRepository(getConnector(), instanceName);
        final RyaDetails details = detailsRepo.getRyaInstanceDetails();

        final RyaDetails expectedDetails = RyaDetails.builder()
                .setRyaInstanceName(instanceName)

                // The version depends on how the test is packaged, so just grab whatever was stored.
                .setRyaVersion( details.getRyaVersion() )

                .setGeoIndexDetails( new GeoIndexDetails(true) )
                .setTemporalIndexDetails(new TemporalIndexDetails(true) )
                .setFreeTextDetails( new FreeTextIndexDetails(true) )
                .setEntityCentricIndexDetails( new EntityCentricIndexDetails(true) )
                .setPCJIndexDetails(
                        PCJIndexDetails.builder()
                            .setEnabled(true)
                            .setFluoDetails( new FluoDetails("fluo_app_name") )
                            .build())
                .setProspectorDetails( new ProspectorDetails(Optional.<Date>absent()) )
                .setJoinSelectivityDetails( new JoinSelectivityDetails(Optional.<Date>absent()) )
                .build();

        assertEquals(expectedDetails, details);
    }

    @Test(expected = DuplicateInstanceNameException.class)
    public void install_alreadyExists() throws DuplicateInstanceNameException, CommandException, AccumuloException, AccumuloSecurityException {
        // Install an instance of Rya.
        final String instanceName = "testInstance_";
        final InstallConfiguration installConfig = InstallConfiguration.builder().build();

        final Install install = new AccumuloInstall(getConnectionDetails(), getConnector(), new Authorizations());
        install.install(instanceName, installConfig);

        // Install it again.
        install.install(instanceName, installConfig);
    }
}