package Coocos.madnessCup.systems.managers;

import Coocos.madnessCup.MadnessCup;
import Coocos.madnessCup.systems.Queue;
import Coocos.madnessCup.utils.DirectoryManager;
import Coocos.madnessCup.utils.MenuHandler;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Queue manager to associate a unique string to each queue
 */
public class QueueManager {
    private final Map<String, Queue> queues = new HashMap<>();
    private final MadnessCup plugin;

    public QueueManager(MadnessCup plugin) {
        this.plugin = plugin;
    }

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
        DirectoryManager.deleteDimension(worldName); //Comment this line to modify world

        // Of course this has to change when there's going to be different types of queues
        MenuHandler menuHandler = new MenuHandler(plugin);
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getOpenInventory().getTitle().equals("Reincarnation Battle Queues"))
                    menuHandler.openQueueInventory(player);
            }
        });
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
