package Coocos.madnessCup.game;

import java.util.List;
import java.util.UUID;

public class Queue {
    private Game game;
    private List<UUID> players;
    private int minCapacity, maxCapacity, currentCapacity;

    public Queue(Game game, List<UUID> players, int minCapacity, int maxCapacity, int currentCapacity) {
        this.game = game;
        this.players = players;
        this.minCapacity = minCapacity;
        this.maxCapacity = maxCapacity;
        this.currentCapacity = currentCapacity;
    }

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
    public void setCurrentCapacity(int currentCapacity) { this.currentCapacity = currentCapacity; }

}
