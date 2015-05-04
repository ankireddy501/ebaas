package com.ebaas.controller;

import com.ebaas.SecurityContext;
import com.ebaas.SecurityContextThreadLocal;
import com.ebaas.dao.UserOnboardingDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by anki on 02-05-2015.
 */
@Controller
public class AppUserRegistrationController {

    @Autowired
    UserOnboardingDAO userOnboardingDao;

    @RequestMapping(value="/appUserRegistration/_schema", produces = "application/json", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public void createSchema(String json){
            System.out.println("Created Schema");
    }

    @RequestMapping(value="/appUserRegistration/_schema", produces = "application/json", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.CREATED)
    public void updateSchema(String json){
        System.out.println("Updated Schema");
    }

    @RequestMapping(value="/appUserRegistration/_count", produces = "application/json", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.CREATED)
    public Integer getTotalRegisteredUsers(){

        return 0;
    }

    @RequestMapping(value="/appUserRegistration", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public void createUser(@RequestBody String json){
        SecurityContext context = SecurityContextThreadLocal.get();
        String tenantId = context.getTenantId();
        userOnboardingDao.registerUser(tenantId,json);
    }

}
