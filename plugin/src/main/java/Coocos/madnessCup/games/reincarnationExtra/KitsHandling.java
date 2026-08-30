package Coocos.madnessCup.games.reincarnationExtra;

import Coocos.madnessCup.MadnessCup;
import Coocos.madnessCup.systems.PlayerInfo;
import io.papermc.paper.block.BlockPredicate;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DamageResistant;
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
import org.bukkit.potion.PotionEffect;
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

    /**
     * Calculate the amount of times a kit can be picked before it gets blocked
     * @param playerCount Number of players in the game
     */
    public void initialize(int playerCount) {
        this.maxPerKit = (int) Math.ceil(
                (double) playerCount / Kit.values().length
        );
    }

    /**
     * Give a player the basic first life inventory
     * @param player The player who is getting the basic inventory
     */
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
                        kitCounts.put(kit, kitCounts.get(kit) + 1);
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
            switch (chosenKit) {
                case SWORDSMAN -> swordsmanKit(player);
                case TANK -> tankKit(player);
                case LUMBERJACK -> lumbermanKit(player);
                case UTILITY -> utilityKit(player);
            }

            playerChoices.remove(uuid);
            player.setGameMode(GameMode.ADVENTURE);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "You are now immune for 5 seconds");
            player.setNoDamageTicks(100);
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

    /**
     * Open the choice menu of available kits
     * @param player The player who has to select a kit
     */
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

    /**
     * Store which kit was selected by a player
     * @param player The player selecting a kit
     * @param kit Kit selected by the player
     */
    private void selectKit(Player player, Kit kit) {
        if (kitCounts.get(kit) >= maxPerKit) {
            player.sendMessage(ChatColor.RED + "This kit has already been taken!");
            return;
        }
        UUID uuid = player.getUniqueId();
        playerChoices.put(uuid, kit);
        kitCounts.put(kit, kitCounts.get(kit) + 1);

        player.closeInventory();
        player.sendMessage(ChatColor.LIGHT_PURPLE + "You selected the " + kit + " Kit!");
        updateAllKitMenus();
    }

    /**
     * Update the menu view of players with the menu already open
     */
    private void updateAllKitMenus() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getOpenInventory().getTitle().equals("Choose your kit")) continue;
            Inventory inv = player.getOpenInventory().getTopInventory();

            if (kitCounts.get(Kit.SWORDSMAN) >= maxPerKit) inv.setItem(2, createKitItem(Material.BARRIER, "Swordsman - Taken"));
            else inv.setItem(2, createKitItem(Material.IRON_SWORD, "Swordsman"));

            if (kitCounts.get(Kit.TANK) >= maxPerKit) inv.setItem(3, createKitItem(Material.BARRIER, "Tank - Taken"));
            else inv.setItem(3, createKitItem(Material.SHIELD, "Tank"));

            if (kitCounts.get(Kit.LUMBERJACK) >= maxPerKit) inv.setItem(5, createKitItem(Material.BARRIER, "Lumberjack - Taken"));
            else inv.setItem(5, createKitItem(Material.IRON_AXE, "Lumberjack"));

            if (kitCounts.get(Kit.UTILITY) >= maxPerKit) inv.setItem(6, createKitItem(Material.BARRIER, "Utility - Taken"));
            else inv.setItem(6, createKitItem(Material.TOTEM_OF_UNDYING, "Utility"));
        }
    }
    /**
     * Create an item with a custom material and name
     * @param material Item material
     * @param name Item name
     * @return the item
     */
    private ItemStack createKitItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(name));
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Create an armor item with a custom material color and protection level
     * @param material Armor material
     * @param color Armor color
     * @param level Armor protection level
     * @return armor item
     */
    private ItemStack customizeArmor(Material material, Color color, Integer level) {
        ItemStack item = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();

        meta.setColor(color);
        item.setItemMeta(meta);

        item.addUnsafeEnchantment(Enchantment.PROTECTION, level);
        item.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
        return item;
    }

    /**
     * When a player clicks in their inventory check if they select a slot which
     * contains a kit
     * @param event The inventory click event
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (!plugin.isInGame(player)) return;
        if (!event.getView().getTitle().equals("Choose your kit")) return;

        event.setCancelled(true);
        switch (event.getSlot()) {
            case 2 -> selectKit(player, Kit.SWORDSMAN);
            case 3 -> selectKit(player, Kit.TANK);
            case 5 -> selectKit(player, Kit.LUMBERJACK);
            case 6 -> selectKit(player, Kit.UTILITY);
        }

        Bukkit.getLogger().info(ChatColor.RED + "playerChoices = " + playerChoices);;
    }

    /**
     * Event handler to prevent players from closing the kit selection inventory
     * @param event inventory closing event
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        if (!plugin.isInGame(player)) return;
        if (!event.getView().getTitle().equals("Choose your kit")) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            UUID uuid = player.getUniqueId();
            if (playerChoices.get(uuid) == null) openChoice(player);
        });
    }

    /**
     * Event handler to prevent players from dragging items in their inventory
     * @param event inventory drag event
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (!plugin.isInGame(player)) return;
        if (!event.getView().getTitle().equals("Choose your kit")) return;
        event.setCancelled(true);
    }

    /**
     * Event handler to prevent players from dropping inventory items
     * @param event drop event
     */
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (!plugin.isInGame(player)) return;
        event.setCancelled(true);
    }

    /**
     * Utility kit creator
     * @param player Player getting the utility kit
     */
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

    /**
     * Swordsman kit creator
     * @param player Player getting the swordsman kit
     */
    public void swordsmanKit(Player player) {
        givePlayersInventory(player);
        Inventory inv = player.getInventory();
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        inv.setItem(0, sword);
    }

    /**
     * Tank kit creator
     * @param player Player getting the tank kit
     */
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

    /**
     * Lumberjack kit creator
     * @param player Player getting the lumberjack kit
     */
    public void lumbermanKit(Player player) {
        givePlayersInventory(player);
        Inventory inv = player.getInventory();
        inv.setItem(0, new ItemStack(Material.IRON_AXE));
    }
}
