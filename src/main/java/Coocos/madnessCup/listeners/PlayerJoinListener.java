package Coocos.madnessCup.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Random;

/**
 * Listener interface implementation for when a player enters the server
 */
public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.setGameMode(GameMode.ADVENTURE);
        Random random = new Random();
        String playerName = player.getName();

        String[] MESSAGES = {
                playerName + " is ready for trouble!",
                "Have no fear " + playerName + " is here!",
                "How much wood could " + playerName + " chuck if " + playerName + " could chuck wood?",
                playerName + " is here! With good intentions hopefully.",
        };

        int index = random.nextInt(MESSAGES.length);
        Bukkit.broadcastMessage(MESSAGES[index]);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }
}