package com.widowcrawler.core.worker;

import org.junit.Test;

import java.util.function.BooleanSupplier;

import static org.easymock.EasyMock.*;

/**
 * @author Scott Mansfield
 */
public class WorkerTest {

    private Worker workerUnderTest;
    private BooleanSupplier booleanSupplierMock;

    @Test
    public void run_workSucceeded_callbackCalled() {

        // Arrange
        workerUnderTest = new Worker() {
            @Override
            protected boolean doWork() {
                return true;
            }
        };

        booleanSupplierMock = createMock(BooleanSupplier.class);
        expect(booleanSupplierMock.getAsBoolean()).andReturn(true).once();
        replay(booleanSupplierMock);

        workerUnderTest.withCallback(booleanSupplierMock);

        // Act
        workerUnderTest.run();

        // Assert
        verify(booleanSupplierMock);
    }

    @Test
    public void run_workFailed_callbackNotCalled() {

        // Arrange
        workerUnderTest = new Worker() {
            @Override
            protected boolean doWork() {
                return false;
            }
        };

        booleanSupplierMock = createMock(BooleanSupplier.class);
        replay(booleanSupplierMock);

        workerUnderTest.withCallback(booleanSupplierMock);

        // Act
        workerUnderTest.run();

        // Assert
        verify(booleanSupplierMock);
    }
}
