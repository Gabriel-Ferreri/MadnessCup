package Coocos.madnessCup.game;

import java.util.List;
import java.util.UUID;

public class Queue {
    private Game game;
    private List<UUID> players;

    public Queue(Game game, List<UUID> players) {
        this.game = game;
        this.players = players;
    }

    public Game getGame() {
        return this.game;
    }
    public List<UUID> getPlayers() { return this.players; }

}
