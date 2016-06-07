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
package mvm.rya.shell.command.administrative;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import mvm.rya.shell.command.CommandException;

/**
 * Installs a new instance of Rya.
 */
@ParametersAreNonnullByDefault
public interface Install {

    /**
     * Install a new instance of Rya.
     *
     * @param instanceName - Indicates the name of the Rya instance to install. (not null)
     * @param installConfig - Configures how the Rya instance will operate. The
     *   instance name that is in this variable must match the {@code instanceName}. (not null)
     * @throws DuplicateInstanceNameException A Rya instance already exists for the provided name.
     * @throws CommandException Something caused the command to fail.
     */
    public void install(final String instanceName, final InstallConfiguration installConfig) throws DuplicateInstanceNameException, CommandException;

    /**
     * A Rya instance already exists for the provided name.
     */
    public static class DuplicateInstanceNameException extends CommandException {
        private static final long serialVersionUID = 1L;

        public DuplicateInstanceNameException(final String message) {
            super(message);
        }
    }

    /**
     * Configures how an instance of Rya will be configured when it is installed.
     */
    @Immutable
    @ParametersAreNonnullByDefault
    public static class InstallConfiguration {

        private final boolean enableFreeTextIndex;
        private final boolean enableGeoIndex;
        private final boolean enableEntityCentricIndex;
        private final boolean enableTemporalIndex;
        private final boolean enablePcjIndex;

        /**
         * Use a {@link Builder} to create instances of this class.
         */
        private InstallConfiguration(
                final boolean enableFreeTextIndex,
                final boolean enableGeoIndex,
                final boolean enableEntityCentricIndex,
                final boolean enableTemporalIndex,
                final boolean enablePcjIndex) {
            this.enableFreeTextIndex = enableFreeTextIndex;
            this.enableGeoIndex = enableGeoIndex;
            this.enableEntityCentricIndex = enableEntityCentricIndex;
            this.enableTemporalIndex = enableTemporalIndex;
            this.enablePcjIndex = enablePcjIndex;
        }

        /**
         * @return Whether or not the installed instance of Rya will maintain a Free Text index.
         */
        public boolean isFreeTextIndexEnabled() {
            return enableFreeTextIndex;
        }

        /**
         * @return Whether or not the installed instance of Rya will maintain a Geospatial index.
         */
        public boolean isGeoIndexEnabled() {
            return enableGeoIndex;
        }

        /**
         * @return Whether or not the installed instance of Rya will maintain an Entity Centric index.
         */
        public boolean isEntityCentrixIndexEnabled() {
            return enableEntityCentricIndex;
        }

        /**
         * @return Whether or not the installed instance of Rya will maintain a Temporal index.
         */
        public boolean isTemporalIndexEnabled() {
            return enableTemporalIndex;
        }

        /**
         * @return Whether or not the installed instance of Rya will maintain a PCJ index.
         */
        public boolean isPcjIndexEnabled() {
            return enablePcjIndex;
        }

        /**
         * Builds instances of {@link InstallConfiguration}.
         */
        @ParametersAreNonnullByDefault
        public static class Builder {
            private boolean enableFreeTextIndex = false;
            private boolean enableGeoIndex = false;
            private boolean enableEntityCentricIndex = false;
            private boolean enableTemporalIndex = false;
            private boolean enablePcjIndex = false;

            /**
             * @param enabled - Whether or not the installed instance of Rya will maintain a Free Text index.
             * @return This {@link Builder} so that method invocations may be chained.
             */
            public Builder setEnableFreeTextIndex(final boolean enabled) {
                enableFreeTextIndex = enabled;
                return this;
            }

            /**
             * @param enabled - Whether or not the installed instance of Rya will maintain a Geospatial index.
             * @return This {@link Builder} so that method invocations may be chained.
             */
            public Builder setEnableGeoIndex(final boolean enabled) {
                enableGeoIndex = true;
                return this;
            }

            /**
             * @param enabled - Whether or not the installed instance of Rya will maintain an Entity Centric index.
             * @return This {@link Builder} so that method invocations may be chained.
             */
            public Builder setEnableEntityCentricIndex(final boolean enabled) {
                enableEntityCentricIndex = true;
                return this;
            }

            /**
             * @param enabled - Whether or not the installed instance of Rya will maintain a Temporal index.
             * @return This {@link Builder} so that method invocations may be chained.
             */
            public Builder setEnableTemporalIndex(final boolean enabled) {
                enableTemporalIndex = true;
                return this;
            }

            /**
             * @param enabled - Whether or not the installed instance of Rya will maintain a PCJ index.
             * @return This {@link Builder} so that method invocations may be chained.
             */
            public Builder setEnablePcjIndex(final boolean enabled) {
                enablePcjIndex = true;
                return this;
            }

            /**
             * @return Builds an instance of {@link InstallConfiguration} using this builder's values.
             */
            public InstallConfiguration build() {
                return new InstallConfiguration(
                        enableFreeTextIndex,
                        enableGeoIndex,
                        enableEntityCentricIndex,
                        enableTemporalIndex,
                        enablePcjIndex);
            }
        }
    }
}