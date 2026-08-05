package Coocos.madnessCup.systems;

import Coocos.madnessCup.MadnessCup;

import java.util.List;

/**
 *  Game abstract class from which all the games will inherit some fundamental
 *  methods with encapsulation.
*/
public abstract class Game {
    protected List<Team> teams;
    protected boolean isRunning = false;
    protected final MadnessCup plugin;

    public Game(MadnessCup plugin, List<Team> teams, boolean isRunning) {
        this.plugin = plugin;
        this.teams = teams;
        this.isRunning = isRunning;
    }

    public abstract void startGame();
    public abstract void endGame();

    public MadnessCup getPlugin() { return this.plugin; }
    public List<Team> getTeams() { return this.teams; }
    public boolean isRunning() { return this.isRunning; }

    public void setTeams(List<Team> teams) { this.teams = teams; }
    public void setRunning(boolean isRunning) { this.isRunning = isRunning; }

    public void addTeam(Team team) { this.teams.add(team); }

}
