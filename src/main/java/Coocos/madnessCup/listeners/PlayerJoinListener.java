package Coocos.madnessCup.listeners;

import Coocos.madnessCup.MadnessCup;
import Coocos.madnessCup.game.other.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.Random;

/**
 * Listener interface implementation for when a player enters the server
 */
public class PlayerJoinListener implements Listener {

    private final MadnessCup plugin;

    public PlayerJoinListener(MadnessCup plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerManager playerManager = plugin.getPlayerManager();
        playerManager.registerPlayer(player.getUniqueId());
        event.setJoinMessage(null);
        player.setGameMode(GameMode.ADVENTURE);
        Random random = new Random();
        String playerName = player.getName();
        player.setFoodLevel(20);
        Location gameLocation = new Location(
                Bukkit.getWorld("World"), 8.5, -56, 8.5, 0, 0);
        player.teleport(gameLocation);
        String[] MESSAGES = {
                playerName + " has joined! The nerds are taking over!",
                "Have no fear " + playerName + " is here!",
                "How much wood could a " + playerName + " chuck if a " + playerName + " could chuck wood?",
                playerName + " is here! With good intentions hopefully.",
        };

        int index = random.nextInt(MESSAGES.length);
        Bukkit.broadcastMessage(ChatColor.YELLOW + MESSAGES[index]);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player)
            event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        event.getEntity().remove();
    }


    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        plugin.disableVanillaFeatures(event.getWorld());
    }
}