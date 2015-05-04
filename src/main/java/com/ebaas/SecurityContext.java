package com.ebaas;

import com.ebaas.domain.Person;

/**
 * Created by anki on 03-05-2015.
 */
public class SecurityContext {

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    private String tenantId;

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    private Person person;


}
