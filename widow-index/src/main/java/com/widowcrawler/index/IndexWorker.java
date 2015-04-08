package com.widowcrawler.index;

import com.widowcrawler.core.model.IndexInput;
import com.widowcrawler.core.worker.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Scott Mansfield
 */
public class IndexWorker extends Worker {

    private static final Logger logger = LoggerFactory.getLogger(IndexWorker.class);

    private IndexInput indexInput;

    public IndexWorker withInput(IndexInput indexInput) {
        this.indexInput = indexInput;
        return this;
    }

    @Override
    protected boolean doWork() {
        logger.info("Received IndexInput:" + indexInput.getOriginalURL());

        return true;
    }
}
