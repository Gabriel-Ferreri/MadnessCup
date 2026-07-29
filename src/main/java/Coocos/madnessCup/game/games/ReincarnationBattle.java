package Coocos.madnessCup.game.games;

import Coocos.madnessCup.MadnessCup;
import Coocos.madnessCup.game.Game;
import Coocos.madnessCup.game.PlayerInfo;
import Coocos.madnessCup.game.Team;
import Coocos.madnessCup.utils.Countdown;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReincarnationBattle extends Game {
    List<UUID> players = new ArrayList<>();
    public ReincarnationBattle(MadnessCup plugin, List<Team> teams, boolean isRunning) {
        super(plugin, teams, isRunning);
    }

    @Override
    public void startGame() {
        this.isRunning = true;
        Location gameLocation = new Location(
                Bukkit.getWorld("game"), 9.5, -54, -10.5, 0, 0);
        for (Team team : this.teams) {
            team.getPlayers().forEach(player -> {
                players.add(player);
                Player p = Bukkit.getPlayer(player);
                if (p != null) p.teleport(gameLocation);
            });
        }
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
        this.isRunning = false;
        Location gameLocation = new Location(
                Bukkit.getWorld("World"), 8.5, -56, 8.5, 0, 0);
        for (Team team : this.teams) {
            team.getPlayers().forEach(player -> {
                players.add(player);
                Player p = Bukkit.getPlayer(player);
                if (p != null) p.teleport(gameLocation);
            });
        }
    }
}
