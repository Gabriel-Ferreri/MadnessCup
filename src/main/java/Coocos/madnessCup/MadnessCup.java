package Coocos.madnessCup;

import Coocos.madnessCup.game.Game;
import Coocos.madnessCup.game.Queue;
import Coocos.madnessCup.game.Team;
import Coocos.madnessCup.game.games.ReincarnationBattle;
import Coocos.madnessCup.game.other.PlayerManager;
import Coocos.madnessCup.game.other.QueueManager;
import Coocos.madnessCup.game.other.TeamManager;
import Coocos.madnessCup.listeners.PlayerJoinListener;
import Coocos.madnessCup.utils.ItemFactory;
import Coocos.madnessCup.utils.MenuHandler;
import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

/**
 * Main class for plugin starting logic implementation
 */
public final class MadnessCup extends JavaPlugin {
    private QueueManager queueManager;
    private TeamManager teamManager;
    private PlayerManager playerManager;

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
        Queue reincarnationQueue = new Queue(this, "reincarnation1", reincarnation, new ArrayList<>(), 2, 3, 0);
        queueManager.registerQueue(reincarnationQueue.getQueueName(), reincarnationQueue);

        // Delay team setup until the server is fully ready
        Bukkit.getScheduler().runTask(this, () -> {

            //Create team manager and add teams
            teamManager = new TeamManager();

            Team redTeam = new Team(this, new ArrayList<>(), "Red Nerds", ChatColor.RED, 0, 4);
            Team orangeTeam = new Team(this, new ArrayList<>(), "Orange Nerds", ChatColor.GOLD, 0, 4);
            Team yellowTeam = new Team(this, new ArrayList<>(), "Yellow Nerds", ChatColor.YELLOW, 0, 4);
            Team limeTeam = new Team(this, new ArrayList<>(), "Lime Nerds", ChatColor.GREEN, 0, 4);

            teamManager.addTeam(redTeam.getTeamName(), redTeam);
            teamManager.addTeam(orangeTeam.getTeamName(), orangeTeam);
            teamManager.addTeam(yellowTeam.getTeamName(), yellowTeam);
            teamManager.addTeam(limeTeam.getTeamName(), limeTeam);

        });

        for (World world : Bukkit.getWorlds()) console.sendMessage(("Loaded world: " + world.getName()));

        //Create player manager
        playerManager = new PlayerManager();

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

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public boolean isAdmin(Player player) {
        return player.getScoreboardTags().contains("admin");
    }

    public void disableVanillaFeatures(World world) {
        world.setGameRule(GameRules.ADVANCE_TIME, false);
        world.setGameRule(GameRules.SPAWN_MOBS, false);
        world.setGameRule(GameRules.SHOW_ADVANCEMENT_MESSAGES, false);
        world.setGameRule(GameRules.ADVANCE_WEATHER, false);
        world.setGameRule(GameRules.BLOCK_DROPS, false);
        world.setGameRule(GameRules.ENTITY_DROPS, false);

    }

}