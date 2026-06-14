package Coocos.madnessCup;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

public final class MadnessCup extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic

        ConsoleCommandSender console = getServer().getConsoleSender();
        console.sendMessage(ChatColor.GREEN + "MadnessCup Plugin Enabled");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        ConsoleCommandSender console = getServer().getConsoleSender();
        console.sendMessage(ChatColor.RED + "MadnessCup Plugin Disabled");
    }
}
