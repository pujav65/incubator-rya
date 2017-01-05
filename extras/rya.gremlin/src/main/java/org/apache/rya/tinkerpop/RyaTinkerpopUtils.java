package org.apache.rya.tinkerpop;

import java.net.URI;

import org.apache.rya.api.RdfCloudTripleStoreConstants;

public class RyaTinkerpopUtils {

    static boolean validID(String string) {
        if ((string.split(RdfCloudTripleStoreConstants.DELIM).length <= 2) && !string.isEmpty()){
            return true;
        }
        return false;
    }

    static boolean isValidURI(String id) {
        try {
           URI.create(id); 
        }
        catch (Exception ex){
            return false;
        }
        return true;
    }

}
