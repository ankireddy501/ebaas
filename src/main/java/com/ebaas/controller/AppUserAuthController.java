package com.ebaas.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by anki on 04-05-2015.
 */
public class AppUserAuthController {

    @RequestMapping(value="/appUserAuth", produces = "application/json", method = RequestMethod.POST)
    public @ResponseBody String authenticatePerson(@RequestBody String basicAuth) throws Exception {
/*        Person person = personDAO.authenticate(userName, password);
        if(person == null){
            throw new Exception("Invalid Credentials");
        }
        String token = UUID.randomUUID().toString().toLowerCase();
        SecurityContext securityContext = new SecurityContext();
        securityContext.setTenantId(person.getTenantId());
        securityContext.setPerson(person);
        SecurityContextThreadLocal.set(securityContext);
        SecurityContextProvider.addToken(token, securityContext);
        return "{" +"\""+"Authorization"+"\""+":"+"\""+token+"\""+"}";*/
        return null;
    }
}
