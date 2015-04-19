package com.ebaas.controller;

import com.ebaas.domain.Person;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by anki on 04-04-2015.
 */
@Controller
@RequestMapping("/person")
public class PersonController {


    @RequestMapping(value="/{id}", produces="application/json", method = RequestMethod.GET)
    public Person getPerson(@PathVariable String id) {

        Person person = null;

        return person;

    }

}