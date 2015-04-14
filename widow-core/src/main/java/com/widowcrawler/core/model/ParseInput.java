package com.widowcrawler.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Scott Mansfield
 */
public class ParseInput {

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

        public Builder withAttribute(PageAttribute pageAttribute, Object value) {
            this.building.attributes.put(pageAttribute, value);
            return this;
        }
    }

    // no-arg constructor for the builder
    private ParseInput() { }

    @JsonCreator
    private ParseInput(@JsonProperty("attributes") Map<PageAttribute, Object> attributes) {
        this.attributes = Collections.unmodifiableMap(attributes);
    }

    public Map<PageAttribute, Object> getAttributes() {
        return attributes;
    }

    public Object getAttribute(PageAttribute pageAttribute) {
        return this.attributes.get(pageAttribute);
    }
}
