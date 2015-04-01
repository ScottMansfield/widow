package com.widowcrawler.core.worker;

/**
 * @author Scott Mansfield
 */
public class ExitWorkerProvider extends WorkerProvider {

    public static final Worker EXIT_SIGNAL = new Worker() {
        @Override
        protected boolean doWork() {
            return true;
        }
    };

    @Override
    public Worker get() {
        return EXIT_SIGNAL;
    }
}
