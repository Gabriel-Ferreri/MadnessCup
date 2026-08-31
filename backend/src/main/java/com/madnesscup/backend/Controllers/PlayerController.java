package com.madnesscup.backend;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping
    public void addPlayer(@RequestBody PlayerDTO player) {
        playerService.savePlayer(player);
    }

    @GetMapping
    public Collection<PlayerDTO> getPlayers() {
        return playerService.getAllPlayers();
    }

    @PutMapping("/{uuid}")
    public void updatePlayer(
            @PathVariable UUID uuid,
            @RequestBody PlayerDTO playerDTO) {

        playerService.updatePlayer(uuid, playerDTO);
    }
}