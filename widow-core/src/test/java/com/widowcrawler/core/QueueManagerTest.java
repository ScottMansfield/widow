package com.widowcrawler.core;

import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.widowcrawler.core.queue.QueueManager;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertNull;
import static org.powermock.api.easymock.PowerMock.*;

/**
 * @author Scott Mansfield
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({System.class, QueueManager.class, QueueManagerTest.class})
public class QueueManagerTest {

    private QueueManager queueManager;

    @Before
    public void before() {
        this.queueManager = new QueueManager();
    }

    @Test
    public void constructor_queuePropertyExistsAndQueuesExist_messagesMapCreated() throws Exception {

        //// ARRANGE
        mockStatic(System.class);
        expect(System.getProperty("com.widowcrawler.queues")).andReturn("foo           | bar |baz|||||").once();

        AmazonSQSAsyncClient sqsAsyncClient = createMock(AmazonSQSAsyncClient.class);

        for (String queueName : new String[] { "foo", "bar", "baz"}) {
            expect(sqsAsyncClient.getQueueUrl(queueName))
                    .andReturn(new GetQueueUrlResult().withQueueUrl(queueName + ".queueUrl")).once();
        }

        FieldUtils.writeField(this.queueManager, "sqsClient", sqsAsyncClient, true);

        replay(System.class);
        replay(sqsAsyncClient);

        //// ACT
        this.queueManager.postConstruct();

        //// ASSERT
        assertNull(this.queueManager.nextMessage("foo"));
        assertNull(this.queueManager.nextMessage("bar"));
        assertNull(this.queueManager.nextMessage("baz"));

        verify(System.class);
        verify(sqsAsyncClient);
    }
}
