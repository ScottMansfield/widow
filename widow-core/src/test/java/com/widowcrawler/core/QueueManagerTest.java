package com.widowcrawler.core;

import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiptHandleIsInvalidException;
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

    private static final String QUEUES_PROP = "com.widowcrawler.queues";

    private QueueManager queueManager;
    private AmazonSQSAsyncClient sqsAsyncClientMock;
    private ExecutorService executorServiceMock;

    @Before
    public void before() {
        this.queueManager = new QueueManager();
        System.clearProperty(QUEUES_PROP);
    }

    @Test
    public void postConstruct_queuePropertyExistsAndQueuesExist_messagesMapCreated() throws Exception {

        // ARRANGE
        setupStandardQueuesPropsAndClient();
        setupExecutorService();

        replay(sqsAsyncClientMock, executorServiceMock);

        // ACT
        this.queueManager.postConstruct();

        // ASSERT
        assertNull(this.queueManager.nextMessage("foo"));
        assertNull(this.queueManager.nextMessage("bar"));
        assertNull(this.queueManager.nextMessage("baz"));

        verify(sqsAsyncClientMock, executorServiceMock);
    }

    @Test(expected = NullPointerException.class)
    public void postConstruct_queuePropertyIsNull_NullPointerExceptionThrown() {

        // ARRANGE
        System.clearProperty(QUEUES_PROP);

        // ACT
        this.queueManager.postConstruct();

        // ASSERT
        // nothing to assert, expecting exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void postConstruct_queuePropertyIsEmpty_IllegalArgumentExceptionThrown() {

        // ARRANGE
        System.setProperty(QUEUES_PROP, "");

        // ACT
        this.queueManager.postConstruct();

        // ASSERT
        // nothing to assert, expecting exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void postConstruct_queuePropertyIsWhitespace_IllegalArgumentExceptionThrown() {

        // ARRANGE
        System.setProperty(QUEUES_PROP, "      ");

        // ACT
        this.queueManager.postConstruct();

        // ASSERT
        // nothing to assert, expecting exception
    }

    @Test
    public void postConstruct_queuePropertyContainsNonExistentQueue_createsQueue() throws Exception {

        // ARRANGE
        System.setProperty(QUEUES_PROP, "foo | bar | baz");

        sqsAsyncClientMock = createMock(AmazonSQSAsyncClient.class);

        for (String queueName : new String[] { "foo", "bar" }) {
            expect(sqsAsyncClientMock.getQueueUrl(queueName))
                    .andReturn(new GetQueueUrlResult().withQueueUrl(queueName + ".queueUrl")).once();
        }

        expect(sqsAsyncClientMock.getQueueUrl("baz")).andThrow(new QueueDoesNotExistException("lol")).once();
        expect(sqsAsyncClientMock.createQueue("baz")).andReturn(new CreateQueueResult().withQueueUrl("baz.queueurl")).once();

        FieldUtils.writeField(this.queueManager, "sqsClient", sqsAsyncClientMock, true);

        setupExecutorService();

        replay(sqsAsyncClientMock, executorServiceMock);

        // ACT
        this.queueManager.postConstruct();

        // ASSERT
        assertNull(this.queueManager.nextMessage("foo"));
        assertNull(this.queueManager.nextMessage("bar"));
        assertNull(this.queueManager.nextMessage("baz"));

        verify(sqsAsyncClientMock, executorServiceMock);
    }

    /*
    @Test
    public void nextMessage_queueExists_returnsNextMessage() throws Exception {

        // ARRANGE
        System.setProperty(QUEUES_PROP, "foo | bar | baz");

        sqsAsyncClientMock = createMock(AmazonSQSAsyncClient.class);

        for (String queueName : new String[] { "foo", "bar", "baz"}) {
            expect(sqsAsyncClientMock.getQueueUrl(queueName))
                    .andReturn(new GetQueueUrlResult().withQueueUrl(queueName + ".queueUrl")).once();
        }

        expect(sqsAsyncClientMock.receiveMessage(anyObject(ReceiveMessageRequest.class)))
                .andReturn(new ReceiveMessageResult().withMessages(new Message().withBody("foo")));

        FieldUtils.writeField(this.queueManager, "sqsClient", sqsAsyncClientMock, true);

        setupExecutorService();

        replay(sqsAsyncClientMock, executorServiceMock);

        // ACT
        this.queueManager.postConstruct();
        Thread.sleep(500);

        // ASSERT
        assertNull(this.queueManager.nextMessage("foo"));
        assertNull(this.queueManager.nextMessage("bar"));
        assertNull(this.queueManager.nextMessage("baz"));

        verify(sqsAsyncClientMock, executorServiceMock);
    }*/

    @Test(expected = IllegalArgumentException.class)
    public void nextMessage_queueDoesNotExist_IllegalArgumentExceptionThrown() throws Exception {

        // ARRANGE
        setupStandardQueuesPropsAndClient();
        setupExecutorService();

        replay(sqsAsyncClientMock, executorServiceMock);

        // ACT
        this.queueManager.postConstruct();
        this.queueManager.nextMessage("lol");

        // ASSERT
        // nothing to assert, expecting exception
    }

    @Test
    public void confirmReceipt_queueExistsValidHandle_messageDeleted() throws Exception {

        // ARRANGE
        setupStandardQueuesPropsAndClient();
        setupExecutorService();

        sqsAsyncClientMock.deleteMessage("foo.queueUrl", "foo_msg");
        expectLastCall().once();

        replay(sqsAsyncClientMock, executorServiceMock);

        // ACT
        this.queueManager.postConstruct();
        this.queueManager.confirmReceipt("foo", "foo_msg");

        // ASSERT
        verify(sqsAsyncClientMock, executorServiceMock);
    }

    @Test(expected = ReceiptHandleIsInvalidException.class)
    public void confirmReceipt_queueExistsInvalidHandle_ReceiptHandleIsInvalidExceptionThrown() throws Exception {

        // ARRANGE
        setupStandardQueuesPropsAndClient();
        setupExecutorService();

        sqsAsyncClientMock.deleteMessage("foo.queueUrl", "foo_msg");
        expectLastCall().andThrow(new ReceiptHandleIsInvalidException("invalid")).once();

        replay(sqsAsyncClientMock, executorServiceMock);

        // ACT
        this.queueManager.postConstruct();
        this.queueManager.confirmReceipt("foo", "foo_msg");

        // ASSERT
        // nothing to assert, expecting exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void confirmReceipt_queueDoesNotExist_IllegalArgumentExceptionThrown() throws Exception {

        // ARRANGE
        setupStandardQueuesPropsAndClient();
        setupExecutorService();

        sqsAsyncClientMock.deleteMessage("foo.queueUrl", "foo_msg");
        expectLastCall().andThrow(new QueueDoesNotExistException("nope")).once();

        replay(sqsAsyncClientMock, executorServiceMock);

        // ACT
        this.queueManager.postConstruct();
        this.queueManager.confirmReceipt("lol", "foo_msg");

        // ASSERT
        // nothing to assert, expecting exception
    }


    //////////////////
    // Helper Methods
    //////////////////
    private void setupStandardQueuesPropsAndClient() throws Exception {
        System.setProperty(QUEUES_PROP, "foo | bar | baz");

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
