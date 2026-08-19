package Coocos.madnessCup.systems;

import java.util.UUID;

public class PlayerInfo {
    private final UUID uuid;
    private Team team;
    private int coins;
    private int kills;
    private int deaths;

    public PlayerInfo(UUID uuid) {
        this.uuid = uuid;
        this.team = null;
        this.coins = 0;
        this.kills = 0;
        this.deaths = 0;
    }

    public UUID getUuid() { return uuid; }
    public Team getTeam() { return team; }
    public int getCoins() { return coins; }
    public int getKills() { return kills; }
    public int getDeaths() { return deaths; }

    public void setTeam(Team team) { this.team = team; }
    public void setCoins(int coins) { this.coins = coins; }
    public void setKills(int kills) { this.kills = kills; }
    public void setDeaths(int deaths) { this.deaths = deaths; }

    public void addCoins(int amount) { this.coins += amount; }
    public void addKill() { this.kills++; }
    public void addDeath() { this.deaths++; }

    public void reset() { coins = 0; kills = 0; deaths = 0; }
}