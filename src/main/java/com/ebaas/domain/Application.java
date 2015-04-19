package com.ebaas.domain;

/**
 * Created by anki on 04-04-2015.
 */
public class Application {

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String name;
    private String description;




}
