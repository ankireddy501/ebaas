package com.ebaas.controller;

import com.ebaas.SecurityContext;
import com.ebaas.SecurityContextProvider;
import com.ebaas.SecurityContextThreadLocal;
import com.ebaas.dao.PersonDAO;
import com.ebaas.domain.Person;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Created by anki on 04-04-2015.
 */
@Controller
public class PersonController {

    @Autowired
    PersonDAO personDAO;

    @RequestMapping(value="/person", produces = "application/json", method = RequestMethod.POST)
    public @ResponseBody String createPerson(@RequestBody Person person) throws Exception {
        try {

            String tenantId = UUID.randomUUID().toString().toLowerCase();
            person.setTenantId(tenantId);
            boolean isCreated = personDAO.createPerson(person);
            if(isCreated){
                return authenticatePerson(person.getEmail(),person.getPassword());
            }
        } catch (JsonProcessingException e) {
            throw new Exception("Failed to create the user");
        }
        return null;
    }

    @RequestMapping(value="/person/{userName}/{password}", produces = "application/json", method = RequestMethod.POST)
    public @ResponseBody String authenticatePerson(@PathVariable String userName, @PathVariable String password) throws Exception {
        Person person = personDAO.authenticate(userName, password);
        if(person == null){
            throw new Exception("Invalid Credentials");
        }
        String token = UUID.randomUUID().toString().toLowerCase();
        SecurityContext securityContext = new SecurityContext();
        securityContext.setTenantId(person.getTenantId());
        securityContext.setPerson(person);
        SecurityContextThreadLocal.set(securityContext);
        SecurityContextProvider.addToken(token, securityContext);
        return "{" +"\""+"Authorization"+"\""+":"+"\""+token+"\""+"}";
    }

}