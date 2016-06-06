package mvm.rya.shell.command.administrative;

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

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

/**
 * TODO doc
 */
@ParametersAreNonnullByDefault
public interface Install {

    /**
     * TODO doc
     *
     * the details are used to configure what's going to be installed. Right?
     *
     * @param instanceName
     * @param details
     */
    public void install(final String instanceName, final InstallConfiguration installConfig);


    /**
     * TODO impl, test, doc
     */
    @Immutable
    @ParametersAreNonnullByDefault
    public static class InstallConfiguration {

        private boolean enableFreeTextIndex;
        private boolean enableGeoIndex;
        private boolean enableEntityCentricIndex;
        private boolean enableTemporalIndex;
        private boolean enablePcjIndex;

        private InstallConfiguration() {

        }

        public static class Builder {
            private final boolean enableFreeTextIndex = false;
            private final boolean enableGeoIndex = false;
            private final boolean enableEntityCentricIndex = false;
            private final boolean enableTemporalIndex = false;
            private final boolean enablePcjIndex = false;

        }


        // TODO for now just have a list of which indexes will be enabled by default?



        // booleans for each of the indices

    }
}