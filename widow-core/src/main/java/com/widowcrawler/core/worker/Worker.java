package com.widowcrawler.core.worker;

import java.util.function.BooleanSupplier;

/**
 * @author Scott Mansfield
 */
public abstract class Worker implements Runnable {
    // so far, just a marker interface for DI type resolution

    private BooleanSupplier callback;

    /**
     * Sets the queue cleanup calback for this worker. Must be set by creator.
     *
     * @param callback the {@link java.util.function.BooleanSupplier} to run after the work is done
     */
    public Worker withCallback(BooleanSupplier callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public void run() {
        if (doWork()) {
            // this is a bit wonky, but there's no way to just
            // have a plain callback
            callback.getAsBoolean();
        }
    }

    // TODO: return boolean for success
    protected abstract boolean doWork();
}
