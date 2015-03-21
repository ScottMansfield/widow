package com.widowcrawler.parse;

import com.widowcrawler.core.Worker;

/**
 * @author Scott Mansfield
 */
public class ParseWorker implements Worker {

    // some sort of message needs to be input in the constructor
    private String htmlContent;

    public ParseWorker(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    @Override
    public void run() {
        // magic
    }
}
