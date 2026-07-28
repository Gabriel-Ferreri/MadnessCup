package Coocos.madnessCup.game;

import Coocos.madnessCup.MadnessCup;
import Coocos.madnessCup.utils.Countdown;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * Queue class to organize queues before starting a game
 */
public class Queue {
    private Game game;
    private List<UUID> players;
    private int minCapacity, maxCapacity, currentCapacity;
    private final MadnessCup plugin;
    private Countdown countdown;

    public Queue(MadnessCup plugin, Game game, List<UUID> players, int minCapacity, int maxCapacity, int currentCapacity) {
        this.plugin = plugin;
        this.game = game;
        this.players = players;
        this.minCapacity = minCapacity;
        this.maxCapacity = maxCapacity;
        this.currentCapacity = currentCapacity;
    }

    public MadnessCup getPlugin() { return plugin; }
    public Game getGame() { return this.game; }
    public List<UUID> getPlayers() { return this.players; }
    public int getMinCapacity() { return this.minCapacity; }
    public int getMaxCapacity() { return this.maxCapacity; }
    public int getCurrentCapacity() { return this.currentCapacity; }

    public void setGame(Game game) { this.game = game; }
    public void setPlayers(List<UUID> players) { this.players = players; }
    public void setMinCapacity(int minCapacity) {
        if (minCapacity > 1 && minCapacity <= maxCapacity)
            this.minCapacity = minCapacity;
        else return;
    }
    public void setMaxCapacity(int maxCapacity) {
        if (maxCapacity > 1 && minCapacity <= maxCapacity)
            this.maxCapacity = maxCapacity;
        else return;
    }
    public void addPlayer(UUID player) {
        this.players.add(player);
        this.currentCapacity++;
        if (this.currentCapacity >= minCapacity)
            queueStart(this.game);
    }

    public void removePlayer(UUID player) {
        this.players.remove(player);
        this.currentCapacity--;
        if (this.currentCapacity < minCapacity)
            queueCancel();
    }

    public void queueStart(Game game) {
        countdown = new Countdown(plugin, players, 5) {
            @Override
            public void onFinish() {
                Location gameLocation = new Location(
                        Bukkit.getWorld("game"), 9.5, -57, -10.5, 0, 0);

                for (UUID uuid : players) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) p.teleport(gameLocation);
                }
                players.clear();
                currentCapacity = 0;
                game.startGame();
            }
        };
        countdown.start();
    }
    public void queueCancel() {
        countdown.cancel();
    }

}
