package com.widowcrawler.core.queue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Scott Mansfield
 */
public class Message {
    private String body;
    private String messageID;
    private String receiptHandle;

    @JsonCreator
    public Message(
            @JsonProperty("body") String body,
            @JsonProperty("messageID") String messageID,
            @JsonProperty("receiptHandle") String receiptHandle) {
        this.body = body;
        this.messageID = messageID;
        this.receiptHandle = receiptHandle;
    }

    public String getBody() {
        return body;
    }

    public String getMessageID() {
        return messageID;
    }

    public String getReceiptHandle() {
        return receiptHandle;
    }
}
