package Coocos.madnessCup.game.other;

import Coocos.madnessCup.game.Queue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Queue manager to associate a unique string to each queue
 */
public class QueueManager {
    private final Map<String, Queue> queues = new HashMap<>();

    public void registerQueue(String name, Queue queue) {
        queues.put(name.toLowerCase(), queue);
    }

    public Queue getQueue(String name) {
        return queues.get(name.toLowerCase());
    }

    public Collection<Queue> getAllQueues() {
        // Maps return Collections
        return queues.values();
    }
}
