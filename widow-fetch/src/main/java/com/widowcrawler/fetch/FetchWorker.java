package com.widowcrawler.fetch;

import com.widowcrawler.core.Worker;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * @author Scott Mansfield
 */
public class FetchWorker implements Worker {

    private String target;

    public FetchWorker(String target) {
        this.target = target;
    }

    @Override
    public void run() {
        Client client = ClientBuilder.newClient();

        Response response = client.target(this.target).request().buildGet().invoke();

        if (response.getStatus() == Status.OK.getStatusCode()) {
            // assemble message to next stage
        }
    }
}
