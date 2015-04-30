package com.ebaas.dao;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

/**
 * Created by VST on 30-04-2015.
 */
public class BaseDAO {

    private Client client;

    public Client getClient() {
        return client;
    }

    public BaseDAO(String host, String port) {
        InetSocketTransportAddress address = new InetSocketTransportAddress(host,
                Integer.parseInt(port));
        client = new TransportClient().addTransportAddress(address);
    }
}
