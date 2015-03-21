package com.widowcrawler.fetch.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model class for Fetch stage input
 *
 * @author Scott Mansfield
 */
public class FetchInput {
    private String url;

    @JsonCreator
    public FetchInput(@JsonProperty("url") String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
