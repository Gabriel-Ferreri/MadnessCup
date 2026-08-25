package Coocos.madnessCup;

import Coocos.madnessCup.games.reincarnationExtra.KitsHandling;
import Coocos.madnessCup.systems.Game;
import Coocos.madnessCup.systems.PlayerInfo;
import Coocos.madnessCup.systems.Queue;
import Coocos.madnessCup.systems.Team;
import Coocos.madnessCup.games.ReincarnationBattle;
import Coocos.madnessCup.systems.managers.PlayerManager;
import Coocos.madnessCup.systems.managers.QueueManager;
import Coocos.madnessCup.systems.managers.TeamManager;
import Coocos.madnessCup.listeners.PlayerJoinListener;
import Coocos.madnessCup.utils.DirectoryManager;
import Coocos.madnessCup.utils.ItemFactory;
import Coocos.madnessCup.utils.MenuHandler;
import com.google.gson.Gson;
import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

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
        DirectoryManager.deleteDimension("reincarnation1");
        ItemFactory.init(this);
        queueManager = new QueueManager();

        Game reincarnation = new ReincarnationBattle(this, new ArrayList<>(), false);
        Queue reincarnationQueue = new Queue(this, "reincarnation1", reincarnation, new ArrayList<>(), 2, 12, 0);
        queueManager.registerQueue(reincarnationQueue.getQueueName(), reincarnationQueue);

        // Delay team setup until the server is fully ready
        Bukkit.getScheduler().runTask(this, () -> {

            //Create team manager and add teams
            teamManager = new TeamManager();

            Team redTeam = new Team(this, new ArrayList<>(), "Red Nerds", ChatColor.RED, Color.RED, 0, 4);
            Team orangeTeam = new Team(this, new ArrayList<>(), "Orange Nerds", ChatColor.GOLD, Color.ORANGE, 0, 4);
            Team yellowTeam = new Team(this, new ArrayList<>(), "Yellow Nerds", ChatColor.YELLOW, Color.YELLOW, 0, 4);
            Team limeTeam = new Team(this, new ArrayList<>(), "Lime Nerds", ChatColor.GREEN, Color.LIME, 0, 4);

            teamManager.addTeam(redTeam.getTeamName(), redTeam);
            teamManager.addTeam(orangeTeam.getTeamName(), orangeTeam);
            teamManager.addTeam(yellowTeam.getTeamName(), yellowTeam);
            teamManager.addTeam(limeTeam.getTeamName(), limeTeam);


            testBackend();
            testTeamBackend();
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
        for (PlayerInfo info : playerManager.getAllPlayers()) {
            Bukkit.getPlayer(info.getUuid()).removeScoreboardTag("ingame");
            if (info.getTeam() != null) teamManager.removePlayerFromTeam(info.getUuid(), info.getTeam().getTeamName());
        }
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

    public boolean isAdmin(Player player) {return player.getScoreboardTags().contains("admin");}
    public boolean isInGame(Player player) {return player.getScoreboardTags().contains("ingame");}

    public boolean bypassLobbyRestrictions(Player player) {return isAdmin(player) || isInGame(player);}

    public void disableVanillaFeatures(World world) {
        world.setGameRule(GameRules.ADVANCE_TIME, false);
        world.setGameRule(GameRules.SPAWN_MOBS, false);
        world.setGameRule(GameRules.SHOW_ADVANCEMENT_MESSAGES, false);
        world.setGameRule(GameRules.ADVANCE_WEATHER, false);
        world.setGameRule(GameRules.BLOCK_DROPS, false);
        world.setGameRule(GameRules.ENTITY_DROPS, false);
        world.setGameRule(GameRules.NATURAL_HEALTH_REGENERATION, false);
    }

    public void testBackend() {

        try {

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/hello")).GET().build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println(response.body());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void testTeamBackend() {

        try {

            List<String> teamNames = this.getTeamManager()
                    .getAllTeams()
                    .stream()
                    .map(Team::getTeamName)
                    .collect(Collectors.toList());

            Gson gson = new Gson();
            String json = gson.toJson(teamNames);

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

}