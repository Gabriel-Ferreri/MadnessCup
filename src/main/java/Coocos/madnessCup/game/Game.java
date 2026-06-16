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
    protected boolean isRunning = false;
    protected final MadnessCup plugin;

    public Game(MadnessCup plugin, List<UUID> players, boolean isRunning) {
        this.plugin = plugin;
        this.players = players;
        this.isRunning = isRunning;
    }

    public abstract void startGame();
    public abstract void endGame();
}
