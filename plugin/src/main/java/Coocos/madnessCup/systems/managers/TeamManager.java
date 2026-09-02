package Coocos.madnessCup.systems.managers;

import Coocos.madnessCup.systems.Team;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Team manager class to keep track of the teams created for the games
 */
public class TeamManager {
    private final Map<String, Team> teams = new HashMap<>();
    private final Scoreboard scoreboard;

    public TeamManager() {
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    public Collection<Team> getAllTeams() {
        return teams.values();
    }

    public Team getTeam(String name) {return teams.get(name);}

    public void addTeam(String name, Team team) {
        teams.put(name, team);

        // Create or get the Minecraft scoreboard team
        org.bukkit.scoreboard.Team mcTeam = scoreboard.getTeam(name);
        if (mcTeam == null) mcTeam = scoreboard.registerNewTeam(name);

        // Sync color and prefix
        ChatColor color = team.getTeamColor();
        mcTeam.setColor(color);
        mcTeam.setPrefix(color + "[" + team.getTeamName() + "] ");
        mcTeam.setAllowFriendlyFire(false);
        sendTeamToBackend(team);
    }

    public void addPlayerToTeam(UUID player, String teamName) {
        Team team = teams.get(teamName);
        Player p = Bukkit.getPlayer(player);
        if (team == null || p == null) return;

        if (team.getPlayers().size() >= team.getTeamLimit()) {
            p.sendMessage(ChatColor.RED + "The team you're trying to join is full!");
            return;
        }

        for (Team t : teams.values()) {
            if (t.getPlayers().contains(player)) {
                p.sendMessage(ChatColor.RED + "You're already in a team!");
                return;
            }
        }

        team.addPlayer(player);

        // Add to Minecraft scoreboard team
        org.bukkit.scoreboard.Team mcTeam = scoreboard.getTeam(teamName);
        if (mcTeam != null) mcTeam.addEntry(p.getName());

        p.sendMessage(ChatColor.GREEN + "You joined " + teamName + "!");
        putTeamToBackend(team);
    }

    public void removePlayerFromTeam(UUID player, String teamName) {
        Team team = teams.get(teamName);
        Player p = Bukkit.getPlayer(player);
        if (team == null) return;
        team.removePlayer(player); // Remove from your plugin’s logical list

        // Remove from the Minecraft scoreboard team
        org.bukkit.scoreboard.Team mcTeam = Bukkit.getScoreboardManager()
                    .getMainScoreboard().getTeam(teamName);
        if (mcTeam != null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
            mcTeam.removeEntry(offlinePlayer.getName());
        }
        if (p != null) p.sendMessage(ChatColor.YELLOW + "You left " + teamName + "!");
        putTeamToBackend(team);
    }

    public void sendTeamToBackend(Team team) {

        try {
            Map<String, Object> teamData = new HashMap<>();

            teamData.put("teamName", team.getTeamName());
            teamData.put("players", team.getPlayers());
            teamData.put("teamColor", team.getTeamColor().name());
            teamData.put("customizeColor", team.getCustomizeColor().asRGB());
            teamData.put("teamCoins",team.getTeamCoins());
            teamData.put("teamLimit", team.getTeamLimit());

            Gson gson = new Gson();
            String json = gson.toJson(teamData);

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/teams"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status: " + response.statusCode());
            System.out.println("Response: " + response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void putTeamToBackend(Team team) {

        try {
            Map<String, Object> teamData = new HashMap<>();

            teamData.put("teamName", team.getTeamName());
            teamData.put("players", team.getPlayers());
            teamData.put("teamColor", team.getTeamColor().name());
            teamData.put("customizeColor", team.getCustomizeColor().asRGB());
            teamData.put("teamCoins",team.getTeamCoins());
            teamData.put("teamLimit", team.getTeamLimit());

            Gson gson = new Gson();
            String json = gson.toJson(teamData);

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/teams/" + team.getTeamName()))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status: " + response.statusCode());
            System.out.println("Response: " + response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

}
