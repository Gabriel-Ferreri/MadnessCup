package Coocos.madnessCup.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

/**
 * Countdown class to create countdowns dynamically for queues and possibly more.
 */
public class Countdown {
    private final JavaPlugin plugin;
    private final List<UUID> players;
    private int seconds;
    private BukkitRunnable task;

    public Countdown(JavaPlugin plugin, List<UUID> players, int seconds) {
        this.plugin = plugin;
        this.players = players;
        this.seconds = seconds;
    }

    public void onFinish() {
        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null)
                p.sendMessage(ChatColor.RED + "Countdown finished!");
        }
    }

    public void start() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (seconds > 0) {
                    for (UUID uuid : players) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) {
                            p.sendMessage(ChatColor.GOLD + "Starting in " + seconds + "...");
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.0f);
                        }
                    }
                    seconds--;
                } else {
                    for (UUID uuid : players) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) {
                            p.sendMessage(ChatColor.GOLD + "Game started!");
                            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        }
                    }
                    onFinish();
                    cancel();
                }
            }
        };
        task.runTaskTimer(plugin, 0L, 20L);
    }

    public void cancel() {
        if (task != null) {
            task.cancel();
            for (UUID uuid : players) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null)
                    p.sendMessage(ChatColor.RED + "Countdown cancelled!");
            }
        }
    }
}