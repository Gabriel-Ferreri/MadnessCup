package Coocos.madnessCup;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import static org.bukkit.ChatColor.*;

public final class MadnessCup extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic

        ConsoleCommandSender console = getServer().getConsoleSender();
        console.sendMessage(GREEN + "MadnessCup Plugin Enabled");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        ConsoleCommandSender console = getServer().getConsoleSender();
        console.sendMessage(RED + "MadnessCup Plugin Disabled");
    }
}
