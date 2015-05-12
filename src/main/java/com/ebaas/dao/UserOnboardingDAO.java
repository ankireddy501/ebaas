package com.ebaas.dao;

import com.ebaas.util.RESTClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;

import java.net.URI;
import java.util.logging.Logger;

/**
 * Created by anki on 04-05-2015.
 */
public class UserOnboardingDAO {

    private RESTClient client;
    private URI uri;

    private ObjectMapper mapper = new ObjectMapper();

    private Logger logger = Logger.getLogger("UserOnboardingDAO");

    public UserOnboardingDAO(BaseDAO baseDAO){
        this.client = baseDAO.getClient();
        this.uri = baseDAO.getUri();
    }

    public boolean registerUser(String tenantId, String json){
        logger.info("tenant:"+tenantId);
        logger.info("json:"+json);
        logger.info(createUri(tenantId).toString());
        RESTClient.Response response = client.post(createUri(tenantId), json);
        return response.isCreated();
    }

    private URI createUri(String tenantId) {
        return URI.create(uri.toString() +  "/ebaas" + tenantId.toLowerCase() + "/person");
    }

    private URI createSerachURI(String tenantId) {
        return URI.create(createUri(tenantId).toString() + "/_search");
    }

    private URI createUri(String tenantId, String id) {
        return URI.create(createUri(tenantId).toString() + "/" + id);
    }

}
