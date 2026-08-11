package Coocos.madnessCup.systems.managers;

import Coocos.madnessCup.systems.Queue;
import Coocos.madnessCup.utils.DirectoryManager;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Queue manager to associate a unique string to each queue
 */
public class QueueManager {
    private final Map<String, Queue> queues = new HashMap<>();

    // Whoever calls this method has to deal with the exception
    public void registerQueue(String name, Queue queue) {
        String worldName = name.toLowerCase();
        DirectoryManager.copyDimension(worldName);
        queues.put(worldName, queue);
        Bukkit.getScheduler().runTask(queue.getPlugin(), () -> {
            new WorldCreator(worldName).createWorld();
        });
    }

    public Queue getQueue(String name) {
        return queues.get(name.toLowerCase());
    }

    public void removeQueue(String name) {
        String worldName = name.toLowerCase();
        // Maps remove also returns the value it removed
        Queue queue = queues.remove(worldName);
        if (queue == null) return;
        org.bukkit.World world = Bukkit.getWorld(worldName);
        if (world != null) Bukkit.unloadWorld(world, false);
        DirectoryManager.deleteDimension(worldName);
    }

    public Collection<Queue> getAllQueues() {
        // Maps return Collections
        return queues.values();
    }

    public void removeAllQueues() {
        for (String worldName : new ArrayList<>(queues.keySet())) removeQueue(worldName);
        queues.clear();
    }
}
