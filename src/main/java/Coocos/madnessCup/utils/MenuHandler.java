package Coocos.madnessCup.utils;

import Coocos.madnessCup.MadnessCup;
import Coocos.madnessCup.systems.Queue;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * MenuHandler class to prevent players from moving stuff around in their
 * inventory and to manage the necessary papers with commands
 */
public class MenuHandler implements Listener {
    private final MadnessCup plugin;

    public MenuHandler(MadnessCup plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player && plugin.isAdmin(player)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        if (plugin.isAdmin(event.getPlayer())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        defaultInventory(player);
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        if (plugin.isAdmin(event.getPlayer())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        handleMenuItem(event.getPlayer(), event.getItem());
        if (plugin.isAdmin(event.getPlayer())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        handleMenuItem((Player) event.getWhoClicked(), clicked);
        if (event.getWhoClicked() instanceof Player player && plugin.isAdmin(player)) return;
        event.setCancelled(true);
    }

    private void handleMenuItem(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String key = meta.getPersistentDataContainer().get(
                ItemFactory.KEY_MENU,
                PersistentDataType.STRING);

        if (key == null) return;

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);

        switch (key) {
            case "play":
                openQueueInventory(player);
                break;

            case "join":
                String queueName = ChatColor.stripColor(meta.getLore().get(0));
                Queue queue = plugin.getQueueManager().getQueue(queueName);

                if (!joinQueue(queue, player)) return;
                queue.addPlayer(player.getUniqueId());
                queueStartInventory(player);
                player.closeInventory();
                break;

            case "leave":
                //check in which queue a player is
                ArrayList<Queue> queues = new ArrayList<>(plugin.getQueueManager().getAllQueues());
                for (Queue q : queues) {
                    if (q.getPlayers().contains(player.getUniqueId()))
                        q.removePlayer(player.getUniqueId());
                }
                defaultInventory(player);
                player.closeInventory();
                break;

            default:
                break;
        }
    }

    public void defaultInventory(Player player) {
        Inventory inv = player.getInventory();
        inv.clear();
        inv.setItem(8, ItemFactory.createPaper("Play","Check out what games are available", "play"));
    }

    public void openQueueInventory(Player player) {
        ArrayList<Queue> queues = new ArrayList<>(plugin.getQueueManager().getAllQueues());
        Inventory inv = Bukkit.createInventory(null, 27,"Reincarnation Battle Queues");
        int i = 0;
        for (Queue queue : queues) {
            inv.setItem(i, ItemFactory.createPaper("Join Queue",queue.getQueueName(), "join"));
            i+=1;
        }
        player.openInventory(inv);
    }

    public void queueStartInventory(Player player) {
        Inventory inv = player.getInventory();
        inv.clear();
        inv.setItem(4, ItemFactory.createPaper("Leave Queue","Press to leave the queue", "leave"));
    }

    public boolean joinQueue(Queue queue, Player player) {
        if (queue == null) {
            player.sendMessage(ChatColor.RED + "That queue doesn't exist.");
            return false;
        }
        // If there is a queue then check if the player is already in it
        List<UUID> playersInQueue = queue.getPlayers();
        if (playersInQueue.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are already in the queue.");
            return false;
        }
        return true;
    }
}
