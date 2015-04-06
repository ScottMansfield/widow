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
import java.util.concurrent.ExecutorService;

/**
 * @author Scott Mansfield
 */
public class DispatcherTest {

    private Dispatcher dispatcher;
    private WorkerProvider workerProviderMock;
    private ExecutorService executorServiceMock;

    @Before
    public void before() throws Exception {
        this.dispatcher = new Dispatcher();

        workerProviderMock = createMock(WorkerProvider.class);
        FieldUtils.writeField(this.dispatcher, "workerProvider", workerProviderMock, true);

        executorServiceMock = createMock(ExecutorService.class);
        FieldUtils.writeField(this.dispatcher, "executor", executorServiceMock, true);
    }

    @Test
    public void dispatch_workExists_workDispatched() {
        // Arrange
        Worker workerMock = createMock(Worker.class);

        expect(workerProviderMock.get()).andReturn(workerMock);
        expect(executorServiceMock.submit(eq(workerMock))).andReturn(null);

        replay(workerMock, workerProviderMock, executorServiceMock);

        // Act
        boolean retval = this.dispatcher.dispatch();

        // Assert
        verify(workerMock, workerProviderMock, executorServiceMock);
        assertTrue(retval);
    }

    @Test
    public void dispatch_exitWorkerProvided_dispatchExitsWithFalse() {
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
