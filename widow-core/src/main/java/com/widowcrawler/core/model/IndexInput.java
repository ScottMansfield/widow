package com.widowcrawler.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *@author Scott Mansfield
 */
public class IndexInput {

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
    private IndexInput(@JsonProperty("attributes") Map<PageAttribute, Object> attributes) {
        this.attributes = Collections.unmodifiableMap(attributes);
    }

    public Map<PageAttribute, Object> getAttributes() {
        return attributes;
    }

    public Object getAttribute(PageAttribute pageAttribute) {
        return this.attributes.get(pageAttribute);
    }
}
