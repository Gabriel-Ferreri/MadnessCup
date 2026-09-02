package com.madnesscup.backend;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

@RestController
@RequestMapping("/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public void addTeam(@RequestBody TeamDTO team) {
        teamService.saveTeam(team);
    }

    @GetMapping
    public Collection<TeamDTO> getTeams() {
        return teamService.getAllTeams();
    }

    @PutMapping("/{teamName}")
    public void updateTeam(
            @PathVariable String teamName,
            @RequestBody TeamDTO teamDTO) {

            teamService.updateTeam(teamName, teamDTO);
    }
}