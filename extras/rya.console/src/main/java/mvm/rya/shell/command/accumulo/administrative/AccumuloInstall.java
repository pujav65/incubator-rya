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

import static java.util.Objects.requireNonNull;

import java.util.Date;

import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.security.Authorizations;

import com.google.common.base.Optional;

import mvm.rya.accumulo.AccumuloRdfConfiguration;
import mvm.rya.accumulo.instance.AccumuloRyaInstanceDetailsRepository;
import mvm.rya.api.RdfCloudTripleStoreConfiguration;
import mvm.rya.api.instance.RyaDetails;
import mvm.rya.api.instance.RyaDetails.EntityCentricIndexDetails;
import mvm.rya.api.instance.RyaDetails.FreeTextIndexDetails;
import mvm.rya.api.instance.RyaDetails.GeoIndexDetails;
import mvm.rya.api.instance.RyaDetails.JoinSelectivityDetails;
import mvm.rya.api.instance.RyaDetails.PCJIndexDetails;
import mvm.rya.api.instance.RyaDetails.ProspectorDetails;
import mvm.rya.api.instance.RyaDetails.TemporalIndexDetails;
import mvm.rya.api.instance.RyaDetailsRepository;
import mvm.rya.api.instance.RyaDetailsRepository.AlreadyInitializedException;
import mvm.rya.api.instance.RyaDetailsRepository.RyaDetailsRepositoryException;
import mvm.rya.indexing.accumulo.ConfigUtils;
import mvm.rya.shell.command.CommandException;
import mvm.rya.shell.command.accumulo.AccumuloCommand;
import mvm.rya.shell.command.accumulo.AccumuloConnectionDetails;
import mvm.rya.shell.command.administrative.Install;
import mvm.rya.shell.command.administrative.InstanceExists;

// TODO impl

/**
 * An Accumulo implementation of the {@link Install} command.
 */
@ParametersAreNonnullByDefault
public class AccumuloInstall extends AccumuloCommand implements Install {

    private final InstanceExists instanceExists;

    /**
     * Constructs an instance of {@link AccumuloInstall}.
     *
     * @param connectionDetails - Details about the values that were used to create the connector to the cluster. (not null)
     * @param connector - Provides programatic access to the instance of Accumulo
     *   that hosts Rya instance. (not null)
     * @param auths - The authorizations that will be used when interacting with
     *   the instance of Accumulo. (not null)
     */
    public AccumuloInstall(final AccumuloConnectionDetails connectionDetails, final Connector connector, final Authorizations auths) {
        super(connectionDetails, connector, auths);
        instanceExists = new AccumuloInstanceExists(connectionDetails, connector, auths);
    }

    @Override
    public void install(final String instanceName, final InstallConfiguration installConfig) throws DuplicateInstanceNameException, CommandException {
        requireNonNull(instanceName);
        requireNonNull(installConfig);

        // Check to see if a Rya instance has already been installed with this name.
        if(instanceExists.exists(instanceName)) {
            throw new DuplicateInstanceNameException("An instance of Rya has already been installed to this Rya storage " +
                    "with the name '" + instanceName + "'. Try again with a different name.");
        }

        // Initialize the Rya Details table.
        RyaDetails details;
        try {
            details = initializeRyaDetails(instanceName, installConfig);
        } catch (final AlreadyInitializedException e) {
            // This can only happen if somebody else installs an instance of Rya with the name between the check and now.
            throw new DuplicateInstanceNameException("An instance of Rya has already been installed to this Rya storage " +
                    "with the name '" + instanceName + "'. Try again with a different name.");
        } catch (final RyaDetailsRepositoryException e) {
            throw new CommandException("The RyaDetails couldn't be initialized. Details: " + e.getMessage(), e);
        }

        // TODO initialize the tables that will be used by the instance (spo, indexes, etc)

        // Initialize the rest of the tables used by the Rya instance.
//        final AccumuloRdfConfiguration ryaConfig = makeRyaConfig(getAccumuloConnectionDetails(), details);
//        final AccumuloRyaDAO ryaDao = new AccumuloRyaDAO();
//        ryaDao.setConf( ryaConfig );
//        ryaDao.setConnector( getConnector() );
//
//        final TablePrefixLayoutStrategy tls = new TablePrefixLayoutStrategy();
//        tls.setTablePrefix(instanceName);
//        ryaConfig.setTableLayoutStrategy(tls);
//
//        try {
//            ryaDao.init();
//        } catch (final RyaDAOException e) {
//            // TODO text
//            throw new CommandException("", e);
//        }
    }

    /**
     * TODO doc
     *
     * @return
     * @throws CommandException
     */
    private String getVersion() {
        return "" + this.getClass().getPackage().getImplementationVersion();
    }

    /**
     * TODO doc
     *
     * @param instanceName
     * @param installConfig
     * @return
     * @throws AlreadyInitializedException
     * @throws RyaDetailsRepositoryException
     * @throws CommandException
     */
    private RyaDetails initializeRyaDetails(final String instanceName, final InstallConfiguration installConfig) throws AlreadyInitializedException, RyaDetailsRepositoryException, CommandException {
        final RyaDetailsRepository detailsRepo = new AccumuloRyaInstanceDetailsRepository(getConnector(), instanceName);

        // Store the initial configuration information about the Rya instance to an accumulo table.
        final RyaDetails details = RyaDetails.builder()
                // General Metadata
                .setRyaInstanceName(instanceName)
                .setRyaVersion( getVersion() )

                // Secondary Index Values
                .setGeoIndexDetails(
                        new GeoIndexDetails(installConfig.isGeoIndexEnabled()))
                .setTemporalIndexDetails(
                        new TemporalIndexDetails(installConfig.isTemporalIndexEnabled()))
                .setFreeTextDetails(
                        new FreeTextIndexDetails(installConfig.isFreeTextIndexEnabled()))
                .setEntityCentricIndexDetails(
                        new EntityCentricIndexDetails(installConfig.isEntityCentrixIndexEnabled()))
                .setPCJIndexDetails(
                        PCJIndexDetails.builder()
                            .setEnabled(installConfig.isPcjIndexEnabled())
                            // TODO fluo app stuff if pcjs are enabled.
                            .build())

                // Statistics values.
                .setProspectorDetails(
                        new ProspectorDetails(Optional.<Date>absent()) )
                .setJoinSelectivityDetails(
                        new JoinSelectivityDetails(Optional.<Date>absent()) )
                .build();

        // Initialize the table.
        detailsRepo.initialize(details);

        return details;
    }

    private static AccumuloRdfConfiguration makeRyaConfig(final AccumuloConnectionDetails connectionDetails, final RyaDetails details) {
        final AccumuloRdfConfiguration conf = new AccumuloRdfConfiguration();

        conf.set(RdfCloudTripleStoreConfiguration.CONF_TBL_PREFIX, details.getRyaInstanceName());

        conf.set(ConfigUtils.USE_PCJ, "" + details.getPCJIndexDetails().isEnabled() );
        conf.set(ConfigUtils.USE_GEO, "" + details.getGeoIndexDetails().isEnabled() );
        conf.set(ConfigUtils.USE_FREETEXT, "" + details.getFreeTextIndexDetails().isEnabled() );
        conf.set(ConfigUtils.USE_TEMPORAL, "" + details.getTemporalIndexDetails().isEnabled() );

        final boolean entityEnabled = details.getEntityCentricIndexDetails().isEnabled();
        conf.set(ConfigUtils.USE_ENTITY, "" +  entityEnabled);
        if(entityEnabled) {
            conf.set(ConfigUtils.ENTITY_TABLENAME, ConfigUtils.getEntityTableName(conf));
        }

        // XXX The Accumulo implementation of the secondary indices make need all
        //     of the accumulo connector's parameters for some reason, so we need
        //     to include them here. This should be required, though. The indexer
        //     should use the connector that is provided to it.
        conf.set(ConfigUtils.CLOUDBASE_USER, connectionDetails.getUsername());
        conf.set(ConfigUtils.CLOUDBASE_PASSWORD, new String(connectionDetails.getPassword()));
        conf.set(ConfigUtils.CLOUDBASE_INSTANCE, connectionDetails.getInstanceName());
        conf.set(ConfigUtils.CLOUDBASE_ZOOKEEPERS, connectionDetails.getZookeepers());

        ConfigUtils.setIndexers(conf);

        return conf;
    }
}