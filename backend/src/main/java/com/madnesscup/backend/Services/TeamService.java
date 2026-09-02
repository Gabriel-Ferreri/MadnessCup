package com.madnesscup.backend;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) { this.teamRepository = teamRepository; }

    public void saveTeam(TeamDTO dto) {

        Team team = new Team();

        team.setTeamName(dto.getTeamName());
        team.setPlayers(dto.getPlayers());
        team.setTeamColor(dto.getTeamColor());
        team.setCustomizeColor(dto.getCustomizeColor());
        team.setTeamCoins(dto.getTeamCoins());
        team.setTeamLimit(dto.getTeamLimit());

        teamRepository.save(team);

        System.out.println("Saved team: " + team.getTeamName());
    }

    public TeamDTO getTeam(String teamName) {
        Team team = teamRepository.findById(teamName).orElse(null);
        if (team == null) return null;

        return toDTO(team);
    }

    public Collection<TeamDTO> getAllTeams() {
        return teamRepository.findAll().stream().map(this::toDTO).toList();
    }

    public void updateTeam(String teamName, TeamDTO dto) {

        Team team = teamRepository.findById(teamName).orElse(null);

        if (team == null) {
            System.out.println("Team doesn't exist: " + teamName);
            return;
        }

        team.setPlayers(dto.getPlayers());
        team.setTeamColor(dto.getTeamColor());
        team.setCustomizeColor(dto.getCustomizeColor());
        team.setTeamCoins(dto.getTeamCoins());
        team.setTeamLimit(dto.getTeamLimit());

        teamRepository.save(team);

        System.out.println("Updated team: " + teamName);
    }

    private TeamDTO toDTO(Team team) {

        TeamDTO dto = new TeamDTO();

        dto.setTeamName(team.getTeamName());
        dto.setPlayers(team.getPlayers());
        dto.setTeamColor(team.getTeamColor());
        dto.setCustomizeColor(team.getCustomizeColor());
        dto.setTeamCoins(team.getTeamCoins());
        dto.setTeamLimit(team.getTeamLimit());

        return dto;
    }
}