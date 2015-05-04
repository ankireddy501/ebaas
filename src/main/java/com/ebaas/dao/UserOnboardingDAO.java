package com.ebaas.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;

/**
 * Created by anki on 04-05-2015.
 */
public class UserOnboardingDAO {

    private BaseDAO baseDAO;
    private Client client;

    private ObjectMapper mapper = new ObjectMapper();

    public UserOnboardingDAO(BaseDAO baseDAO){
        this.baseDAO = baseDAO;
        this.client = baseDAO.getClient();
    }

    public boolean registerUser(String tenantId, String json){
        System.out.println("tenant:"+tenantId);
        System.out.println("json:"+json);
        IndexResponse response = client.prepareIndex("ebaas"+tenantId.toLowerCase(), "person")
                .setSource(json)
                .execute().actionGet();
        return response.isCreated();
    }

}
