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
package org.apache.rya.api.domain;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.rya.api.persist.RdfDAOException;

import com.google.gson.Gson;

public class StatementMetadata {
    
    private static Gson gson = new Gson();
    public static StatementMetadata EMPTY_METADATA = new StatementMetadata();
    
    private Map<String, String> metadataMap = new HashMap<String, String>();
    
    public StatementMetadata() {
        
    }
    
    public Map<String, String> asMap() {
        return new HashMap<String, String>(metadataMap);
    }
    
    public String getValue(String key){
        return metadataMap.get(key);
    }
    
    public void addMetadata(String key, String value){
        metadataMap.put(key, value);
    }

    public void remove(String key){
        metadataMap.remove(key);
    }

    public StatementMetadata(byte[] value) throws RdfDAOException {
        try {
            if (value == null) {
                metadataMap = new HashMap<>();
            } else {
                // try to convert back to a json string and then back to the map.
                String metadataString = new String(value, "UTF8");
                metadataMap = gson.fromJson(metadataString, HashMap.class);
                if (metadataMap == null) {
                    metadataMap = new HashMap<>();
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new RdfDAOException(e);
        }
    }

    public StatementMetadata(String statementMetadata) {
        try {
            metadataMap = gson.fromJson(statementMetadata, HashMap.class);
        } catch (Exception e) {
            throw new RdfDAOException(e);
        }
    }
    
    public String toString(){
        return gson.toJson(metadataMap);
    }

    public byte[] toBytes() {
        // convert the map to a json string
        if (metadataMap.isEmpty()) {
            return null;
        }
        String metadataString = gson.toJson(metadataMap);
        // TODO may want to cache this for performance reasons
        return metadataString.getBytes();
    }

}
