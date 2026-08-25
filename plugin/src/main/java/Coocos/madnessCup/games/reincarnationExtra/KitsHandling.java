package Coocos.madnessCup.games.reincarnationExtra;

import Coocos.madnessCup.MadnessCup;
import Coocos.madnessCup.systems.PlayerInfo;
import io.papermc.paper.block.BlockPredicate;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemAdventurePredicate;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.BlockTypeKeys;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.*;

/**
 * ReincarnationBattle helper class to handle kits and how players manage their
 * inventory.
 */
public class KitsHandling implements Listener {
    private final MadnessCup plugin;
    private final Map<UUID, Kit> playerChoices = new HashMap<>();
    private final Map<Kit, Integer> kitCounts = new EnumMap<>(Kit.class);
    private int maxPerKit;

    public enum Kit {
        SWORDSMAN, TANK, LUMBERJACK, UTILITY
    }

    public KitsHandling(MadnessCup plugin) {
        this.plugin = plugin;
        for (Kit kit : Kit.values()) kitCounts.put(kit, 0);
    }

    public void initialize(int playerCount) {
        this.maxPerKit = (int) Math.ceil(
                (double) playerCount / Kit.values().length
        );
    }

    public void givePlayersInventory(Player player) {
        PlayerInfo info = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        Inventory inv = player.getInventory();
        inv.clear();
        inv.setItem(0, new ItemStack(Material.STONE_SWORD));
        ItemStack pickaxe = new ItemStack(Material.IRON_PICKAXE);
        // Pickaxe can break only raw gold
        BlockPredicate predicate = BlockPredicate.predicate().blocks(RegistrySet.keySet(RegistryKey.BLOCK, BlockTypeKeys.RAW_GOLD_BLOCK,BlockTypeKeys.GOLD_BLOCK)).build();
        ItemAdventurePredicate canBreak = ItemAdventurePredicate.itemAdventurePredicate().addPredicate(predicate).build();
        pickaxe.setData(DataComponentTypes.CAN_BREAK, canBreak);
        inv.setItem(1, pickaxe);

        // Armor creation
        Color teamColor = info.getTeam().getCustomizeColor();
        player.getInventory().setBoots(customizeArmor(Material.LEATHER_BOOTS, teamColor, 2));
        player.getInventory().setLeggings(customizeArmor(Material.LEATHER_LEGGINGS, teamColor,2));
        player.getInventory().setChestplate(customizeArmor(Material.LEATHER_CHESTPLATE, teamColor,2));
        player.getInventory().setHelmet(customizeArmor(Material.LEATHER_HELMET, teamColor,2));
        player.updateInventory();
    }

    public void startKitSelection(Player player) {
        player.setGameMode(GameMode.SPECTATOR);

        openChoice(player);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.closeInventory();
            UUID uuid = player.getUniqueId();
            Kit chosenKit = playerChoices.get(uuid);

            if (chosenKit == null) {
                for (Kit kit : Kit.values()) {
                    if (kitCounts.get(kit) < maxPerKit) {
                        chosenKit = kit;
                        break;
                    }
                }
            }
            if (chosenKit == null) {
                Bukkit.getLogger().warning("[MadnessCup] No kit available for " + player.getName());
                player.setGameMode(GameMode.ADVENTURE);
                return;
            }
            kitCounts.put(chosenKit, kitCounts.get(chosenKit) + 1);
            switch (chosenKit) {
                case SWORDSMAN -> swordsmanKit(player);
                case TANK -> tankKit(player);
                case LUMBERJACK -> lumbermanKit(player);
                case UTILITY -> utilityKit(player);
            }

            playerChoices.remove(uuid);
            player.setGameMode(GameMode.ADVENTURE);
            PlayerInfo info = plugin.getPlayerManager().getPlayer(uuid);
            Location newLocation = null;
            switch (info.getTeam().getTeamName()) {
                case "Red Nerds" -> newLocation = new Location(Bukkit.getWorld("reincarnation1"), -18.5, -54, 22.5, 0, 0);
                case "Orange Nerds" -> newLocation = new Location(Bukkit.getWorld("reincarnation1"), -15.5, -54, -17.5, 0, 0);
                case "Yellow Nerds" -> newLocation = new Location(Bukkit.getWorld("reincarnation1"), 24.5, -54, -15.5, 0, 0);
                case "Lime Nerds" -> newLocation = new Location(Bukkit.getWorld("reincarnation1"), 22.5, -54, 24.5, 0, 0);
            }
            if ( newLocation != null ) player.teleport(newLocation);
        }, 160L);
    }

    public void openChoice(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9,
                Component.text("Choose your kit"));
        // Swordsman
        if (kitCounts.get(Kit.SWORDSMAN) >= maxPerKit) inv.setItem(2, createKitItem(Material.BARRIER, "Swordsman - Taken"));
        else inv.setItem(2, createKitItem(Material.IRON_SWORD, "Swordsman"));
        // Tank
        if (kitCounts.get(Kit.TANK) >= maxPerKit) inv.setItem(3, createKitItem(Material.BARRIER, "Tank - Taken"));
        else inv.setItem(3, createKitItem(Material.SHIELD, "Tank"));
        // Lumberjack
        if (kitCounts.get(Kit.LUMBERJACK) >= maxPerKit) inv.setItem(5, createKitItem(Material.BARRIER, "Lumberjack - Taken"));
        else inv.setItem(5, createKitItem(Material.IRON_AXE, "Lumberjack"));
        // Utility
        if (kitCounts.get(Kit.UTILITY) >= maxPerKit) inv.setItem(6, createKitItem(Material.BARRIER, "Utility - Taken"));
        else inv.setItem(6, createKitItem(Material.TOTEM_OF_UNDYING, "Utility"));
        player.openInventory(inv);
    }

    private ItemStack createKitItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(name));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack customizeArmor(Material material, Color color, Integer level) {
        ItemStack item = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();

        meta.setColor(color);
        item.setItemMeta(meta);

        item.addUnsafeEnchantment(Enchantment.PROTECTION, level);
        item.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!plugin.isInGame(player)) return;
        if (!event.getView().getTitle().equals("Choose your kit")) return;
        event.setCancelled(true);

        switch (event.getSlot()) {
            case 2 -> {
                if (kitCounts.get(Kit.SWORDSMAN) >= maxPerKit) return;
                playerChoices.put(player.getUniqueId(), Kit.SWORDSMAN);
                swordsmanKit(player);
            }
            case 3 -> {
                if (kitCounts.get(Kit.TANK) >= maxPerKit) return;
                playerChoices.put(player.getUniqueId(), Kit.TANK);
                tankKit(player);
            }
            case 5 -> {
                if (kitCounts.get(Kit.LUMBERJACK) >= maxPerKit) return;
                playerChoices.put(player.getUniqueId(), Kit.LUMBERJACK);
                lumbermanKit(player);
            }
            case 6 -> {
                if (kitCounts.get(Kit.UTILITY) >= maxPerKit) return;
                playerChoices.put(player.getUniqueId(), Kit.UTILITY);
                utilityKit(player);
            }
        }
        Bukkit.getLogger().info(ChatColor.RED + "playerChoices = " + playerChoices);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        if (!plugin.isInGame(player)) return;
        if (!event.getView().getTitle().equals("Choose your kit")) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.getGameMode() == GameMode.SPECTATOR) openChoice(player);
        });
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (!plugin.isInGame(player)) return;
        if (!event.getView().getTitle().equals("Choose your kit")) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (!plugin.isInGame(player)) return;
        event.setCancelled(true);
    }

    public void utilityKit(Player player) {
        givePlayersInventory(player);
        Inventory inv = player.getInventory();
        ItemStack sword = new ItemStack(Material.STONE_SWORD);
        sword.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        inv.setItem(0, sword);
        ItemStack potion = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.setBasePotionType(PotionType.STRONG_HEALING);
        potion.setItemMeta(meta);
        inv.setItem(2, potion);
        meta.setBasePotionType(PotionType.STRONG_HARMING);
        potion.setItemMeta(meta);
        inv.setItem(3, potion);
    }

    public void swordsmanKit(Player player) {
        givePlayersInventory(player);
        Inventory inv = player.getInventory();
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        inv.setItem(0, sword);
    }

    public void tankKit(Player player) {
        givePlayersInventory(player);
        Inventory inv = player.getInventory();
        ItemStack sword = new ItemStack(Material.STONE_SWORD);
        sword.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        inv.setItem(0, sword);
        PlayerInfo info = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        Color teamColor = info.getTeam().getCustomizeColor();
        player.getInventory().setBoots(customizeArmor(Material.LEATHER_BOOTS, teamColor, 4));
        player.getInventory().setLeggings(customizeArmor(Material.LEATHER_LEGGINGS, teamColor,4));
        player.getInventory().setChestplate(customizeArmor(Material.LEATHER_CHESTPLATE, teamColor,4));
        player.getInventory().setHelmet(customizeArmor(Material.LEATHER_HELMET, teamColor,4));
    }

    public void lumbermanKit(Player player) {
        givePlayersInventory(player);
        Inventory inv = player.getInventory();
        inv.setItem(0, new ItemStack(Material.IRON_AXE));
    }
}
