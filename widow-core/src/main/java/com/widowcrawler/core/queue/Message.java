package com.widowcrawler.core.queue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Scott Mansfield
 */
public class Message {
    private String body;

    @JsonCreator
    public Message(
            @JsonProperty("body") String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }
}
