package Coocos.madnessCup;

import Coocos.madnessCup.listeners.PlayerJoinListener;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;


public final class MadnessCup extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic

        ConsoleCommandSender console = getServer().getConsoleSender();
        console.sendMessage(ChatColor.GREEN + "MadnessCup Plugin Enabled");
        // Delay world loading until server is ready
        Bukkit.getScheduler().runTask(this, () -> {
            new WorldCreator("game").createWorld();
        });
        //Event Listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        ConsoleCommandSender console = getServer().getConsoleSender();
        console.sendMessage(ChatColor.RED + "MadnessCup Plugin Disabled");
    }
}
