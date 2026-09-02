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
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitTask;

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
            Bukkit.getLogger().warning(
                    "[MadnessCup] Tried to start an already running game!");
            return;
        }
        isRunning = true;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getPluginManager().registerEvents(kitsHandling, plugin);
        for (Team team : teams) {
            Location spawn = getTeamSpawn(team);
            for (UUID uuid : team.getPlayers()) setupPlayer(uuid, spawn);
        }
        kitsHandling.initialize(players.size());
        startCountdown();
    }

    /**
     * Set up a single player to be ready for the game
     * @param uuid Player id
     * @param spawn Spawn location of the player
     */
    private void setupPlayer(UUID uuid, Location spawn) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        players.put(uuid, 2);
        player.addScoreboardTag("ingame");
        player.teleport(spawn);
        kitsHandling.givePlayersInventory(player);
    }

    /**
     * Start the countdown before the player cages open and handle what happens
     * at the end
     */
    private void startCountdown() {
        Countdown countdown = new Countdown(plugin, new ArrayList<>(
                players.keySet()), 5) {
            @Override
            public void onFinish() {
                for (UUID uuid : players.keySet()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) player.sendMessage(ChatColor.GOLD + "Start fighting");
                }
                glassRemoval();
            }
        };
        countdown.start();
    }

    private Location getTeamSpawn(Team team) {
        World world = Bukkit.getWorld("reincarnation1");
        return switch (team.getTeamName()) {
            case "Red Nerds" -> new Location(world, -18.5, -54, 22.5, 0, 0);
            case "Orange Nerds" -> new Location(world, -15.5, -54, -17.5, 0, 0);
            case "Yellow Nerds" -> new Location(world, 24.5, -54, -15.5, 0, 0);
            case "Lime Nerds" -> new Location(world, 22.5, -54, 24.5, 0, 0);
            default -> throw new IllegalArgumentException("Unknown team: " + team.getTeamName());
        };
    }

    @Override
    public void endGame() {
        winnerCalculator();
        HandlerList.unregisterAll(kitsHandling);
        HandlerList.unregisterAll(this);
        isRunning = false;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            resetPlayers();
            recreateWorld("reincarnation1");
        }, 160L);
    }

    /**
     * Reset all the players by teleporting them back to spawn
     */
    private void resetPlayers() {
        MenuHandler menuHandler = new MenuHandler(plugin);
        Location spawn = new Location(Bukkit.getWorld("world"), 8.5, -56, 8.5, 0, 0);
        for (UUID uuid : players.keySet()) {
            PlayerInfo info = plugin.getPlayerManager().getPlayer(uuid);
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) resetPlayer(player, spawn, menuHandler);
            if (info.getTeam() == null) return;
            plugin.getTeamManager().removePlayerFromTeam(uuid, info.getTeam().getTeamName());
            info.reset();
        }
        players.clear();
    }

    /**
     * Reset the values of a single player
     * @param player Player whose infos are resetting
     * @param spawn Spawn point to tp the player
     * @param menuHandler Menu handler object
     */
    private void resetPlayer(Player player, Location spawn, MenuHandler menuHandler) {
        player.removeScoreboardTag("ingame");
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(spawn);
        menuHandler.defaultInventory(player);
    }

    /**
     * Calculate the points scored by each team at the end of the game and print
     * the results
     */
    public void winnerCalculator() {
        Map<Team, Integer> teamPoints = calculateTeamPoints();
        List<Map.Entry<Team, Integer>> sortedTeams = sortTeamsByPoints(teamPoints);
        announceGameFinished();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            displayIndividualStandings();
            displayTeamStandings(sortedTeams);
            playFinalSound();
        }, 60L);
    }

    /**
     * Calculate the amount of points scored by each team
     * @return A list of the teams with their associated points
     */
    private Map<Team, Integer> calculateTeamPoints() {
        Map<Team, Integer> teamPoints = new HashMap<>();
        for (Team team : teams) {
            int points = calculateTeamPoints(team);
            teamPoints.put(team, points);
        }
        return teamPoints;
    }

    /**
     * Calculate the amount of points scored by a single team
     * @param team Team whose points are going to be calculated
     * @return Total points of a single team
     */
    private int calculateTeamPoints(Team team) {
        int points = 0;
        for (UUID uuid : team.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) player.setNoDamageTicks(180);
            PlayerInfo info = plugin.getPlayerManager().getPlayer(uuid);
            points += info.getCoins();
        }
        return points;
    }

    /**
     * Sort the game teams by points
     * @param teamPoints Team with their points
     * @return Sorted teams based on their points
     */
    private List<Map.Entry<Team, Integer>> sortTeamsByPoints(Map<Team, Integer> teamPoints) {

        List<Map.Entry<Team, Integer>> sortedTeams = new ArrayList<>(teamPoints.entrySet());

        sortedTeams.sort(Map.Entry.<Team, Integer>comparingByValue().reversed());
        return sortedTeams;
    }

    /**
     * Announce the game has finished with a nice formatted message
     */
    private void announceGameFinished() {
        Bukkit.broadcastMessage(ChatColor.WHITE + " ");
        Bukkit.broadcastMessage(ChatColor.GOLD + "The Reincarnation Battle has finished!");
        Bukkit.broadcastMessage(ChatColor.GOLD + "The Final Standings are...");
        Bukkit.broadcastMessage(ChatColor.WHITE + " ");
        for (Player player : Bukkit.getOnlinePlayers())
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 4.0f, 1.0f);
    }

    /**
     * Display the individual standings with a good formatted message
     */
    private void displayIndividualStandings() {
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.GOLD + "Individual Standings:");
        List<Map.Entry<UUID, Integer>> sortedPlayers = calculateIndividualStandings();
        displayPlayers(sortedPlayers);
        Bukkit.broadcastMessage("");
    }

    /**
     * Create a list of players with their points and sort it
     * @return The sorted player list
     */
    private List<Map.Entry<UUID, Integer>> calculateIndividualStandings() {
        List<Map.Entry<UUID, Integer>> sortedPlayers = new ArrayList<>();

        for (UUID uuid : players.keySet()) {
            PlayerInfo info = plugin.getPlayerManager().getPlayer(uuid);
            sortedPlayers.add(Map.entry(uuid, info.getCoins()));
        }

        sortedPlayers.sort(Map.Entry.<UUID, Integer>comparingByValue().reversed());
        return sortedPlayers;
    }

    /**
     * Print in chat the leaderboard of players and their coins
     * @param sortedPlayers Sorted list of player
     */
    private void displayPlayers(List<Map.Entry<UUID, Integer>> sortedPlayers) {
        int position = 1;
        for (Map.Entry<UUID, Integer> entry : sortedPlayers) {
            UUID uuid = entry.getKey();
            int points = entry.getValue();
            String playerName = getPlayerName(uuid);
            Bukkit.broadcastMessage(ChatColor.WHITE + " "
                            + position + ". " + playerName + " - " + points + " points");
            position++;
        }
    }

    /**
     * Return a players name based on the id whether the player is online or not
     * @param uuid Player id
     * @return Player name
     */
    private String getPlayerName(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) return player.getName();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        return offlinePlayer.getName();
    }

    /**
     * Print in chat a leaderboard with the teams position and points
     * @param sortedTeams List of teams final positions
     */
    private void displayTeamStandings(List<Map.Entry<Team, Integer>> sortedTeams) {
        Bukkit.broadcastMessage(ChatColor.GOLD + "Team Standings:");
        int position = 1;
        for (Map.Entry<Team, Integer> entry : sortedTeams) {
            Team team = entry.getKey();
            int points = entry.getValue();
            if (position == 1) {
                Bukkit.broadcastMessage(team.getTeamColor() + "🏆 1st Place: "
                        + team.getTeamName() + " - " + points + " points!");
                rewardWinningTeam(team);
            }
            else Bukkit.broadcastMessage(team.getTeamColor() + " " + position
                    + ". " + team.getTeamName() + " - " + points + " points");
            position++;
        }
        Bukkit.broadcastMessage(ChatColor.WHITE + " ");
    }

    /**
     *  Add a win to each player in the team and call the fireworks spawning function
     * @param team Winning team
     */
    private void rewardWinningTeam(Team team) {
        for (UUID uuid : team.getPlayers()) {
            PlayerInfo info = plugin.getPlayerManager().getPlayer(uuid);
            info.addWin();
            Player player = Bukkit.getPlayer(uuid);

            if (player != null) {
                BukkitTask fireworkTask = Bukkit.getScheduler().runTaskTimer(
                        plugin, () -> spawnWinnerFirework(player), 150L, 10L);
                Bukkit.getScheduler().runTaskLater(plugin, fireworkTask::cancel, 190L);
            }
        }
    }

    /**
     * Shoot some fireworks dealing no damage coming from the winners to celebrate
     * @param player Player from which fireworks will spawn
     */
    private void spawnWinnerFirework(Player player) {
        if (!player.isOnline()) return;
        Firework firework = player.getWorld().spawn(
                player.getLocation().add(0, 1, 0), Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).
                withColor(Color.YELLOW).withColor(Color.WHITE).flicker(true).trail(true).build());
        meta.setPower(0);
        firework.setFireworkMeta(meta);
    }

    private void playFinalSound() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(),
                    Sound.BLOCK_NOTE_BLOCK_IMITATE_ENDER_DRAGON, 1.0f, 1.0f);
        }
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

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!player.getScoreboardTags().contains("ingame")) return;
        UUID uuid = player.getUniqueId();
        players.put(uuid, 0);
        player.removeScoreboardTag("ingame");
        checkForGameEnd();
    }
}