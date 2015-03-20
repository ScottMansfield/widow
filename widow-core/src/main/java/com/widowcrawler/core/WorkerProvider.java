package com.widowcrawler.core;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * @author Scott Mansfield
 */
public class WorkerProvider implements Provider<Worker> {

    // @Inject
    // inject something wrapping the sqs client

    @Override
    public Worker get() {
        // return a worker wrapping the message and raring to do some work
        return null;
    }
}
