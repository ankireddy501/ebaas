package com.ebaas.dao;

import com.ebaas.domain.Application;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by anki on 04-04-2015.
 */
public class ApplicationDAO {

    private Client client;

    private ObjectMapper mapper = new ObjectMapper();

    public ApplicationDAO(BaseDAO baseDAO) {
        client = baseDAO.getClient();
    }

    public List<Application> getApplications(String tenantId) throws IOException {
        List<Application> applications = new ArrayList<Application>();
        SearchResponse response = client.prepareSearch("ebaas").setTypes("application").
                setQuery(QueryBuilders.matchQuery("tenantId",tenantId))
                .execute().actionGet();

        for (SearchHit hit : response.getHits().hits()) {
            applications.add(mapper.readValue(hit.source(), Application.class));
        }
        return applications;
    }

    public Application getApplication(String id) throws IOException {
        GetResponse response = client.prepareGet("ebaas", "application", id).execute().actionGet();
        return mapper.readValue(response.getSourceAsBytes(), Application.class);
    }

    public boolean createApplication(Application application) throws JsonProcessingException {
        IndexResponse response = client.prepareIndex("ebaas", "application", application.getId())
                .setSource(mapper.writeValueAsBytes(application))
                .execute().actionGet();
        return response.isCreated();
    }

    public boolean updateApplication(Application application) throws JsonProcessingException {
        UpdateResponse response = client.prepareUpdate("ebaas", "application", application.getId())
                .setDoc(mapper.writeValueAsBytes(application)).execute().actionGet();
        return response.isCreated();
    }

    public boolean deleteApplication(Application application) {
        DeleteResponse response = client.prepareDelete("ebaas", "application", application.getId())
                .execute().actionGet();
        return response.isFound();
    }

    public boolean deleteApplication(String id) {
        DeleteResponse response = client.prepareDelete("ebaas", "application", id)
                .execute().actionGet();
        return response.isFound();
    }
}
