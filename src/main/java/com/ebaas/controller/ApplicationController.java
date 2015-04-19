package com.ebaas.controller;

import com.ebaas.domain.Application;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anki on 04-04-2015.
 */
@RestController
public class ApplicationController {

    @RequestMapping(value="/application", produces = "application/json", method = RequestMethod.GET)
    public @ResponseBody List<Application> getApplications(){

        List<Application> applications = new ArrayList<Application>();
        Application application = new Application();
        application.setName("webchain1");
        application.setDescription("manages the billboard");
        applications.add(application);

        Application application1 = new Application();
        application1.setName("webchain2");
        application1.setDescription("manages the billboard from mobile");
        applications.add(application1);

        return applications;
    }

    @RequestMapping(value="/application/{id}", produces = "application/json" , method = RequestMethod.GET)
    public @ResponseBody Application getApplication(@PathVariable String id) {

       Application application = new Application();
       application.setName("webchain");
       application.setDescription("manages the billboard");
       return application;

    }

}
