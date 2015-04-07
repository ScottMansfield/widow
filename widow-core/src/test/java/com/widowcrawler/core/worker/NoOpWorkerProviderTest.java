package com.widowcrawler.core.worker;

import org.junit.Test;

/**
 * @author Scott Mansfield
 */
public class NoOpWorkerProviderTest {

    @Test
    public void testNoOpProviderAndStaticExitSignalWorker() {
        Worker worker = new NoOpWorkerProvider().get();
        worker.run();
    }
}
