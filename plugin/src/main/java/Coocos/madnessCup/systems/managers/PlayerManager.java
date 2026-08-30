package Coocos.madnessCup.systems.managers;

import Coocos.madnessCup.systems.PlayerInfo;
import Coocos.madnessCup.systems.Team;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Player manager class to keep track of players online
 */
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

    public Team getPlayerTeam(UUID uuid) {
        PlayerInfo info = players.get(uuid);
        if (info == null) return null;
        return info.getTeam();
    }
}
