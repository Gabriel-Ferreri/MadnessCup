package Coocos.madnessCup.queue;

import java.util.UUID;

public class PlayerInfo {
    private final UUID uuid;
    private Team team;
    private int coins;
    private int kills;
    private int deaths;

    public PlayerInfo(UUID uuid) {
        this.uuid = uuid;
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
    public void addCoins(int amount) { this.coins += amount; }
    public void addKill() { this.kills++; }
    public void addDeath() { this.deaths++; }
}