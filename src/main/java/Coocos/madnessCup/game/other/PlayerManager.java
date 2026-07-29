package Coocos.madnessCup.game.other;

import Coocos.madnessCup.game.PlayerInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {
    private final Map<UUID, PlayerInfo> players = new HashMap<>();

    //Compute if absent is a Map interface Java method to create add a value if it doesn't exist
    public PlayerInfo registerPlayer(UUID uuid) {
        return players.computeIfAbsent(uuid, PlayerInfo::new);
    }

    public PlayerInfo getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public Collection<PlayerInfo> getAllPlayers() {
        return players.values();
    }
}
