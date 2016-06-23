package mvm.rya.shell.command.administrative;

import javax.annotation.ParametersAreNonnullByDefault;

import mvm.rya.shell.command.CommandException;

/**
 * Checks if an instance of Rya has been installed.
 */
@ParametersAreNonnullByDefault
public interface InstanceExists {

    /**
     * Checks if an instance of Rya has been installed.
     *
     * @param instanceName - The name to check. (not null)
     * @return {@code true} If an instance of Rya exists with the provided name; otherwise {@code false}.
     * @throws CommandException Something caused the command to fail.
     */
    public boolean exists(String instanceName) throws CommandException;
}