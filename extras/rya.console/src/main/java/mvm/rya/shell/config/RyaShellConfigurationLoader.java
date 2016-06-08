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

import static java.util.Objects.requireNonNull;

import java.io.InputStream;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import mvm.rya.shell.RyaShellConfiguration;
import mvm.rya.shell.RyaShellConfiguration.AccumuloConfiguration;
import mvm.rya.shell.config.generated.Configuration;
import mvm.rya.shell.config.generated.Configuration.Accumulo;

/**
 * Loads a {@link RyaShellConfiguration} from an XML document.
 */
@ParametersAreNonnullByDefault
public class RyaShellConfigurationLoader {

    private static final String SCHEMA_NAME = "config.xsd";

    /**
     * Loads a UTF-8 encoded XML document into a {@link RyaShellConfiguration} object.
     *
     * @param xmlStream - A stream holding the UTF-8 encoded XML to unmarshall. (not null)
     * @return The unmarshalled {@link RyaShellConfigurationLoader}.
     * @throws SAXException There was a problem loading the schema file.
     * @throws JAXBException There was a problem unmarshalling the XML file.
     */
    public RyaShellConfiguration load(final InputStream xmlStream) throws SAXException, JAXBException {
        requireNonNull( xmlStream );

        // Load the schema from the classpath.
        final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final InputStream schemaStream = ClassLoader.getSystemResourceAsStream( SCHEMA_NAME );
        final Source schemaSource = new StreamSource(schemaStream);
        final Schema schema = schemaFactory.newSchema(schemaSource);

        // Configure a context that loads the Configuration object and validates it using the schema.
        final JAXBContext context = JAXBContext.newInstance(Configuration.class);
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        unmarshaller.setSchema(schema);

        final Configuration config = (Configuration) unmarshaller.unmarshal( xmlStream );

        // Load the configuration values into the returned object.
        final Accumulo accumulo = config.getAccumulo();

        final AccumuloConfiguration.Builder builder = AccumuloConfiguration.builder()
                .setUsername( accumulo.getUsername() )
                .setPassword( accumulo.getPassword() )
                .setInstanceName( accumulo.getInstanceName() );

        for(final String hostname : accumulo.getZookeeperServers().getHostname()) {
            builder.addZookeeperHostname( hostname );
        }

        return new RyaShellConfiguration( builder.build() );
    }
}