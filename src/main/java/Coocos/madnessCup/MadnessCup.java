package Coocos.madnessCup;

import Coocos.madnessCup.game.Game;
import Coocos.madnessCup.game.Queue;
import Coocos.madnessCup.game.games.ReincarnationBattle;
import Coocos.madnessCup.listeners.PlayerJoinListener;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

import java.util.Collections;

public final class MadnessCup extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic

        ConsoleCommandSender console = getServer().getConsoleSender();
        console.sendMessage(ChatColor.GREEN + "MadnessCup Plugin Enabled");

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
