package Coocos.madnessCup.games;

import Coocos.madnessCup.MadnessCup;
import Coocos.madnessCup.games.reincarnationExtra.KitsHandling;
import Coocos.madnessCup.systems.Game;
import Coocos.madnessCup.systems.PlayerInfo;
import Coocos.madnessCup.systems.Queue;
import Coocos.madnessCup.systems.Team;
import Coocos.madnessCup.systems.managers.QueueManager;
import Coocos.madnessCup.utils.Countdown;
import Coocos.madnessCup.utils.ItemFactory;
import Coocos.madnessCup.utils.MenuHandler;
import io.papermc.paper.block.BlockPredicate;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemAdventurePredicate;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.BlockTypeKeys;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.BlockType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class ReincarnationBattle extends Game implements Listener{
    Map<UUID, Integer> players = new HashMap<>();
    private final Set<KitsHandling.Kit> selectedKits = new HashSet<>();
    public ReincarnationBattle(MadnessCup plugin, List<Team> teams, boolean isRunning) {
        super(plugin, teams, isRunning);
    }

    @Override
    public void startGame() {
        if (isRunning) {
            Bukkit.getLogger().warning("[MadnessCup] Tried to start an already running game!");
            return;
        }
        this.isRunning = true;
        KitsHandling kitsHandling = new KitsHandling(plugin, selectedKits);
        Bukkit.getPluginManager().registerEvents(this, plugin);
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
        HandlerList.unregisterAll(this);
        this.isRunning = false;
        MenuHandler menuHandler = new MenuHandler(plugin);
        Location spawn = new Location(Bukkit.getWorld("world"), 8.5, -56, 8.5, 0, 0);

        for (UUID uuid : players.keySet()) {
            PlayerInfo info = plugin.getPlayerManager().getPlayer(uuid);
            Player player = Bukkit.getPlayer(uuid);
            player.sendMessage(ChatColor.GOLD + "GAME OVER");
            player.removeScoreboardTag("ingame");
            player.setHealth(20);
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
            Queue reincarnationQueue = new Queue(plugin, "reincarnation1", reincarnation, new ArrayList<>(), 1, 3, 0);
            queueManager.registerQueue(reincarnationQueue.getQueueName(), reincarnationQueue);
        },20L);
    }

    public void glassRemoval() {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute in minecraft:reincarnation1 run gamerule send_command_feedback false");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute in minecraft:reincarnation1 run fill -16 -52 24 -21 -54 20 air replace red_stained_glass");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute in minecraft:reincarnation1 run fill -18 -52 -16 -14 -54 -21 air replace orange_stained_glass");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute in minecraft:reincarnation1 run fill 22 -52 -14 27 -54 -18 air replace yellow_stained_glass");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute in minecraft:reincarnation1 run fill 24 -52 22 20 -54 27 air replace lime_stained_glass");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute in minecraft:reincarnation1 run gamerule send_command_feedback true");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Integer lives = players.get(uuid);

        //debug
        if (lives == null) {
            Bukkit.getLogger().warning("[MadnessCup] Player " + player.getName()
                    + " died but was not in the ReincarnationBattle players map.");
            return;
        }

        lives--;
        players.put(uuid, lives);
        Bukkit.getLogger().info("[MadnessCup] " + player.getName() + " now has " + lives + " lives.");
        Location location = new Location(Bukkit.getWorld("reincarnation1"), -15.5, -54, -17.5, 0, 0);

        Bukkit.getScheduler().runTask(plugin, () -> {
            player.spigot().respawn();
            player.teleport(location);
            lifeCheck(player);
            if (isOneTeamLeft()) endGame();
        });
    }

    // Manages what happens if a player has 1 or 0 lives
    private void lifeCheck(Player player) {
        if (players.get(player.getUniqueId()) == 1) {
            KitsHandling kitsHandling = new KitsHandling(plugin, selectedKits);
            kitsHandling.startKitSelection(player);
        }
        if (players.get(player.getUniqueId()) == 0) {
            player.setGameMode(GameMode.SPECTATOR);
        }
    }

    private boolean isOneTeamLeft() {
        Team team = null;
        for (UUID uuid : players.keySet()) {
            if (players.get(uuid) > 0) {
                PlayerInfo info = plugin.getPlayerManager().getPlayer(uuid);

                if (team == null) {
                    team = info.getTeam();
                } else if (!team.equals(info.getTeam())) {
                    return false;
                }
            }
        }
        return true;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (event.getBlock().getType() == Material.RAW_GOLD_BLOCK &&
                players.containsKey(player.getUniqueId())) {
            PlayerInfo info = plugin.getPlayerManager().getPlayer(player.getUniqueId());
            info.addCoins(10);
            Bukkit.getLogger().info("Player " + player.getName() + " has coins " + info.getCoins());
        };
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player &&
                players.containsKey(player.getUniqueId())) {
            if (event.getFoodLevel() < player.getFoodLevel()) event.setCancelled(true);
        }
    }
}
