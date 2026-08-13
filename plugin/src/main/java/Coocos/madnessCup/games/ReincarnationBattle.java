package Coocos.madnessCup.games;

import Coocos.madnessCup.MadnessCup;
import Coocos.madnessCup.systems.Game;
import Coocos.madnessCup.systems.PlayerInfo;
import Coocos.madnessCup.systems.Queue;
import Coocos.madnessCup.systems.Team;
import Coocos.madnessCup.systems.managers.QueueManager;
import Coocos.madnessCup.utils.Countdown;
import Coocos.madnessCup.utils.ItemFactory;
import io.papermc.paper.block.BlockPredicate;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemAdventurePredicate;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.BlockTypeKeys;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import org.bukkit.*;
import org.bukkit.block.BlockType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class ReincarnationBattle extends Game implements Listener{
    Map<UUID, Integer> players = new HashMap<>();
    public ReincarnationBattle(MadnessCup plugin, List<Team> teams, boolean isRunning) {
        super(plugin, teams, isRunning);
    }

    @Override
    public void startGame() {
        this.isRunning = true;
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
                givePlayersInventory(player);
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

        for (UUID uuid : players.keySet()) {
            PlayerInfo info = plugin.getPlayerManager().getPlayer(uuid);
            Player player = Bukkit.getPlayer(uuid);
            player.removeScoreboardTag("ingame");
            if (info != null && info.getTeam() != null)
                plugin.getTeamManager().removePlayerFromTeam(
                        uuid, info.getTeam().getTeamName());
        }

        players.clear();
        QueueManager queueManager = plugin.getQueueManager();
        queueManager.removeQueue("reincarnation1");
        Game reincarnation = new ReincarnationBattle(plugin, new ArrayList<>(), false);
        Queue reincarnationQueue = new Queue(plugin, "reincarnation1", reincarnation, new ArrayList<>(), 2, 3, 0);
        queueManager.registerQueue(reincarnationQueue.getQueueName(), reincarnationQueue);

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
        players.put(event.getPlayer().getUniqueId(), players.get(event.getPlayer().getUniqueId()) - 1);
        if (isOneTeamLeft()) endGame();
    }

    private boolean isOneTeamLeft() {
        Team team = null;
        for (UUID uuid : players.keySet()) {
            if (players.get(uuid) >= 2) {
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

    public void givePlayersInventory(Player player) {
        PlayerInfo info = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        Inventory inv = player.getInventory();
        inv.clear();
        inv.setItem(0, new ItemStack(Material.STONE_SWORD));
        ItemStack pickaxe = new ItemStack(Material.IRON_PICKAXE);
        // Pickaxe can break only raw gold
        BlockPredicate predicate = BlockPredicate.predicate().blocks(RegistrySet.keySet(RegistryKey.BLOCK, BlockTypeKeys.RAW_GOLD_BLOCK)).build();
        ItemAdventurePredicate canBreak = ItemAdventurePredicate.itemAdventurePredicate().addPredicate(predicate).build();
        pickaxe.setData(DataComponentTypes.CAN_BREAK, canBreak);
        inv.setItem(1, pickaxe);
        inv.setItem(2, new ItemStack(Material.COOKED_BEEF, 16));

        // Armor creation
        Color teamColor = info.getTeam().getCustomizeColor();
        player.getInventory().setBoots(customizeArmor(Material.LEATHER_BOOTS, teamColor));
        player.getInventory().setLeggings(customizeArmor(Material.LEATHER_LEGGINGS, teamColor));
        player.getInventory().setChestplate(customizeArmor(Material.LEATHER_CHESTPLATE, teamColor));
        player.getInventory().setHelmet(customizeArmor(Material.LEATHER_HELMET, teamColor));

    }

    private ItemStack customizeArmor(Material material, Color color) {
        ItemStack item = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();

        meta.setColor(color);
        item.setItemMeta(meta);

        item.addUnsafeEnchantment(Enchantment.PROTECTION, 2);
        item.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
        return item;
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
}
