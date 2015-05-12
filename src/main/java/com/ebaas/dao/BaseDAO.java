package com.ebaas.dao;

import com.ebaas.util.RESTClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by VST on 30-04-2015.
 */
public class BaseDAO {

    private RESTClient client;

    private Client oldClient;

    private URI uri = null;

    public RESTClient getClient() {
        return client;
    }

    public BaseDAO(String host, String port) {
        try {
            uri = new URI("http://" + host + ":" + port);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        client = new RESTClient();

        InetSocketTransportAddress address = new InetSocketTransportAddress(host,
                Integer.parseInt("9300"));
        oldClient = new TransportClient().addTransportAddress(address);
    }

    public URI getUri() {
        return uri;
    }

    public Client getOldClient() {
        return oldClient;
    }
}
