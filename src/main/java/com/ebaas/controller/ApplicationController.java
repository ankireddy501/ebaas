package com.ebaas.controller;

import com.ebaas.SecurityContext;
import com.ebaas.SecurityContextThreadLocal;
import com.ebaas.dao.ApplicationDAO;
import com.ebaas.domain.Application;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by anki on 04-04-2015.
 */
@Controller
public class ApplicationController {

    @Autowired
    private ApplicationDAO applicationDao;

    @RequestMapping(value="/application", produces = "application/json", method = RequestMethod.GET)
    public @ResponseBody List<Application> getApplications(){

        List<Application> applications = new ArrayList<Application>();
        try {
            SecurityContext context = SecurityContextThreadLocal.get();
            String tenantId = context.getTenantId();
            System.out.println("tenantId:"+tenantId);
            applications = applicationDao.getApplications(tenantId);
            if(applications.size() == 0){
                Application dummy = new Application();
                dummy.setId("Dummy");
                dummy.setName("Billboad India");
                dummy.setDescription("Place for finding the ad space");
                applications.add(dummy);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return applications;
    }

    @RequestMapping(value="/application/{id}", produces = "application/json" , method = RequestMethod.GET)
    public @ResponseBody Application getApplication(@PathVariable String id) {
        Application application = null;
        try {
            application = applicationDao.getApplication(id);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return application;

    }

    @RequestMapping(value = "/application", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public void createApplication(@RequestBody Application application) {
        application.setId(UUID.randomUUID().toString());
        try {
            SecurityContext context = SecurityContextThreadLocal.get();
            String tenantId = context.getTenantId();
            application.setTenantId(tenantId);
            applicationDao.createApplication(application);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/application", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateApplication(@RequestBody Application application) {
        try {
            applicationDao.updateApplication(application);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/application", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void deleteApplication(@RequestBody Application application) {
        applicationDao.deleteApplication(application);
    }

    @RequestMapping(value = "/application/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void deleteApplication(@PathVariable String id) {
        applicationDao.deleteApplication(id);
    }
}
