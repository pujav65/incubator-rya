package mvm.rya.shell.command.accumulo.administrative;

import static java.util.Objects.requireNonNull;

import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.admin.TableOperations;
import org.apache.accumulo.core.security.Authorizations;

import mvm.rya.accumulo.instance.AccumuloRyaInstanceDetailsRepository;
import mvm.rya.api.RdfCloudTripleStoreConstants;
import mvm.rya.shell.command.CommandException;
import mvm.rya.shell.command.accumulo.AccumuloCommand;
import mvm.rya.shell.command.accumulo.AccumuloConnectionDetails;
import mvm.rya.shell.command.administrative.InstanceExists;

/**
 * An Accumulo implementation of the {@link InstanceExists} command.
 */
@ParametersAreNonnullByDefault
public class AccumuloInstanceExists extends AccumuloCommand implements InstanceExists {

    /**
     * Constructs an insatnce of {@link AccumuloInstanceExists}.
     *
     * @param connectionDetails - Details about the values that were used to create the connector to the cluster. (not null)
     * @param connector - Provides programatic access to the instance of Accumulo
     *   that hosts Rya instance. (not null)
     * @param auths - The authorizations that will be used when interacting with
     *   the instance of Accumulo. (not null)
     */
    public AccumuloInstanceExists(final AccumuloConnectionDetails connectionDetails, final Connector connector, final Authorizations auths) {
        super(connectionDetails, connector, auths);
    }

    @Override
    public boolean exists(final String instanceName) throws CommandException {
        requireNonNull( instanceName );

        final TableOperations tableOps = getConnector().tableOperations();

        // Newer versions of Rya will have a Rya Details table.
        final String ryaDetailsTableName = instanceName + AccumuloRyaInstanceDetailsRepository.INSTANCE_DETAILS_TABLE_NAME;
        if(tableOps.exists(ryaDetailsTableName)) {
            return true;
        }

        // However, older versions only have the data tables.
        final String spoTableName = instanceName + RdfCloudTripleStoreConstants.TBL_SPO_SUFFIX;
        final String posTableName = instanceName + RdfCloudTripleStoreConstants.TBL_PO_SUFFIX;
        final String ospTableName = instanceName + RdfCloudTripleStoreConstants.TBL_OSP_SUFFIX;
        if(tableOps.exists(spoTableName) && tableOps.exists(posTableName) && tableOps.exists(ospTableName)) {
            return true;
        }

        return false;
    }
}