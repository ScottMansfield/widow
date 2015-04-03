package com.widowcrawler.core.model;

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
            return this.building;
        }

        public Builder withExistingAttributes(Map<PageAttribute, Object> attributes) {
            if (this.building.attributes == null) {
                this.building.attributes = new HashMap<>(attributes);
            } else {
                this.building.attributes.putAll(attributes);
            }
            return this;
        }

        public Builder withAttribute(PageAttribute pageAttribute, Object value) {
            this.building.attributes.put(pageAttribute, value);
            return this;
        }
    }

}
