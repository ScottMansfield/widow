package com.widowcrawler.core.dispatch;

import com.netflix.governator.annotations.AutoBindSingleton;
import com.widowcrawler.core.worker.ExitWorkerProvider;
import com.widowcrawler.core.worker.Worker;
import com.widowcrawler.core.worker.WorkerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;

/**
 * @author Scott Mansfield
 */
@AutoBindSingleton
public class Dispatcher {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // pull from queue, send to executor
    // the specific worker runnable will be dictated by the injection config
    // The Worker interface implementation will dictate what work gets done

    @Inject
    WorkerProvider workerProvider;

    @Inject
    ExecutorService executor;

    public boolean dispatch() {
        logger.info("Pre workerProvider.get()");
        Worker worker = workerProvider.get();
        logger.info("Post workerProvider.get()");

        if (worker == ExitWorkerProvider.EXIT_SIGNAL) {
            return false;
        }

        executor.submit(worker);

        return true;
    }
}
