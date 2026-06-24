package Coocos.madnessCup.game;

import Coocos.madnessCup.MadnessCup;

import java.util.List;
import java.util.UUID;

/**
 *  Game abstract class from which all the games will inherit some fundamental
 *  methods with encapsulation.
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

    public MadnessCup getPlugin() { return this.plugin; }
    public List<UUID> getPlayers() { return this.players; }
    public boolean isRunning() { return this.isRunning; }

    public void setPlayers(List<UUID> players) { this.players = players; }
    public void setRunning(boolean isRunning) { this.isRunning = isRunning; }

    public void addPlayer(UUID player) { this.players.add(player); }

}
