package com.widowcrawler.core;

import com.widowcrawler.core.dispatch.Dispatcher;
import com.widowcrawler.core.worker.ExitWorkerProvider;
import com.widowcrawler.core.worker.Worker;
import com.widowcrawler.core.worker.WorkerProvider;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.inject.Provider;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Scott Mansfield
 */
@SuppressWarnings("unchecked")
public class DispatcherTest {

    private Dispatcher dispatcher;
    private WorkerProvider workerProviderMock;
    private ThreadPoolExecutor executorServiceMock;

    @Before
    public void before() throws Exception {
        this.dispatcher = new Dispatcher();

        workerProviderMock = createMock(WorkerProvider.class);
        FieldUtils.writeField(this.dispatcher, "workerProvider", workerProviderMock, true);

        executorServiceMock = createMock(ThreadPoolExecutor.class);
        FieldUtils.writeField(this.dispatcher, "executor", executorServiceMock, true);
    }

    @Test
    public void dispatch_workExists_workDispatched() throws Exception {
        // Arrange
        Worker workerMock = createMock(Worker.class);

        expect(workerProviderMock.get()).andReturn(workerMock);

        BlockingQueue<Runnable> queueMock = createMock(BlockingQueue.class);
        queueMock.put(anyObject(Runnable.class));
        expectLastCall().once();

        expect(executorServiceMock.getQueue()).andReturn(queueMock).once();

        replay(workerMock, workerProviderMock, executorServiceMock, queueMock);

        // Act
        boolean retval = this.dispatcher.dispatch();

        // Assert
        verify(workerMock, workerProviderMock, executorServiceMock, queueMock);
        assertTrue(retval);
    }

    @Test
    public void dispatch_exitWorkerProvided_dispatchExitsWithFalse() throws Exception {
        // Arrange
        expect(workerProviderMock.get()).andReturn(ExitWorkerProvider.EXIT_SIGNAL);

        replay(workerProviderMock, executorServiceMock);

        // Act
        boolean retval = dispatcher.dispatch();

        // Assert
        verify(workerProviderMock, executorServiceMock);
        assertFalse(retval);
    }
}
