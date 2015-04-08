package com.widowcrawler.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *@author Scott Mansfield
 */
public class IndexInput {

    // Raw data from server
    private String originalURL;
    private Integer statusCode;
    private Map<String, List<String>> headers;
    private String pageContent;

    // any extra data that can be collected
    private Map<PageAttribute, Object> attributes;

    public static class Builder {
        private IndexInput building;

        public Builder() {
            this.building = new IndexInput();
            this.building.attributes = new HashMap<>();
        }

        public IndexInput build() {
            this.building.attributes = Collections.unmodifiableMap(this.building.attributes);
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
            this.building.headers = Collections.unmodifiableMap(headers);
            return this;
        }

        public Builder withPageContent(String pageContent) {
            this.building.pageContent = pageContent;
            return this;
        }

        public Builder withExistingAttributes(Map<PageAttribute, Object> attributes) {
            this.building.attributes.putAll(attributes);
            return this;
        }

        public Builder withAttribute(PageAttribute pageAttribute, Object value) {
            this.building.attributes.put(pageAttribute, value);
            return this;
        }
    }

    // for the builder
    private IndexInput() { }

    @JsonCreator
    private IndexInput(
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
