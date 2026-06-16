package Coocos.madnessCup.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

public class Countdown {

    private final JavaPlugin plugin;
    private final List<UUID> players;
    private int seconds;

    public Countdown(JavaPlugin plugin, List<UUID> players, int seconds) {
        this.plugin = plugin;
        this.players = players;
        this.seconds = seconds;
    }

    public void start() {

        new BukkitRunnable() {
            @Override
            public void run() {
                if (seconds > 0) {
                    for (UUID uuid : players) {
                        Player p = Bukkit.getPlayer(uuid);

                        if (p != null)
                            p.sendMessage(ChatColor.GOLD + "Starting in " + seconds + "...");
                    }
                    seconds--;

                }
                else {
                    for (UUID uuid : players) {
                        Player p = Bukkit.getPlayer(uuid);

                        if (p != null)
                            p.sendMessage(ChatColor.GOLD + "Game started!");
                    }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
}