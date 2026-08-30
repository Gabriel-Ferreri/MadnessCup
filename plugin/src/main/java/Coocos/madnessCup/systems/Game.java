package Coocos.madnessCup.systems;

import Coocos.madnessCup.MadnessCup;
import Coocos.madnessCup.games.ReincarnationBattle;
import Coocos.madnessCup.systems.managers.QueueManager;
import Coocos.madnessCup.utils.DirectoryManager;
import org.bukkit.Bukkit;

import java.util.ArrayList;
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

    /**
     * When a game ends, delete the world that was use and create a new one
     * @param worldName Name of the world to recreate
     */
    public void recreateWorld(String worldName) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            String lowerWorld = worldName.toLowerCase();
            QueueManager queueManager = plugin.getQueueManager();
            DirectoryManager.deleteDimension(lowerWorld);
            Bukkit.getLogger().info("[MadnessCup] Creating fresh " + lowerWorld + " world");
            Game reincarnation = new ReincarnationBattle(plugin, new ArrayList<>(), false);
            Queue reincarnationQueue = new Queue(plugin, lowerWorld, reincarnation, new ArrayList<>(), 2, 16, 0);
            queueManager.registerQueue(reincarnationQueue.getQueueName(), reincarnationQueue);
        },20L);
    }

    public MadnessCup getPlugin() { return this.plugin; }
    public List<Team> getTeams() { return this.teams; }
    public boolean isRunning() { return this.isRunning; }

    public void setTeams(List<Team> teams) { this.teams = teams; }
    public void setRunning(boolean isRunning) { this.isRunning = isRunning; }

    public void addTeam(Team team) { this.teams.add(team); }

}
