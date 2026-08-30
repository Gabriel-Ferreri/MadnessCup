package Coocos.madnessCup.systems.managers;

import Coocos.madnessCup.systems.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    }

}
