package com.ebaas.dao;

import com.ebaas.domain.Person;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.io.IOException;

/**
 * Created by anki on 04-04-2015.
 */
public class PersonDAO {

    private BaseDAO baseDAO;
    private Client client;

    private ObjectMapper mapper = new ObjectMapper();

    public PersonDAO(BaseDAO baseDAO){
            this.baseDAO = baseDAO;
            this.client = baseDAO.getClient();
    }

    public boolean createPerson(Person person) throws JsonProcessingException {
        IndexResponse response = client.prepareIndex("ebaas", "person", person.getId())
                .setSource(mapper.writeValueAsBytes(person))
                .execute().actionGet();
        return response.isCreated();
    }

    public Person authenticate(String userName,String password) throws IOException{

        Person person = null;
        MatchQueryBuilder userNameMatch = QueryBuilders.matchQuery("email", userName);
        MatchQueryBuilder passwordMatch = QueryBuilders.matchQuery("password", password);

        SearchRequestBuilder srb1 = client
                .prepareSearch().setQuery(userNameMatch).setSize(1);
        SearchRequestBuilder srb2 = client
                .prepareSearch().setQuery(passwordMatch).setSize(1);

        MultiSearchResponse sr = client.prepareMultiSearch()
                .add(srb1)
                .add(srb2)
                .execute().actionGet();

        long nbHits = 0;
        for (MultiSearchResponse.Item item : sr.getResponses()) {
            SearchResponse response = item.getResponse();
            SearchHit searchHit = response.getHits().getAt(0);
            System.out.println("searchHit:"+searchHit.getSourceAsString());
            person = (Person) mapper.readValue(searchHit.getSourceAsString().getBytes(), Person.class);
        }

        return person;
    }

    public boolean authenticate(String tenantId, String userName,String password) throws IOException{

        Person person = null;
        MatchQueryBuilder userNameMatch = QueryBuilders.matchQuery("email", userName);
        MatchQueryBuilder passwordMatch = QueryBuilders.matchQuery("password", password);

        SearchRequestBuilder srb1 = client
                .prepareSearch("ebaas"+tenantId.toLowerCase()).setQuery(userNameMatch).setSize(1);
        SearchRequestBuilder srb2 = client
                .prepareSearch("ebaas"+tenantId.toLowerCase()).setQuery(passwordMatch).setSize(1);

        MultiSearchResponse sr = client.prepareMultiSearch()
                .add(srb1)
                .add(srb2)
                .execute().actionGet();

        long nbHits = 0;
        for (MultiSearchResponse.Item item : sr.getResponses()) {
            SearchResponse response = item.getResponse();
            SearchHit searchHit = response.getHits().getAt(0);
            System.out.println("searchHit:"+searchHit.getSourceAsString());
            nbHits = 1;
        }
        if(nbHits > 0){
            return true;
        }else{
            return false;
        }

    }
}
