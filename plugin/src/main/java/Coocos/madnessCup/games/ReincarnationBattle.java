package Coocos.madnessCup.games;

import Coocos.madnessCup.MadnessCup;
import Coocos.madnessCup.games.reincarnationExtra.KitsHandling;
import Coocos.madnessCup.systems.Game;
import Coocos.madnessCup.systems.PlayerInfo;
import Coocos.madnessCup.systems.Queue;
import Coocos.madnessCup.systems.Team;
import Coocos.madnessCup.systems.managers.QueueManager;
import Coocos.madnessCup.utils.Countdown;
import Coocos.madnessCup.utils.MenuHandler;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import java.util.*;

/**
 * Represents the Reincarnation Battle minigame.
 * This game mode gives each player 2 lives and tracks their performance
 * throughout the match. The class manages game flow, player state,
 * event handling, and kit assignment.
 * @author Gabriel Ferreri
 */
public class ReincarnationBattle extends Game implements Listener{
    Map<UUID, Integer> players = new HashMap<>(); // Lives left
    private final KitsHandling kitsHandling;
    /**
     * Creates a new instance of the Reincarnation Battle game mode.
     *
     * @param plugin reference to the main MadnessCup plugin
     * @param teams the teams participating in this match
     * @param isRunning whether the game is running or not
     */
    public ReincarnationBattle(MadnessCup plugin, List<Team> teams, boolean isRunning) {
        super(plugin, teams, isRunning);
        this.kitsHandling = new KitsHandling(plugin);
    }

    @Override
    public void startGame() {
        if (isRunning) {
            Bukkit.getLogger().warning("[MadnessCup] Tried to start an already running game!");
            return;
        }
        this.isRunning = true;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getPluginManager().registerEvents(kitsHandling, plugin);
        Location redLocation = new Location(Bukkit.getWorld("reincarnation1"), -18.5, -54, 22.5, 0, 0);
        Location orangeLocation = new Location(Bukkit.getWorld("reincarnation1"), -15.5, -54, -17.5, 0, 0);
        Location yellowLocation = new Location(Bukkit.getWorld("reincarnation1"), 24.5, -54, -15.5, 0, 0);
        Location limeLocation = new Location(Bukkit.getWorld("reincarnation1"), 22.5, -54, 24.5, 0, 0);
        for (Team team : this.teams) {
            for (UUID uuid : team.getPlayers()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;
                players.put(uuid,2);
                player.addScoreboardTag("ingame");
                switch (team.getTeamName()) {
                    case "Red Nerds" -> player.teleport(redLocation);
                    case "Orange Nerds" -> player.teleport(orangeLocation);
                    case "Yellow Nerds" -> player.teleport(yellowLocation);
                    case "Lime Nerds" -> player.teleport(limeLocation);
                }
                kitsHandling.givePlayersInventory(player);
            }
        }
        kitsHandling.initialize(players.size());
        Countdown countdown = new Countdown(plugin, new ArrayList<>(players.keySet()), 5) {
            @Override
            public void onFinish() {
                for (UUID uuid : players.keySet()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) p.sendMessage(ChatColor.GOLD + "Start fighting");
                }
                glassRemoval();
            }
        };
        countdown.start();
    }

    @Override
    public void endGame() {
        winnerCalculator();
        HandlerList.unregisterAll(kitsHandling);
        HandlerList.unregisterAll(this);
        this.isRunning = false;
        MenuHandler menuHandler = new MenuHandler(plugin);
        Location spawn = new Location(Bukkit.getWorld("world"), 8.5, -56, 8.5, 0, 0);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (UUID uuid : players.keySet()) {
                PlayerInfo info = plugin.getPlayerManager().getPlayer(uuid);
                Player player = Bukkit.getPlayer(uuid);
                player.removeScoreboardTag("ingame");
                player.setHealth(20);
                player.setGameMode(GameMode.ADVENTURE);
                Bukkit.getLogger().info("[MadnessCup] Player " + player.getName() + " has " + info.getCoins() + " coins!");
                info.reset();
                if (info != null && info.getTeam() != null)
                    plugin.getTeamManager().removePlayerFromTeam(
                            uuid, info.getTeam().getTeamName());
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.teleport(spawn);
                    menuHandler.defaultInventory(player);
                });
            }
            players.clear();;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                QueueManager queueManager = plugin.getQueueManager();
                queueManager.removeQueue("reincarnation1");
                Bukkit.getLogger().info("[MadnessCup] Creating fresh reincarnation1 world");
                Game reincarnation = new ReincarnationBattle(plugin, new ArrayList<>(), false);
                Queue reincarnationQueue = new Queue(plugin, "reincarnation1", reincarnation, new ArrayList<>(), 2, 12, 0);
                queueManager.registerQueue(reincarnationQueue.getQueueName(), reincarnationQueue);
            },20L);
        }, 160L);
    }

    /**
     * Calculate the points scored by each team at the end of the game and print
     * the results
     */
    public void winnerCalculator() {
        HashMap<Team, Integer> teamPoints = new HashMap<>();
        for (Team team : this.teams) {
            int currentPoints = 0;
            for (UUID uuid : team.getPlayers()) {
                PlayerInfo info = plugin.getPlayerManager().getPlayer(uuid);
                currentPoints += info.getCoins();
            }
            teamPoints.put(team, currentPoints);
        }
        List<Map.Entry<Team, Integer>> sortedTeams = new ArrayList<>(teamPoints.entrySet());
        sortedTeams.sort(Map.Entry.<Team, Integer>comparingByValue().reversed());
        Bukkit.broadcastMessage(ChatColor.WHITE + " ");
        Bukkit.broadcastMessage(ChatColor.GOLD + "The Reincarnation Battle has finished!");
        Bukkit.broadcastMessage(ChatColor.GOLD + "The Final Standings are... ");
        Bukkit.broadcastMessage(ChatColor.WHITE + " ");
        for (Player player : Bukkit.getOnlinePlayers())
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 4.0f, 1.0f);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Individual player standings
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(ChatColor.GOLD + "Individual Standings:");

            List<Map.Entry<UUID, Integer>> sortedPlayers = new ArrayList<>();

            for (UUID uuid : players.keySet()) {
                PlayerInfo info = plugin.getPlayerManager().getPlayer(uuid);
                sortedPlayers.add(Map.entry(uuid, info.getCoins()));
            }
            sortedPlayers.sort(Map.Entry.<UUID, Integer>comparingByValue().reversed());
            int position = 1;

            for (Map.Entry<UUID, Integer> entry : sortedPlayers) {
                UUID uuid = entry.getKey();
                int points = entry.getValue();
                Player player = Bukkit.getPlayer(uuid);
                Bukkit.broadcastMessage(ChatColor.WHITE + " " + position + ". " + player.getName() + " - " + points + " points");
                position++;
            }
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(ChatColor.GOLD + "Team Standings:");
            position = 1;
            for (Map.Entry<Team, Integer> entry : sortedTeams) {
                Team team = entry.getKey();
                int points = entry.getValue();
                if (position == 1) Bukkit.broadcastMessage(team.getTeamColor() + "🏆 1st Place: "
                        + team.getTeamName() + " - " + points + " points!");
                else Bukkit.broadcastMessage(team.getTeamColor() + " " + position + ". " + team.getTeamName() + " - " + points + " points");
                position++;
            }
            Bukkit.broadcastMessage(ChatColor.WHITE + " ");
            for (Player player : Bukkit.getOnlinePlayers())
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IMITATE_ENDER_DRAGON, 1.0f, 1.0f);

        }, 60L);

    }

    /**
     * When a player dies, handle what happens to the victim, the killer and respawn
     * the player. Check if the game has ended.
     * @param event The player death event
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        handleVictimDeath(player);
        handleKillerReward(player);
        Location location = new Location(Bukkit.getWorld("reincarnation1"), -15.5, -54, -17.5, 0, 0);

        Bukkit.getScheduler().runTask(plugin, () -> {
            player.spigot().respawn();
            player.teleport(location);
            lifeCheck(player);
            checkForGameEnd();
        });
    }

    /**
     * Remove a life from the player who died and add it to its statistics
     * @param player The player who has died
     */
    private void handleVictimDeath(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerInfo info = plugin.getPlayerManager().getPlayer(uuid);

        info.addDeath();
        int lives = players.get(uuid) - 1;
        players.put(uuid, lives);

        Bukkit.getLogger().info("[MadnessCup] " + player.getName() + " now has " + lives + " lives.");
    }

    /**
     * Reward the killer and add points and the kill to its statistics
     * @param victim The player who got killed
     */
    private void handleKillerReward(Player victim) {
        Player killer = victim.getKiller();
        if (killer == null) return;

        UUID killerUuid = killer.getUniqueId();
        PlayerInfo killerInfo = plugin.getPlayerManager().getPlayer(killerUuid);

        killerInfo.addKill();
        killerInfo.addCoins(50);

        killer.sendMessage(ChatColor.GOLD + "⚔ You get +50 points for killing " + victim.getName() + "!");
        killer.playSound(victim.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
    }

    /**
     * Check if there is only one team standing and if there is give kill bonus,
     * broadcast the last team standing award and end the game
     */
    private void checkForGameEnd() {
        Team lastTeam = getLastTeamStanding();
        if (lastTeam == null) return;

        awardKillBonus();
        int reward = 150 / lastTeam.getPlayers().size();
        Bukkit.broadcastMessage(lastTeam.getTeamColor() + "🏆 "
                + lastTeam.getTeamName() + " was the last team standing!");
        Bukkit.broadcastMessage(lastTeam.getTeamColor()
                + "Winning team reward: +" + reward + " points per player!");

        for (UUID uuid : lastTeam.getPlayers()) {
            PlayerInfo info = plugin.getPlayerManager().getPlayer(uuid);
            info.addCoins(reward);
        }
        endGame();
    }

    /**
     * Give the top killer(s) of the game extra points
     */
    private void awardKillBonus() {
        List<UUID> topKillers = getTopKillers();
        if (topKillers.isEmpty()) return;

        int highestKills = plugin.getPlayerManager().getPlayer(topKillers.get(0)).getKills();
        int reward = 50 / topKillers.size();

        broadcastKillBonus(topKillers, highestKills, reward);
        giveKillBonus(topKillers, reward);
    }

    /**
     * Get a list of the player in the match with the most kills
     * @return a list of UUIDs representing the top killers
     */
    private List<UUID> getTopKillers() {
        int highestKills = -1;
        List<UUID> topKillers = new ArrayList<>();

        for (UUID uuid : players.keySet()) {
            PlayerInfo info = plugin.getPlayerManager().getPlayer(uuid);
            int kills = info.getKills();

            if (kills > highestKills) {
                highestKills = kills;
                topKillers.clear();
                topKillers.add(uuid);
            }
            else if (kills == highestKills) {
                topKillers.add(uuid);
            }
        }

        return topKillers;
    }

    /**
     * Send a broadcast message to the players in the server announcing which
     * player(s) got the most kills
     * @param topKillers Player(s) with the most kills in the game
     * @param highestKills Number of the top killer's kills
     * @param reward The amount of points each top killer is getting
     */
    private void broadcastKillBonus(List<UUID> topKillers, int highestKills, int reward) {
        if (topKillers.size() == 1) {
            UUID uuid = topKillers.get(0);
            Player player = Bukkit.getPlayer(uuid);
            Bukkit.broadcastMessage(ChatColor.RED + "⚔ " + player.getName()
                    + " had the most kills with " + highestKills + " kills!");
            Bukkit.broadcastMessage(ChatColor.GOLD + player.getName()
                    + " receives +" + reward + " bonus points!");
        }
        else {
            Bukkit.broadcastMessage(ChatColor.RED + "⚔ " + "There is a draw for most kills! "
                    + "Each player receives +" + reward + " points.");
            for (UUID uuid : topKillers) {
                Player player = Bukkit.getPlayer(uuid);
                Bukkit.broadcastMessage(ChatColor.GOLD + player.getName()
                        + " receives +" + reward + " bonus points!");
            }
        }
    }

    /**
     * Gives kill bonus points to the top killer(s)
     * @param topKillers Player(s) with the most kills in the game
     * @param reward The amount of points each top killer is getting
     */
    private void giveKillBonus(List<UUID> topKillers, int reward) {
        for (UUID uuid : topKillers) {
            PlayerInfo info = plugin.getPlayerManager().getPlayer(uuid);
            info.addCoins(reward);
        }
    }

    /**
     * Manages what happens if a player has 1 or 0 lives
     * @param player player being checked
     */
     private void lifeCheck(Player player) {
        if (players.get(player.getUniqueId()) == 1) kitsHandling.startKitSelection(player);
        if (players.get(player.getUniqueId()) == 0) player.setGameMode(GameMode.SPECTATOR);
    }

    /**
     * Gets the only team remaining in the game.
     * @return null if multiple teams are still alive or the last team standing if there
     * is only one left
     */
    private Team getLastTeamStanding() {
        Team survivingTeam = null;
        for (UUID uuid : players.keySet()) {
            if (players.get(uuid) > 0) {
                PlayerInfo info = plugin.getPlayerManager().getPlayer(uuid);

                if (survivingTeam == null) {
                    survivingTeam = info.getTeam();
                } else if (!survivingTeam.equals(info.getTeam())) {
                    return null;
                }
            }
        }
        return survivingTeam;
    }

    /**
     * Checks when a player breaks a block that gives points and adds them.
     * @param event the block break event
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerInfo info = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        if (event.getBlock().getType() == Material.RAW_GOLD_BLOCK &&
                players.containsKey(player.getUniqueId())) {
            info.addCoins(10);
            player.sendMessage(ChatColor.GOLD + "+10 points!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
        };
        if (event.getBlock().getType() == Material.GOLD_BLOCK &&
                players.containsKey(player.getUniqueId())) {
            info.addCoins(30);
            player.sendMessage(ChatColor.GOLD + "+30 points!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
        }
    }

    /**
     * Prevents players in the current game from losing hunger.
     * @param event the food level change event
     */
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player &&
                players.containsKey(player.getUniqueId())) {
            if (event.getFoodLevel() < player.getFoodLevel()) event.setCancelled(true);
        }
    }

    /**
     * Removes glass in the only current map.
     */
    public void glassRemoval() {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute in minecraft:reincarnation1 run gamerule send_command_feedback false");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute in minecraft:reincarnation1 run fill -16 -52 24 -21 -54 20 air replace red_stained_glass");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute in minecraft:reincarnation1 run fill -18 -52 -16 -14 -54 -21 air replace orange_stained_glass");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute in minecraft:reincarnation1 run fill 22 -52 -14 27 -54 -18 air replace yellow_stained_glass");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute in minecraft:reincarnation1 run fill 24 -52 22 20 -54 27 air replace lime_stained_glass");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute in minecraft:reincarnation1 run gamerule send_command_feedback true");
    }
}