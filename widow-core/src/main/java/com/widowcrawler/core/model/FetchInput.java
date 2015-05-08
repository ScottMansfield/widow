package com.widowcrawler.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model class for Fetch stage input
 *
 * @author Scott Mansfield
 */
public class FetchInput {
    private String url;
    private String referrer;

    @JsonCreator
    public FetchInput(
            @JsonProperty("url") String url,
            @JsonProperty("referrer") String referrer) {
        this.url = url;
        this.referrer = referrer;
    }

    public String getUrl() {
        return url;
    }

    public String getReferrer() {
        return referrer;
    }
}
