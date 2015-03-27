package com.widowcrawler.core.worker;

/**
 * @author Scott Mansfield
 */
public class NoOpWorkerProvider extends WorkerProvider {

    private static final Worker noOp = () -> { };

    public Worker get() {
        return noOp;
    }
}
