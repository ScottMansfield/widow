package com.widowcrawler.parse;

import com.widowcrawler.core.worker.Worker;
import com.widowcrawler.parse.model.ParseInput;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @author Scott Mansfield
 */
public class ParseWorker implements Worker {

    private ParseInput parseInput;

    public ParseWorker(ParseInput parseInput) {
        this.parseInput = parseInput;
    }

    @Override
    public void run() {
        // magic!

        Document document = Jsoup.parse(this.parseInput.getPageContent());

        String title = document.title();
        double loadTimeMilliseconds = parseInput.getLoadTimeMillis();
        int responseSizeBytes = parseInput.getResponseSizeBytes();
        int pageContentSize = parseInput.getPageContent().length();
    }
}
