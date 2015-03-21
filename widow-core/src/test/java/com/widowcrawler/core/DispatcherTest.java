package com.widowcrawler.core;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import javax.inject.Provider;
import java.util.concurrent.ExecutorService;

/**
 * @author Scott Mansfield
 */
public class DispatcherTest {

    private Dispatcher dispatcher;
    private Provider<Worker> workerProviderMock;
    private ExecutorService executorServiceMock;

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws Exception {
        this.dispatcher = new Dispatcher();

        workerProviderMock = createMock(Provider.class);
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
        this.dispatcher.dispatch();

        // Assert
        verify(workerMock, workerProviderMock, executorServiceMock);
    }
}
