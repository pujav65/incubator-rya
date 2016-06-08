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
package mvm.rya.shell;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import com.google.common.collect.ImmutableList;

/**
 * Configures a {@link RyaShell}.
 */
@Immutable
@ParametersAreNonnullByDefault
public class RyaShellConfiguration {

    private final AccumuloConfiguration accumuloConf;

    /**
     * Constructs an instance of {@link RyaShellConfiguration}.
     *
     * @param accumuloConf - The Accumulo store specific configuration values. (not null)
     */
    public RyaShellConfiguration(final AccumuloConfiguration accumuloConf) {
        this.accumuloConf = requireNonNull( accumuloConf );
    }

    @Override
    public int hashCode() {
        return Objects.hash( accumuloConf );
    }

    @Override
    public boolean equals(final Object obj) {
        if(this == obj) {
            return true;
        }
        if(obj instanceof RyaShellConfiguration) {
            final RyaShellConfiguration conf = (RyaShellConfiguration) obj;
            return Objects.equals(accumuloConf, conf.accumuloConf);
        }
        return false;
    }

    /**
     * Configures how the shell will connect to an Accumulo Rya store.
     */
    @Immutable
    @ParametersAreNonnullByDefault
    public static class AccumuloConfiguration {

        private final String username;
        private final String password;
        private final String instanceName;
        private final ImmutableList<String> zookeepers;

        /**
         * Use a {@link Builder} to create instances of this class.
         */
        private AccumuloConfiguration(
                final String username,
                final String password,
                final String instanceName,
                final ImmutableList<String> zookeepers) {
            this.username = requireNonNull(username);
            this.password = requireNonNull(password);
            this.instanceName = requireNonNull(instanceName);
            this.zookeepers = requireNonNull(zookeepers);
        }

        /**
         * @return The username used to connect to Accumulo.
         */
        public String getUsername() {
            return username;
        }

        /**
         * @return The password used to connect to Accumulo.
         */
        public String getPassword() {
            return password;
        }

        /**
         * @return The name of the Accumulo instance.
         */
        public String getInstanceName() {
            return instanceName;
        }

        /**
         * @return The hostnames of the Zookeeper servers that provide access to the Accumulo instance.
         */
        public ImmutableList<String> getZookeepers() {
            return zookeepers;
        }

        @Override
        public int hashCode() {
            return Objects.hash(username, password, instanceName, zookeepers);
        }

        @Override
        public boolean equals(final Object obj) {
            if(this == obj) {
                return true;
            }
            if(obj instanceof AccumuloConfiguration) {
                final AccumuloConfiguration conf = (AccumuloConfiguration) obj;
                return Objects.equals(username, conf.username) &&
                        Objects.equals(password, conf.password) &&
                        Objects.equals(instanceName, conf.instanceName) &&
                        Objects.equals(zookeepers, conf.zookeepers);
            }
            return false;
        }

        public static Builder builder() {
            return new Builder();
        }

        /**
         * Builds insatnces of {@link AccumuloConfiguration}.
         */
        @ParametersAreNonnullByDefault
        public static class Builder {

            private String username;
            private String password;
            private String instanceName;
            private final ImmutableList.Builder<String> zookeepers = ImmutableList.builder();

            /**
             * @param username - The username used to connect to Accumulo.
             * @return This {@link Builder} so that method invocations may be chained.
             */
            public Builder setUsername(@Nullable final String username) {
                this.username = username;
                return this;
            }

            /**
             * @param password - The password used to connect to Accumulo.
             * @return This {@link Builder} so that method invocations may be chained.
             */
            public Builder setPassword(@Nullable final String password) {
                this.password = password;
                return this;
            }

            /**
             * @param instanceName - The name of the Accumulo instance.
             * @return This {@link Builder} so that method invocations may be chained.
             */
            public Builder setInstanceName(@Nullable final String instanceName) {
                this.instanceName = instanceName;
                return this;
            }

            /**
             * @param hostname - The hostname of a Zookeeper server that manages the Accumulo instance.
             * @return This {@link Builder} so that method invocations may be chained.
             */
            public Builder addZookeeperHostname(@Nullable final String hostname) {
                zookeepers.add( hostname );
                return this;
            }

            /**
             * @return Builds an instance of {@link AccumuloConfiguration} using this builder's values.
             */
            public AccumuloConfiguration build() {
                return new AccumuloConfiguration(username, password, instanceName, zookeepers.build());
            }
        }
    }
}