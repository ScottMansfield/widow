package com.widowcrawler.core.worker;

/**
 * @author Scott Mansfield
 */
public class NoOpWorkerProvider extends WorkerProvider {

    private static final Worker noOp = new Worker() {
        @Override
        protected boolean doWork() {
            return false;
        }
    };

    public Worker get() {
        return noOp;
    }
}
