package Coocos.madnessCup;

import Coocos.madnessCup.game.Game;
import Coocos.madnessCup.game.Queue;
import Coocos.madnessCup.game.Team;
import Coocos.madnessCup.game.games.ReincarnationBattle;
import Coocos.madnessCup.game.other.QueueManager;
import Coocos.madnessCup.game.other.TeamManager;
import Coocos.madnessCup.listeners.PlayerJoinListener;
import Coocos.madnessCup.utils.ItemFactory;
import Coocos.madnessCup.utils.MenuHandler;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.WorldCreator;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Main class for plugin starting logic implementation
 */
public final class MadnessCup extends JavaPlugin {
    private QueueManager queueManager;
    private TeamManager teamManager;

    @Override
    public void onEnable() {
        // Plugin startup logic

        ConsoleCommandSender console = getServer().getConsoleSender();
        console.sendMessage(ChatColor.GREEN + "MadnessCup Plugin Enabled");
        // Delay world loading until server is ready
        Bukkit.getScheduler().runTask(this, () -> {
            new WorldCreator("game").createWorld();
        });
        ItemFactory.init(this);
        queueManager = new QueueManager();

        Game reincarnation = new ReincarnationBattle(this, new ArrayList<>(), false);
        Queue reincarnationQueue = new Queue(this, reincarnation, new ArrayList<>(), 1, 3, 0);
        queueManager.registerQueue("reincarnation1", reincarnationQueue);

        //Create team manager and add teams
        teamManager = new TeamManager();

        Team redTeam = new Team(this, Collections.emptyList(), "Red Nerds", Color.RED, 0, 4);
        Team orangeTeam = new Team(this, Collections.emptyList(), "Orange Nerds", Color.ORANGE, 0, 4);
        Team yellowTeam = new Team(this, Collections.emptyList(), "Yellow Nerds", Color.YELLOW, 0, 4);
        Team limeTeam = new Team(this, Collections.emptyList(), "Lime Nerds", Color.LIME, 0, 4);

        teamManager.addTeam(redTeam.getTeamName(), redTeam);
        teamManager.addTeam(redTeam.getTeamName(), orangeTeam);
        teamManager.addTeam(redTeam.getTeamName(), yellowTeam);
        teamManager.addTeam(redTeam.getTeamName(), limeTeam);

        //Event Listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuHandler(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        ConsoleCommandSender console = getServer().getConsoleSender();
        console.sendMessage(ChatColor.RED + "MadnessCup Plugin Disabled");
    }

    public QueueManager getQueueManager() {
        return queueManager;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }
}