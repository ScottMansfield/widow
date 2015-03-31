package com.widowcrawler.core.worker;

/**
 * @author Scott Mansfield
 */
public class ExitWorkerProvider extends WorkerProvider {

    public static final Worker EXIT_SIGNAL = new Worker(() -> true) {
        @Override
        protected void doWork() {
            //no-op
        }
    };

    @Override
    public Worker get() {
        return EXIT_SIGNAL;
    }
}
