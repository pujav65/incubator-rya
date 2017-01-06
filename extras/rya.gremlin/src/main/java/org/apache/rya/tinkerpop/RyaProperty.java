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

import java.util.NoSuchElementException;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;

public class RyaProperty implements Property<String> {
    
    private String key;
    private String value;
    private Element element;

    public RyaProperty(String key, String value, Element element){
        this.key = key;
        this.value = value;
        this.element = element;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public String value() throws NoSuchElementException {
        return value;
    }

    @Override
    public boolean isPresent() {
        // TODO no idea what to do here
        return true;
    }

    @Override
    public Element element() {
        return element;
    }

    @Override
    public void remove() {
        // TODO not sure what to do here
        
    }

}
