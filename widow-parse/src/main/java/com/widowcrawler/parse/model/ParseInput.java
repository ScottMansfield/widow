package com.widowcrawler.parse.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Scott Mansfield
 */
public class ParseInput {
    private String pageContent;

    @JsonCreator
    public ParseInput(@JsonProperty("pageContent") String pageContent) {
        this.pageContent = pageContent;
    }

    public String getPageContent() {
        return pageContent;
    }
}
