package Coocos.madnessCup.systems.managers;

import Coocos.madnessCup.systems.PlayerInfo;
import Coocos.madnessCup.systems.Team;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
    public void registerPlayer(UUID uuid) {
        PlayerInfo info = players.computeIfAbsent(uuid, PlayerInfo::new);
        sendPlayerToBackend(info);
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

    public void sendPlayerToBackend(PlayerInfo info) {
        try {
            Map<String, Object> playerData = new HashMap<>();

            UUID uuid = info.getUuid();
            playerData.put("uuid", uuid);
            playerData.put("name", Bukkit.getPlayer(uuid).getName());
            playerData.put("coins", info.getCoins());
            playerData.put("kills", info.getKills());
            playerData.put("deaths", info.getDeaths());
            playerData.put("wins", 0);

            if (info.getTeam() != null) {
                playerData.put("team", info.getTeam().getTeamName());
            } else {
                playerData.put("team", null);
            }

            Gson gson = new Gson();
            String json = gson.toJson(playerData);

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/players"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();


            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            System.out.println("Status: " + response.statusCode());
            System.out.println("Response: " + response.body());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
