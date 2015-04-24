package com.widowcrawler.core.dispatch;

import com.widowcrawler.core.worker.ExitWorkerProvider;
import com.widowcrawler.core.worker.Worker;
import com.widowcrawler.core.worker.WorkerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Scott Mansfield
 */
public class Dispatcher {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // pull from queue, send to executor
    // the specific worker runnable will be dictated by the injection config
    // The Worker interface implementation will dictate what work gets done

    @Inject
    WorkerProvider workerProvider;

    @Inject
    ThreadPoolExecutor executor;

    public boolean dispatch() throws InterruptedException {
        logger.info("Pre workerProvider.get()");
        Worker worker = workerProvider.get();
        logger.info("Post workerProvider.get()");

        if (worker == ExitWorkerProvider.EXIT_SIGNAL) {
            return false;
        }

        logger.info("About to submit work to executor");
        executor.getQueue().put(worker);
        logger.info("Submitted work");

        return true;
    }
}
