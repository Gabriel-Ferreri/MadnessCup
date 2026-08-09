package Coocos.madnessCup.games;

import Coocos.madnessCup.MadnessCup;
import Coocos.madnessCup.systems.Game;
import Coocos.madnessCup.systems.PlayerInfo;
import Coocos.madnessCup.systems.Team;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ReincarnationBattle extends Game implements Listener{
    List<UUID> players = new ArrayList<>();
    List<UUID> alivePlayers = new ArrayList<>();
    public ReincarnationBattle(MadnessCup plugin, List<Team> teams, boolean isRunning) {
        super(plugin, teams, isRunning);
    }

    @Override
    public void startGame() {
        this.isRunning = true;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Location redLocation = new Location(Bukkit.getWorld("game"), -18.5, -54, 22.5, 0, 0);
        Location orangeLocation = new Location(Bukkit.getWorld("game"), -15.5, -54, -17.5, 0, 0);
        Location yellowLocation = new Location(Bukkit.getWorld("game"), 24.5, -54, -15.5, 0, 0);
        Location limeLocation = new Location(Bukkit.getWorld("game"), 22.5, -54, 24.5, 0, 0);
        for (Team team : this.teams) {
            for (UUID uuid : team.getPlayers()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;
                players.add(uuid);
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
        alivePlayers.addAll(players);
        Countdown countdown = new Countdown(plugin, players, 5) {
            @Override
            public void onFinish() {
                for (UUID uuid : players) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) p.sendMessage(ChatColor.GOLD + "Start fighting");
                }
            }
        };
        countdown.start();
    }


    @Override
    public void endGame() {
        HandlerList.unregisterAll(this);
        this.isRunning = false;

        for (UUID uuid : players) {
            PlayerInfo info = plugin.getPlayerManager().getPlayer(uuid);
            Player player = Bukkit.getPlayer(uuid);
            player.removeScoreboardTag("ingame");
            if (info != null && info.getTeam() != null)
                plugin.getTeamManager().removePlayerFromTeam(
                        uuid, info.getTeam().getTeamName());
        }

        players.clear();
        alivePlayers.clear();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        alivePlayers.remove(event.getEntity().getUniqueId());
        if (isOneTeamLeft()) endGame();
    }

    private boolean isOneTeamLeft() {
        Team team = null;
        for (UUID uuid : alivePlayers) {
            PlayerInfo info = plugin.getPlayerManager().getPlayer(uuid);

            if (team == null) {
                team = info.getTeam();
            } else if (!team.equals(info.getTeam())) {
                return false;
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
                players.contains(player.getUniqueId())) {
            PlayerInfo info = plugin.getPlayerManager().getPlayer(player.getUniqueId());
            info.addCoins(10);
            Bukkit.getLogger().info("Player " + player.getName() + " has coins " + info.getCoins());
        };
    }
}
