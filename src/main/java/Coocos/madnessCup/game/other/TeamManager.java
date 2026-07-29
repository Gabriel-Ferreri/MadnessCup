package Coocos.madnessCup.game.other;

import Coocos.madnessCup.game.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeamManager {
    private final Map<String, Team> teams = new HashMap<>();

    public void addTeam(String name, Team team) {
        teams.put(name, team);
    }

    public Team getTeam(String name) {return teams.get(name);}

    public Collection<Team> getAllTeams() {return teams.values();}

    public boolean addPlayerToTeam(UUID player, String teamName) {
        Team team = teams.get(teamName);
        Player p = Bukkit.getPlayer(player);
        if (team == null) return false;

        if (team.getPlayers().size() >= team.getTeamLimit()) {
            p.sendMessage(ChatColor.RED + "The team you're trying to join is full!");
            return false;
        }

        for (Team t : teams.values()) {
            if (t.getPlayers().contains(player)) {
                p.sendMessage(ChatColor.RED + "You're already in a team!");
                return false;
            }
        }

        team.addPlayer(player);
        return true;
    }

}
