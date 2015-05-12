package com.ebaas.dao;

import com.ebaas.domain.Application;
import com.ebaas.util.RESTClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by anki on 04-04-2015.
 */
public class ApplicationDAO {

    private RESTClient client;

    private URI uri;

    private ObjectMapper mapper;

    private Logger logger = Logger.getLogger("ApplicationDAO");

    public ApplicationDAO(BaseDAO baseDAO) {
        uri = baseDAO.getUri();
        client = baseDAO.getClient();
        mapper = new ObjectMapper();
    }

    public List<Application> getApplications(String tenantId) throws IOException {
        List<Application> applications = new ArrayList<Application>();
        Map condition = new HashMap();
        condition.put("tenantId",tenantId);

        Map match = new HashMap();
        match.put("match", condition);

        Map query = new HashMap();
        query.put("query", match);

        logger.info(mapper.writeValueAsString(query));

        RESTClient.Response response = client.post(createSerachURI(), mapper.writeValueAsString(query));

        logger.info(response.getBody());

        for (RESTClient.SearchHit hit : response.getSearchHits()) {
            applications.add(mapper.readValue(hit.getEntry("_source"), Application.class));
        }
        return applications;
    }

    public Application getApplication(String id) throws IOException {
        RESTClient.Response response = client.get(createUri(id));
        return mapper.readValue(response.getSource(), Application.class);
    }

    public boolean createApplication(Application application) throws JsonProcessingException {
        logger.info(createUri(application.getId()).toString());
        logger.info(mapper.writeValueAsString(application));
        RESTClient.Response response = client.post(createUri(application.getId()),
                mapper.writeValueAsString(application));
        return response.isCreated();
    }

    public boolean updateApplication(Application application) throws JsonProcessingException {
        RESTClient.StatusCode code = client.put(createUri(application.getId()),
                mapper.writeValueAsString(application));
        return true;
    }

    public boolean deleteApplication(Application application) {
        deleteApplication(application.getId());
        return true;
    }

    public boolean deleteApplication(String id) {
        RESTClient.StatusCode code = client.delete(createUri(id));
        return true;
    }

    private URI createUri() {
        return URI.create(uri.toString() +  "/ebaas" + "/application");
    }

    private URI createSerachURI() {
        return URI.create(createUri().toString() + "/_search");
    }

    private URI createUri(String id) {
        return URI.create(createUri().toString() + "/" + id);
    }
}
