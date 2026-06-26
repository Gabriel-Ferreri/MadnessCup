package Coocos.madnessCup.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class ItemFactory {
    public static NamespacedKey KEY_MENU;

    public static void init(JavaPlugin plugin) {
        KEY_MENU = new NamespacedKey(plugin, "menu_item");
    }

    public static ItemStack createPaper(String displayName, String lore, String key) {
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + displayName);
        meta.setLore(Arrays.asList(ChatColor.GRAY + lore));
        meta.getPersistentDataContainer().set(KEY_MENU, PersistentDataType.STRING, key);
        paper.setItemMeta(meta);
        return paper;
    }

}