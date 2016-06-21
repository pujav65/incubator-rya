package mvm.rya.shell.util;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Pretty formats a list of Rya instance names.
 */
@ParametersAreNonnullByDefault
public class InstanceNamesFormatter {

    /**
     * Formats the list of Rya instance names with a '*' next to whichever entry
     * matches the connected name.
     *
     * @param names - The Rya instance names. (not null)
     * @param connectedName - The instance name that will have a '*' next to it. (not null)
     * @return A string holding the pretty formatted list.
     */
    public String format(final List<String> names, final String connectedName) {
        requireNonNull(names);
        requireNonNull(connectedName);

        // Will be -1 if the connected name isn't in the list of names, so none will be starred.
        final int connectedIndex = names.indexOf( connectedName );

        final StringBuilder formatted = new StringBuilder("Rya instance names:\n");
        for(int i = 0; i < names.size(); i++) {
            if(i == connectedIndex) {
                formatted.append(" * ");
            } else {
                formatted.append("   ");
            }
            formatted.append( names.get(i) ).append("\n");
        }

        return formatted.toString();
    }

    /**
     * Formats the list of Rya instance names.
     *
     * @param names - The Rya instance names. (not null)
     * @return A string holding the pretty formatted list.
     */
    public String format(final List<String> names) {
        requireNonNull(names);

        final StringBuilder formatted = new StringBuilder("Rya instance names:\n");
        for(int i = 0; i < names.size(); i++) {
            formatted.append("   ").append( names.get(i) ).append("\n");
        }

        return formatted.toString();
    }
}