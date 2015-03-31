package com.widowcrawler.core.worker;

import java.util.function.BooleanSupplier;

/**
 * @author Scott Mansfield
 */
public abstract class Worker implements Runnable {
    // so far, just a marker interface for DI type resolution

    private BooleanSupplier callback;

    protected Worker(BooleanSupplier callback) {
        this.callback = callback;
    }

    @Override
    public void run() {
        doWork();

        // this is a bit wonky, but there's no way to just
        // have a plain callback
        callback.getAsBoolean();
    }

    protected abstract void doWork();
}
