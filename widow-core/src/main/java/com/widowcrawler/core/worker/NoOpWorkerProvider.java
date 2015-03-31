package com.widowcrawler.core.worker;

import java.util.function.BooleanSupplier;

/**
 * @author Scott Mansfield
 */
public class NoOpWorkerProvider extends WorkerProvider {

    // Worker takes a BooleanSupplier that is also no-op
    private static final Worker noOp = new Worker(() -> true) {
        @Override
        protected void doWork() {
            //no-op
        }
    };

    public Worker get() {
        return noOp;
    }
}
