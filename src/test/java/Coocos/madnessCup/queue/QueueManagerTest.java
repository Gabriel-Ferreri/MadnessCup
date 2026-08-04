package Coocos.madnessCup.queue;

import Coocos.madnessCup.MadnessCup;
import Coocos.madnessCup.queue.other.QueueManager;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class QueueManagerTest {

    @Test
    void registerAndGetQueue() {
        QueueManager manager = new QueueManager();

        Queue queue = null;

        manager.registerQueue("reincarnation1", queue);

        assertSame(queue, manager.getQueue("reincarnation1"));
    }


    @Test
    void queueNamesAreCaseInsensitive() {
        QueueManager manager = new QueueManager();

        Queue queue = null;

        manager.registerQueue("Reincarnation1", queue);

        assertSame(queue, manager.getQueue("reincarnation1"));
        assertSame(queue, manager.getQueue("REINCARNATION1"));
    }


    @Test
    void removeQueueWorks() {
        QueueManager manager = new QueueManager();

        Queue queue = null;

        manager.registerQueue("reincarnation1", queue);

        manager.removeQueue("reincarnation1");

        assertNull(manager.getQueue("reincarnation1"));
    }


    @Test
    void missingQueueReturnsNull() {
        QueueManager manager = new QueueManager();

        assertNull(manager.getQueue("does_not_exist"));
    }
}