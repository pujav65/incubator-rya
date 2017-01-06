package org.apache.rya.tinkerpop;
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

import java.net.URI;
import java.util.Optional;

import org.apache.rya.api.RdfCloudTripleStoreConstants;
import org.apache.rya.api.domain.RyaType;
import org.apache.rya.api.domain.RyaURI;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

public class RyaTinkerpopUtils {
    private static ValueFactory valueFactory = new ValueFactoryImpl();

    public static boolean validID(String string) {
        if ((string.split(RdfCloudTripleStoreConstants.DELIM).length <= 2) && !string.isEmpty()){
            return true;
        }
        return false;
    }

    public static boolean isValidURI(String id) {
        try {
           URI.create(id); 
        }
        catch (Exception ex){
            return false;
        }
        return true;
    }

    public static RyaVertex getVertex(RyaGraph graph, Object... keyValues) {
        // look for the id, otherwise look for the label
        Optional<Object> nodeID = ElementHelper.getIdValue(keyValues);
        RyaType ryaType;
        if (nodeID.isPresent() && validID(nodeID.get().toString())){
            String id = nodeID.get().toString();
            String[] delimId = id.split(RdfCloudTripleStoreConstants.DELIM);
            if (delimId.length == 2){
               String value = delimId[0];
               String type = delimId[1];
               ryaType = new RyaType(valueFactory.createURI(type), value);
            }
            else {
                // TODO should we assume it is a uri if it doesn't have a type?
                // right now assuming it is a poorly formed uri, then it is a string
                if (isValidURI(id)){
                    ryaType = new RyaURI(id);                 
                }
                else {
                    ryaType = new RyaType(id);
                }
            }      
        }
        else {
            Optional<String> label = ElementHelper.getLabelValue(keyValues);
            if (label.isPresent() && label.get().isEmpty()){
                String value = label.get();
                ryaType = new RyaType(value);
            }
            else {
                // TODO better exception handling
                throw Graph.Exceptions.vertexAdditionsNotSupported();
             }
        }     
        return new RyaVertex(ryaType, graph);
    }

}
