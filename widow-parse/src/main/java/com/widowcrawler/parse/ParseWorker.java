package com.widowcrawler.parse;

import com.widowcrawler.core.worker.QueueCleanupCallback;
import com.widowcrawler.core.worker.Worker;
import com.widowcrawler.parse.model.ParseInput;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.function.BooleanSupplier;

/**
 * @author Scott Mansfield
 */
public class ParseWorker extends Worker {

    private ParseInput parseInput;

    public ParseWorker(ParseInput parseInput, BooleanSupplier callback) {
        super(callback);
        this.parseInput = parseInput;
    }

    @Override
    public void doWork() {
        // magic!

        Document document = Jsoup.parse(this.parseInput.getPageContent());

        String title = document.title();
        double loadTimeMilliseconds = parseInput.getLoadTimeMillis();
        int responseSizeBytes = parseInput.getResponseSizeBytes();
        int pageContentSize = parseInput.getPageContent().length();
    }
}
