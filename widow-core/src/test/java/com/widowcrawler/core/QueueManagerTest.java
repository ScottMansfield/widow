package com.widowcrawler.core;

import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiptHandleIsInvalidException;
import com.netflix.archaius.Config;
import com.widowcrawler.core.queue.QueueManager;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertNull;

/**
 * @author Scott Mansfield
 */
public class QueueManagerTest {

    private QueueManager queueManager;
    private AmazonSQSAsyncClient sqsAsyncClientMock;
    private ExecutorService executorServiceMock;
    private Config configMock;

    @Before
    public void before() {
        System.out.println("before");
        this.queueManager = new QueueManager();
    }

    @Test
    public void postConstruct_queuePropertyExistsAndQueuesExist_messagesMapCreated() throws Exception {
        // ARRANGE
        setupStandard();
        replay(configMock, sqsAsyncClientMock, executorServiceMock);

        // ACT
        this.queueManager.postConstruct();

        // ASSERT
        verify(configMock, sqsAsyncClientMock, executorServiceMock);
    }

    @Test(expected = NullPointerException.class)
    public void postConstruct_queuePropertyIsNull_NullPointerExceptionThrown() throws Exception {
        System.out.println("postConstruct_queuePropertyIsNull_NullPointerExceptionThrown");

        // ARRANGE
        configMock = createMock(Config.class);
        expect(configMock.getString(QueueManager.QUEUE_NAMES_PROPERTY)).andReturn(null);
        FieldUtils.writeField(this.queueManager, "config", configMock, true);
        replay(configMock);

        // ACT
        this.queueManager.postConstruct();

        // ASSERT
        // nothing to assert, expecting exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void postConstruct_queuePropertyIsEmpty_IllegalArgumentExceptionThrown() throws Exception {
        System.out.println("postConstruct_queuePropertyIsEmpty_IllegalArgumentExceptionThrown");

        // ARRANGE
        configMock = createMock(Config.class);
        expect(configMock.getString(QueueManager.QUEUE_NAMES_PROPERTY)).andReturn("");
        FieldUtils.writeField(this.queueManager, "config", configMock, true);
        replay(configMock);

        // ACT
        this.queueManager.postConstruct();

        // ASSERT
        // nothing to assert, expecting exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void postConstruct_queuePropertyIsWhitespace_IllegalArgumentExceptionThrown() throws Exception {
        System.out.println("postConstruct_queuePropertyIsWhitespace_IllegalArgumentExceptionThrown");

        // ARRANGE
        configMock = createMock(Config.class);
        expect(configMock.getString(QueueManager.QUEUE_NAMES_PROPERTY)).andReturn("        ");
        FieldUtils.writeField(this.queueManager, "config", configMock, true);
        replay(configMock);

        // ACT
        this.queueManager.postConstruct();

        // ASSERT
        // nothing to assert, expecting exception
    }

    @Test
    public void postConstruct_queuePropertyContainsNonExistentQueue_createsQueue() throws Exception {
        System.out.println("postConstruct_queuePropertyContainsNonExistentQueue_createsQueue");

        // ARRANGE
        setupConfig();

        sqsAsyncClientMock = createMock(AmazonSQSAsyncClient.class);

        for (String queueName : new String[] { "foo", "bar" }) {
            expect(sqsAsyncClientMock.getQueueUrl(queueName))
                    .andReturn(new GetQueueUrlResult().withQueueUrl(queueName + ".queueUrl")).once();
        }

        expect(sqsAsyncClientMock.getQueueUrl("baz")).andThrow(new QueueDoesNotExistException("lol")).once();
        expect(sqsAsyncClientMock.createQueue("baz")).andReturn(new CreateQueueResult().withQueueUrl("baz.queueurl")).once();

        FieldUtils.writeField(this.queueManager, "sqsClient", sqsAsyncClientMock, true);

        setupExecutorService();

        replay(configMock, sqsAsyncClientMock, executorServiceMock);

        // ACT
        this.queueManager.postConstruct();

        // ASSERT
        verify(configMock, sqsAsyncClientMock, executorServiceMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nextMessage_queueDoesNotExist_IllegalArgumentExceptionThrown() throws Exception {
        System.out.println("nextMessage_queueDoesNotExist_IllegalArgumentExceptionThrown");

        // ARRANGE
        setupStandard();

        replay(configMock, sqsAsyncClientMock, executorServiceMock);

        // ACT
        this.queueManager.postConstruct();
        this.queueManager.nextMessage("lol");

        // ASSERT
        // nothing to assert, expecting exception
    }

    @Test
    public void confirmReceipt_queueExistsValidHandle_messageDeleted() throws Exception {
        System.out.println("confirmReceipt_queueExistsValidHandle_messageDeleted");

        // ARRANGE
        setupStandard();

        sqsAsyncClientMock.deleteMessage("foo.queueUrl", "foo_msg");
        expectLastCall().once();

        replay(configMock, sqsAsyncClientMock, executorServiceMock);

        // ACT
        this.queueManager.postConstruct();
        this.queueManager.confirmReceipt("foo", "foo_msg");

        // ASSERT
        verify(configMock, sqsAsyncClientMock, executorServiceMock);
    }

    @Test(expected = ReceiptHandleIsInvalidException.class)
    public void confirmReceipt_queueExistsInvalidHandle_ReceiptHandleIsInvalidExceptionThrown() throws Exception {
        System.out.println("confirmReceipt_queueExistsInvalidHandle_ReceiptHandleIsInvalidExceptionThrown");

        // ARRANGE
        setupStandard();

        sqsAsyncClientMock.deleteMessage("foo.queueUrl", "foo_msg");
        expectLastCall().andThrow(new ReceiptHandleIsInvalidException("invalid")).once();

        replay(configMock, sqsAsyncClientMock, executorServiceMock);

        // ACT
        this.queueManager.postConstruct();
        this.queueManager.confirmReceipt("foo", "foo_msg");

        // ASSERT
        // nothing to assert, expecting exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void confirmReceipt_queueDoesNotExist_IllegalArgumentExceptionThrown() throws Exception {
        System.out.println("confirmReceipt_queueDoesNotExist_IllegalArgumentExceptionThrown");

        // ARRANGE
        setupStandard();

        sqsAsyncClientMock.deleteMessage("foo.queueUrl", "foo_msg");
        expectLastCall().andThrow(new QueueDoesNotExistException("nope")).once();

        replay(configMock, sqsAsyncClientMock, executorServiceMock);

        // ACT
        this.queueManager.postConstruct();
        this.queueManager.confirmReceipt("lol", "foo_msg");

        // ASSERT
        // nothing to assert, expecting exception
    }

    //////////////////
    // Helper Methods
    //////////////////
    private void setupStandard() throws Exception {
        setupConfig();
        setupStandardQueueClient();
        setupExecutorService();
    }

    private void setupConfig() throws Exception {
        configMock = createMock(Config.class);

        expect(configMock.getString(QueueManager.QUEUE_NAMES_PROPERTY))
                .andReturn("foo | bar | baz");

        FieldUtils.writeField(this.queueManager, "config", configMock, true);
    }

    private void setupStandardQueueClient() throws Exception {
        sqsAsyncClientMock = createMock(AmazonSQSAsyncClient.class);

        for (String queueName : new String[] { "foo", "bar", "baz"}) {
            expect(sqsAsyncClientMock.getQueueUrl(queueName))
                    .andReturn(new GetQueueUrlResult().withQueueUrl(queueName + ".queueUrl")).once();
        }

        FieldUtils.writeField(this.queueManager, "sqsClient", sqsAsyncClientMock, true);
    }

    @SuppressWarnings("unchecked")
    private void setupExecutorService() throws Exception {
        executorServiceMock = createMock(ExecutorService.class);

        expect(executorServiceMock.submit(anyObject(Runnable.class))).andReturn(null).anyTimes();

        FieldUtils.writeField(this.queueManager, "executorService", executorServiceMock, true);
    }
}
