package Coocos.madnessCup.systems;

import com.google.gson.Gson;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerInfo {
    private final UUID uuid;
    private Team team;
    private int coins;
    private int kills;
    private int deaths;
    private int wins;

    public PlayerInfo(UUID uuid) {
        this.uuid = uuid;
        this.team = null;
        this.coins = 0;
        this.kills = 0;
        this.deaths = 0;
        this.wins = 0;
    }

    public UUID getUuid() { return uuid; }
    public Team getTeam() { return team; }
    public int getCoins() { return coins; }
    public int getKills() { return kills; }
    public int getDeaths() { return deaths; }
    public int getWins() { return wins; }

    public void setTeam(Team team) { this.team = team; updatePlayerBackend(this);}
    public void setCoins(int coins) { this.coins = coins; updatePlayerBackend(this);}
    public void setKills(int kills) { this.kills = kills; updatePlayerBackend(this);}
    public void setDeaths(int deaths) { this.deaths = deaths; updatePlayerBackend(this);}
    public void setWins(int wins) { this.wins = wins; updatePlayerBackend(this);}

    public void addCoins(int amount) { this.coins += amount; updatePlayerBackend(this);}
    public void addKill() { this.kills++; updatePlayerBackend(this);}
    public void addDeath() { this.deaths++; updatePlayerBackend(this);}
    public void addWin() { this.wins++; updatePlayerBackend(this);}

    public void reset() { setCoins(0); setKills(0); setDeaths(0); }

    public void updatePlayerBackend(PlayerInfo info) {
        try {
            Map<String, Object> playerData = new HashMap<>();

            UUID uuid = info.getUuid();

            playerData.put("uuid", uuid);
            playerData.put("name", Bukkit.getPlayer(uuid).getName());
            playerData.put("coins", info.getCoins());
            playerData.put("kills", info.getKills());
            playerData.put("deaths", info.getDeaths());
            playerData.put("wins", info.getWins());

            if (info.getTeam() != null) {
                playerData.put("team", info.getTeam().getTeamName());
            } else {
                playerData.put("team", null);
            }

            Gson gson = new Gson();
            String json = gson.toJson(playerData);

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/players/" + uuid))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            System.out.println("Update status: " + response.statusCode());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}