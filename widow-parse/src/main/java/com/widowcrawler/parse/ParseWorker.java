package com.widowcrawler.parse;

import com.widowcrawler.core.worker.Worker;
import com.widowcrawler.core.model.ParseInput;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @author Scott Mansfield
 */
public class ParseWorker extends Worker {

    private ParseInput parseInput;

    public ParseWorker withInput(ParseInput input) {
        this.parseInput = input;
        return this;
    }

    @Override
    public void doWork() {
        Document document = Jsoup.parse(this.parseInput.getPageContent());

        String title = document.title();
        double loadTimeMilliseconds = parseInput.getLoadTimeMillis();
        int responseSizeBytes = parseInput.getResponseSizeBytes();
        int pageContentSize = parseInput.getPageContent().length();

        // get links
        // get assets && calculate total page size
    }
}
