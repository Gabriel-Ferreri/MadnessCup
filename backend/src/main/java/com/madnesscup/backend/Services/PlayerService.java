package com.madnesscup.backend;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.UUID;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public void savePlayer(PlayerDTO dto) {

        Player player = new Player();

        player.setUuid(dto.getUuid());
        player.setName(dto.getName());
        player.setTeam(dto.getTeam());
        player.setCoins(dto.getCoins());
        player.setKills(dto.getKills());
        player.setDeaths(dto.getDeaths());
        player.setWins(dto.getWins());

        playerRepository.save(player);

        System.out.println("Saved player: " + player.getUuid());
    }

    public void updatePlayer(UUID uuid, PlayerDTO dto) {

        Player player = playerRepository.findById(uuid).orElse(null);

        if (player == null) {
            System.out.println("Player doesn't exist: " + uuid);
            return;
        }

        player.setName(dto.getName());
        player.setTeam(dto.getTeam());
        player.setCoins(dto.getCoins());
        player.setKills(dto.getKills());
        player.setDeaths(dto.getDeaths());
        player.setWins(dto.getWins());

        playerRepository.save(player);

        System.out.println("Updated player: " + uuid);
    }

    public PlayerDTO getPlayer(UUID uuid) {

        Player player = playerRepository.findById(uuid).orElse(null);

        if (player == null) {
            return null;
        }

        return toDTO(player);
    }

    public Collection<PlayerDTO> getAllPlayers() {
        return playerRepository.findAll().stream().map(this::toDTO).toList();
    }

    private PlayerDTO toDTO(Player player) {

        PlayerDTO dto = new PlayerDTO();

        dto.setUuid(player.getUuid());
        dto.setName(player.getName());
        dto.setTeam(player.getTeam());
        dto.setCoins(player.getCoins());
        dto.setKills(player.getKills());
        dto.setDeaths(player.getDeaths());
        dto.setWins(player.getWins());

        return dto;
    }
}