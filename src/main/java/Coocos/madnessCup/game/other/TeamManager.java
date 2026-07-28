package Coocos.madnessCup.game.other;

import Coocos.madnessCup.game.Team;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TeamManager {
    private final Map<String, Team> teams = new HashMap<>();

    public void registerTeam(String name, Team team) {
        teams.put(name, team);
    }

    public Team getTeam(String name) {return teams.get(name);}

    public Collection<Team> getAllTeams() {return teams.values();}
}
