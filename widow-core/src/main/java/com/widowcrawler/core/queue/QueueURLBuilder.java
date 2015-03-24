package com.widowcrawler.core.queue;

import javax.inject.Inject;

/**
 * @author Scott Mansfield
 */
public class QueueURLBuilder {

    // Example URL:
    // http://sqs.us-east-1.amazonaws.com/123456789012/queue2

    // perhaps on startup we need to pull down the queue URL list
    // and lookup the fetch, parse, index, and analyze queues
    // based on config

    @Inject
    String accountNumber;

    public String build() {
        // TODO: Build URL (ha)
        return "";
    }
}
