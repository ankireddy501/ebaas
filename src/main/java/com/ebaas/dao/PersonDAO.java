package com.ebaas.dao;

import com.ebaas.domain.Person;
import com.ebaas.util.RESTClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Created by anki on 04-04-2015.
 */
public class PersonDAO {

    private BaseDAO baseDAO;
    private RESTClient client;
    private URI uri;

    private ObjectMapper mapper = new ObjectMapper();

    private Logger logger = Logger.getLogger("PersonDAO");

    public PersonDAO(BaseDAO baseDAO){
        this.uri = baseDAO.getUri();
        this.baseDAO = baseDAO;
        this.client = baseDAO.getClient();
    }

    public boolean createPerson(Person person) throws JsonProcessingException {
        RESTClient.Response response = client.post(createUri(), mapper.writeValueAsString(person));
        return response.isCreated();
    }

    public Person authenticate(String userName,String password) throws IOException{

        Person person = null;

        String query = "{\n" +
                "\"query\":{\n" +
                "\t  \"bool\": {\n" +
                "\t\t\"must\": [\n" +
                "\t\t  { \"match\": { \"email\": \"" + userName + "\" }},\n" +
                "\t\t  { \"match\": { \"password\": \"" + password + "\" }}\n" +
                "\t\t]\n" +
                "\t  }\n" +
                "\t}\n" +
                "}";

        logger.info(query);

        RESTClient.Response response = client.post(createSerachURI(), query);

        logger.info(response.getBody());

        for (RESTClient.SearchHit searchHit : response.getSearchHits()) {
            person = mapper.readValue(searchHit.getEntry("_source"), Person.class);
        }

        return person;
    }

    public Person authenticate(String tenantId, String userName,String password) throws IOException{

        Person person = null;

        String query = "{\n" +
                "\"query\":{\n" +
                "\t  \"bool\": {\n" +
                "\t\t\"must\": [\n" +
                "\t\t  { \"match\": { \"email\": \"" + userName + "\" }},\n" +
                "\t\t  { \"match\": { \"password\": \"" + password + "\" }}\n" +
                "\t\t]\n" +
                "\t  }\n" +
                "\t}\n" +
                "}";

        logger.info(query);

        RESTClient.Response response = client.post(createSerachURI(tenantId), query);

        logger.info("query response:"+response.getBody());

        try{
            for (RESTClient.SearchHit searchHit : response.getSearchHits()) {
                person = mapper.readValue(searchHit.getEntry("_source"), Person.class);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return person;
    }

    private URI createUri() {
        return URI.create(uri.toString() +  "/ebaas" + "/person");
    }

    private URI createSerachURI() {
        return URI.create(createUri().toString() + "/_search");
    }

    private URI createUri(String id) {
        return URI.create(createUri().toString() + "/" + id);
    }

    private URI createUriWithTenant(String tenantId) {
        return URI.create(uri.toString() +  "/ebaas" + tenantId.toLowerCase() + "/person");
    }

    private URI createSerachURI(String tenantId) {
        return URI.create(createUriWithTenant(tenantId).toString() + "/_search");
    }

    private URI createUri(String tenantId, String id) {
        return URI.create(createUriWithTenant(tenantId).toString() + "/" + id);
    }
}
