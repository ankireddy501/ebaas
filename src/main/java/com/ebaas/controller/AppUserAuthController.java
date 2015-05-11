package com.ebaas.controller;

import com.ebaas.SecurityContext;
import com.ebaas.SecurityContextThreadLocal;
import com.ebaas.dao.PersonDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.misc.BASE64Decoder;
import java.util.UUID;

/**
 * Created by anki on 04-05-2015.
 */
@Controller
public class AppUserAuthController {

    @Autowired
    private PersonDAO personDAO;

    @RequestMapping(value="/appUserAuth", produces = "application/json", method = RequestMethod.POST)
    public @ResponseBody String authenticatePerson(@RequestHeader(value="Authorization") String authorizationHeader) throws Exception {
        System.out.println("authorizationHeader:"+authorizationHeader);
        SecurityContext context = SecurityContextThreadLocal.get();
        String tenantId = context.getTenantId();
        System.out.println("tenantId:"+tenantId);
        String base64Auth = authorizationHeader.substring("Basic ".length());
        System.out.println("base64Auth"+base64Auth);
        BASE64Decoder base64Decoder = new BASE64Decoder();
        String decodedAuth = new String(base64Decoder.decodeBuffer(base64Auth));
        System.out.println("decoded string:"+decodedAuth);
        String[] credentials = decodedAuth.split(":");
        boolean isAuthenticated = personDAO.authenticate(tenantId, credentials[0], credentials[1]);
        if(!isAuthenticated){
            throw new Exception("Invalid Credentials");
        }
        String token = UUID.randomUUID().toString().toLowerCase();
        return "{" +"\""+"Authorization"+"\""+":"+"\""+token+"\""+"}";
    }
}
