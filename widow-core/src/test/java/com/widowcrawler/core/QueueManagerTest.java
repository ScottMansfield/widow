package com.widowcrawler.core;

import static org.junit.Assert.*;

import com.widowcrawler.core.queue.QueueManager;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Scott Mansfield
 */
public class QueueManagerTest {

    private QueueManager queueManager;

    @Before
    public void before() {
        this.queueManager = new QueueManager();
    }

    @Test
    public void constructor_queuePropertyExists_messagesMapCreated() {

        // ARRAGE
        System.setProperty("com.widowcrawler.queues", "foo           | bar |baz|||||");

        // ACT
        this.queueManager = new QueueManager();

        // ASSERT
        assertNull(this.queueManager.nextMessage("foo"));
        assertNull(this.queueManager.nextMessage("bar"));
        assertNull(this.queueManager.nextMessage("baz"));
    }
}
