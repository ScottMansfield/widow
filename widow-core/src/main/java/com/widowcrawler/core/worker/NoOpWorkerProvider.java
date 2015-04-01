package com.widowcrawler.core.worker;

/**
 * @author Scott Mansfield
 */
public class NoOpWorkerProvider extends WorkerProvider {

    // Worker takes a BooleanSupplier that is also no-op
    private static final Worker noOp = new Worker() {
        // TODO: always return false since something went wrong to get here
        @Override
        protected void doWork() {
            //no-op
        }
    };

    public Worker get() {
        return noOp;
    }
}
