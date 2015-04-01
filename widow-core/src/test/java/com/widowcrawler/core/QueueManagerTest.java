package com.widowcrawler.core;

import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.widowcrawler.core.queue.QueueManager;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertNull;
import static org.powermock.api.easymock.PowerMock.*;

/**
 * @author Scott Mansfield
 */
@RunWith(PowerMockRunner.class)
//@PowerMockRunnerDelegate(Parameterized.class)
@PrepareForTest({System.class, QueueManager.class, QueueManagerTest.class})
public class QueueManagerTest {

    private static final String QUEUES_PROP = "com.widowcrawler.queues";

    private QueueManager queueManager;

    @Before
    public void before() {
        this.queueManager = new QueueManager();

        mockStatic(System.class);
    }

    @Test
    public void constructor_queuePropertyExistsAndQueuesExist_messagesMapCreated() throws Exception {

        //// ARRANGE
        expect(System.getProperty(QUEUES_PROP)).andReturn("foo           | bar |baz|||||").once();

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

    @Test(expected = NullPointerException.class)
    public void constructor_queuePropertyIsNull_NullPointerExceptionThrown() throws Exception {

        //// ARRANGE
        expect(System.getProperty(QUEUES_PROP)).andReturn(null).once();

        replay(System.class);

        //// ACT
        this.queueManager.postConstruct();

        //// ASSERT
        // nothing to assert, expecting exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_queuePropertyIsEmpty_IllegalArgumentExceptionThrown() throws Exception {

        //// ARRANGE
        expect(System.getProperty(QUEUES_PROP)).andReturn("").once();

        replay(System.class);

        //// ACT
        this.queueManager.postConstruct();

        //// ASSERT
        // nothing to assert, expecting exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_queuePropertyIsWhitespace_IllegalArgumentExceptionThrown() throws Exception {

        //// ARRANGE
        expect(System.getProperty(QUEUES_PROP)).andReturn("      ").once();

        replay(System.class);

        //// ACT
        this.queueManager.postConstruct();

        //// ASSERT
        // nothing to assert, expecting exception
    }
}
