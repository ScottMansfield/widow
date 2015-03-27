package com.widowcrawler.core.dispatch;

import com.netflix.governator.annotations.AutoBindSingleton;
import com.widowcrawler.core.worker.ExitWorkerProvider;
import com.widowcrawler.core.worker.Worker;
import com.widowcrawler.core.worker.WorkerProvider;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;

/**
 * @author Scott Mansfield
 */
@AutoBindSingleton
public class Dispatcher {
    // pull from queue, send to executor
    // the specific worker runnable will be dictated by the injection config
    // The Worker interface implementation will dictate what work gets done

    @Inject
    WorkerProvider workerProvider;

    @Inject
    ExecutorService executor;

    public boolean dispatch() {
        Worker worker = workerProvider.get();

        if (worker == ExitWorkerProvider.EXIT_SIGNAL) {
            return false;
        }

        executor.submit(worker);

        return true;
    }
}
