package com.ebaas;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anki on 03-05-2015.
 */
public class SecurityContextProvider {

    private static final Map<String, SecurityContext> securityMap = new HashMap<String, SecurityContext>();

    public static boolean isTokenValid(String token){
        return securityMap.get(token) != null? true: false ;
    }

    public static void addToken(String token, SecurityContext securityContext){
        securityMap.put(token,securityContext);
    }

    public static SecurityContext getContext(String token){
        return securityMap.get(token);
    }

}
