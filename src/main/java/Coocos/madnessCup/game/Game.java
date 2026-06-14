package Coocos.madnessCup.game;

import Coocos.madnessCup.MadnessCup;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

/**
 *  Game abstract class from which all the games will inherit some fundamental
 *  methods.
*/
public abstract class Game {
    protected List<UUID> players;
    protected int minCapacity, maxCapacity, currentCapacity;
    protected boolean isRunning = false;
    protected final MadnessCup plugin;

    public Game(MadnessCup plugin, List<UUID> players, int minCapacity, int maxCapacity, int currentCapacity, boolean isRunning) {
        this.plugin = plugin;
        this.players = players;
        this.minCapacity = minCapacity;
        this.maxCapacity = maxCapacity;
        this.currentCapacity = currentCapacity;
        this.isRunning = isRunning;
    }

    /**
     * Function to count 5 seconds before the game starts.
     * @param players: List of players in the game about to start
     */
    public void startCountdown (List<UUID> players) {
        new BukkitRunnable() {
            int seconds = 5;
            @Override
            public void run() {
                if (seconds > 0) {
                    for (UUID player : players) {
                        Player p = Bukkit.getPlayer(player);
                        if (p != null)
                            p.sendMessage(ChatColor.GOLD +
                                    "The game is gonna start in " + seconds + " seconds!");

                    }
                    seconds--;
                }
                else {
                    for (UUID player : players) {
                        Player p = Bukkit.getPlayer(player);
                        if (p != null)
                            p.sendMessage(ChatColor.GOLD + "Game started!");
                    }
                    cancel();
                }
            }

        }.runTaskTimer(plugin, 0L, 20L);
    };


    public abstract void startGame();
    public abstract void endGame();
}
