package com.widowcrawler.core.worker;

import org.junit.Test;

/**
 * @author Scott Mansfield
 */
public class ExitWorkerProviderTest {

    @Test
    public void testExitProviderAndStaticExitSignalWorker() {
        Worker worker = new ExitWorkerProvider().get();
        worker.run();
    }
}
