package com.widowcrawler.analyze.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * @author Scott Mansfield
 */
@Path("test")
public class TestResources {

    @GET
    @Path("ping")
    public String testGet() {
        return "pong";
    }

}
