package Coocos.madnessCup.games;

import Coocos.madnessCup.MadnessCup;
import Coocos.madnessCup.systems.Game;
import Coocos.madnessCup.systems.PlayerInfo;
import Coocos.madnessCup.systems.Team;
import Coocos.madnessCup.utils.Countdown;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ReincarnationBattle extends Game {
    List<UUID> players = new ArrayList<>();
    List<UUID> alivePlayers = new ArrayList<>();
    public ReincarnationBattle(MadnessCup plugin, List<Team> teams, boolean isRunning) {
        super(plugin, teams, isRunning);
    }

    @Override
    public void startGame() {
        this.isRunning = true;
        Location redLocation = new Location(Bukkit.getWorld("game"), -18.5, -54, 22.5, 0, 0);
        Location orangeLocation = new Location(Bukkit.getWorld("game"), -15.5, -54, -17.5, 0, 0);
        Location yellowLocation = new Location(Bukkit.getWorld("game"), 24.5, -54, -15.5, 0, 0);
        Location limeLocation = new Location(Bukkit.getWorld("game"), 22.5, -54, 24.5, 0, 0);

        for (Team team : this.teams) {
            team.getPlayers().forEach(player -> {
                players.add(player);
                Player p = Bukkit.getPlayer(player);
                if (Objects.equals(team.getTeamName(), "Red Nerds")) p.teleport(redLocation);
                if (Objects.equals(team.getTeamName(), "Orange Nerds")) p.teleport(orangeLocation);
                if (Objects.equals(team.getTeamName(), "Yellow Nerds")) p.teleport(yellowLocation);
                if (Objects.equals(team.getTeamName(), "Lime Nerds")) p.teleport(limeLocation);
            });
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
        this.isRunning = false;
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
}
