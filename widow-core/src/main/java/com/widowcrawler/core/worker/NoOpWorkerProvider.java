package com.widowcrawler.core.worker;

import com.widowcrawler.core.Worker;

/**
 * @author Scott Mansfield
 */
public class NoOpWorkerProvider {

    private class NoOpWorker implements Worker {
        @Override
        public void run() {
            // do nothing
        }
    }

    public Worker get() {
        return new NoOpWorker();
    }
}
