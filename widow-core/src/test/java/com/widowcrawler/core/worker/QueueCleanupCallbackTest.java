package com.widowcrawler.core.worker;

import com.widowcrawler.core.queue.QueueManager;
import org.junit.Test;

import static org.easymock.EasyMock.*;

/**
 * @author Scott Mansfield
 */
public class QueueCleanupCallbackTest {

    @Test
    public void get_getCalled_queueMessageAcknowledged() {
        // Arrange
        QueueManager queueManagerMock = createMock(QueueManager.class);
        queueManagerMock.confirmReceipt("foo", "foo_msg");
        expectLastCall().once();
        replay(queueManagerMock);

        QueueCleanupCallback callback = new QueueCleanupCallback(queueManagerMock, "foo", "foo_msg");

        // Act
        callback.getAsBoolean();

        // Assert
        verify(queueManagerMock);
    }
}
