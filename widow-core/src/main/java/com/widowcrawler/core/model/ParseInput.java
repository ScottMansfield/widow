package com.widowcrawler.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

import java.util.*;

/**
 * @author Scott Mansfield
 */
public class ParseInput {

    // Raw data from server
    private String originalURL;
    private Integer statusCode;
    private Map<String, List<String>> headers;
    private String pageContent;

    // any extra data that can be collected
    private Map<PageAttribute, Object> attributes;

    public static class Builder {
        private ParseInput building;

        public Builder() {
            this.building = new ParseInput();
            this.building.attributes = new HashMap<>();
        }

        public ParseInput build() {
            return this.building;
        }

        public Builder withOriginalURL(String originalURL) {
            this.building.originalURL = originalURL;
            return this;
        }

        public Builder withStatusCode(Integer statusCode) {
            this.building.statusCode = statusCode;
            return this;
        }

        public Builder withHeaders(Map<String, List<String>> headers) {
            this.building.headers = headers;
            return this;
        }

        public Builder withPageContent(String pageContent) {
            this.building.pageContent = pageContent;
            return this;
        }

        public Builder withAttribute(PageAttribute pageAttribute, Object value) {
            this.building.attributes.put(pageAttribute, value);
            return this;
        }
    }

    // no-arg constructor for the builder
    private ParseInput() { }

    // save me from my OCD
    @JsonCreator
    private ParseInput(
            @JsonProperty("originalURL")       String originalURL,
            @JsonProperty("pageContent")       String pageContent,
            @JsonProperty("headers")           Map<String, List<String>> headers,
            @JsonProperty("status")            Integer statusCode,
            @JsonProperty("attributes")        Map<PageAttribute, Object> attributes) {
        this.originalURL = originalURL;
        this.pageContent = pageContent;
        this.headers = Collections.unmodifiableMap(headers);
        this.statusCode = statusCode;
        this.attributes = Collections.unmodifiableMap(attributes);
    }

    public String getOriginalURL() {
        return originalURL;
    }

    public String getPageContent() {
        return pageContent;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public Map<PageAttribute, Object> getAttributes() {
        return attributes;
    }
}
