package com.widowcrawler.core;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;

/**
 * @author Scott Mansfield
 */
@Singleton
public class Dispatcher {
    // pull from queue, send to executor
    // the specific worker runnable will be dictated by the injection config
    // The Worker interface implementation will dictate what work gets done

    @Inject
    Provider<Worker> workerProvider;

    @Inject
    ExecutorService executor;

    public void dispatch() {
        executor.submit(workerProvider.get());
    }
}
