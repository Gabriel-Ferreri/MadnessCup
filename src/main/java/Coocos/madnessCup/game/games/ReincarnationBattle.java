package Coocos.madnessCup.game.games;

import Coocos.madnessCup.MadnessCup;
import Coocos.madnessCup.game.Game;
import java.util.List;
import java.util.UUID;

public class ReincarnationBattle extends Game {
    public ReincarnationBattle(MadnessCup plugin, List<UUID> players, boolean isRunning) {
        super(plugin, players, isRunning);
    }

    @Override
    public void startGame() {
        this.isRunning = true;
    }


    @Override
    public void endGame() {
        this.isRunning = false;
    }
}
