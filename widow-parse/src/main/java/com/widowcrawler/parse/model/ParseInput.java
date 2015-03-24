package com.widowcrawler.parse.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Scott Mansfield
 */
public class ParseInput {

    // TODO: still probably needs a lot more fields

    // basic content
    private String pageContent;
    private Map<String, List<String>> headers;
    private Integer statusCode;

    // any extra data that can be collected
    private Locale locale;
    private DateTime timeAccessed;
    private Double loadTimeMillis;
    private Integer responseSizeBytes;

    public static class Builder {
        private ParseInput building;

        public Builder() {
            this.building = new ParseInput();
        }

        public ParseInput build() {
            return this.building;
        }

        public Builder withPageContent(String pageContent) {
            this.building.pageContent = pageContent;
            return this;
        }

        public Builder withHeaders(Map<String, List<String>> headers) {
            this.building.headers = headers;
            return this;
        }

        public Builder withStatusCode(Integer statusCode) {
            this.building.statusCode = statusCode;
            return this;
        }

        public Builder withLocale(Locale locale) {
            this.building.locale = locale;
            return this;
        }

        public Builder withTimeAccessed(DateTime timeAccessed) {
            this.building.timeAccessed = timeAccessed;
            return this;
        }

        public Builder withLoadTimeMillis(Double loadTimeMillis) {
            this.building.loadTimeMillis = loadTimeMillis;
            return this;
        }

        public Builder withResponseSizeBytes(Integer responseSizeBytes) {
            this.building.responseSizeBytes = responseSizeBytes;
            return this;
        }
    }

    // no-arg constructor for the builder
    private ParseInput() { }

    // save me from my OCD
    @JsonCreator
    private ParseInput(
            @JsonProperty("pageContent")       String pageContent,
            @JsonProperty("headers")           Map<String, List<String>> headers,
            @JsonProperty("status")            Integer statusCode,
            @JsonProperty("locale")            Locale locale,
            @JsonProperty("timeAccessed")      DateTime timeAccessed,
            @JsonProperty("loadTimeMillis")    Double loadTimeMillis,
            @JsonProperty("responseSizeBytes") Integer responseSizeBytes) {
        this.pageContent = pageContent;
        this.headers = headers;
        this.statusCode = statusCode;
        this.timeAccessed = timeAccessed;
        this.loadTimeMillis = loadTimeMillis;
        this.responseSizeBytes = responseSizeBytes;
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

    public Locale getLocale() {
        return locale;
    }

    public DateTime getTimeAccessed() {
        return timeAccessed;
    }

    public Double getLoadTimeMillis() {
        return loadTimeMillis;
    }

    public Integer getResponseSizeBytes() {
        return responseSizeBytes;
    }
}
