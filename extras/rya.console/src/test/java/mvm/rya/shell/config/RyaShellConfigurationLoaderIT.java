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
package mvm.rya.shell.config;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.xml.sax.SAXException;

import mvm.rya.shell.RyaShellConfiguration;
import mvm.rya.shell.RyaShellConfiguration.AccumuloConfiguration;

/**
 * Tests the methods of {@link RyaShellConfigurationLoader}.
 */
public class RyaShellConfigurationLoaderIT {

    @Test
    public void unmarshal() throws SAXException, IOException, JAXBException {
        // Valid XML.
        final String xml =
                "<configuration>" +
                    "<accumulo>" +
                        "<username>user</username>" +
                        "<password>password</password>" +
                        "<instanceName>accumuloInstanceName</instanceName>" +
                        "<zookeeperServers>" +
                            "<hostname>zoo1</hostname>" +
                            "<hostname>zoo2</hostname>" +
                            "<hostname>zoo3</hostname>" +
                        "</zookeeperServers>" +
                    "</accumulo>" +
                "</configuration>";

        // Unmarshall it.
        final byte[] xmlBytes = xml.getBytes( StandardCharsets.UTF_8 );
        final ByteArrayInputStream stream = new ByteArrayInputStream( xmlBytes );
        final RyaShellConfiguration unmershalled = new RyaShellConfigurationLoader().load(stream);

        // Ensure the unmarshalled configuration matches teh expected value.
        final RyaShellConfiguration expected = new RyaShellConfiguration(
                AccumuloConfiguration.builder()
                    .setUsername("user")
                    .setPassword("password")
                    .setInstanceName("accumuloInstanceName")
                    .addZookeeperHostname("zoo1")
                    .addZookeeperHostname("zoo2")
                    .addZookeeperHostname("zoo3")
                    .build());

        assertEquals(expected, unmershalled);
    }

    @Test(expected = JAXBException.class)
    public void invalidXml() throws SAXException, JAXBException {
        // Invalid XML.
        final String xml =
                "<configuration>" +
                    "<accumulo>" +
                        "<username>user</username>" +
                        "<password>password</password>" +
                        "<instanceName>accumuloInstanceName</instanceName>" +
                    "</accumulo>" +
                "</configuration>";

        // Show that an exception is thrown when trying to unmarshall it.
        final byte[] xmlBytes = xml.getBytes( StandardCharsets.UTF_8 );
        final ByteArrayInputStream stream = new ByteArrayInputStream( xmlBytes );
        new RyaShellConfigurationLoader().load(stream);
    }
}