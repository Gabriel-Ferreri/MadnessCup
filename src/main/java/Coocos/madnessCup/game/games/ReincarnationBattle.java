package Coocos.madnessCup.game.games;

import Coocos.madnessCup.MadnessCup;
import Coocos.madnessCup.game.Game;
import Coocos.madnessCup.utils.Countdown;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ReincarnationBattle extends Game {
    public ReincarnationBattle(MadnessCup plugin, List<UUID> players, boolean isRunning) {
        super(plugin, players, isRunning);
    }

    @Override
    public void startGame() {
        this.isRunning = true;

        Countdown countdown = new Countdown(plugin, players, 5) {
            @Override
            public void onFinish() {
                Location gameLocation = new Location(
                        Bukkit.getWorld("game"), 9.5, -57, -10.5, 0, 0);

                for (UUID uuid : players) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) p.teleport(gameLocation);
                }
            }
        };
        countdown.start();
    }


    @Override
    public void endGame() {
        this.isRunning = false;
    }
}
