package org.apache.rya.tinkerpop;

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
        // TODO Auto-generated method stub
        return value;
    }

    @Override
    public boolean isPresent() {
        // TODO no idea what to do here
        return true;
    }

    @Override
    public Element element() {
        // TODO Auto-generated method stub
        return element;
    }

    @Override
    public void remove() {
        
    }

}
