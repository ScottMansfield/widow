package com.widowcrawler.core.worker;

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
