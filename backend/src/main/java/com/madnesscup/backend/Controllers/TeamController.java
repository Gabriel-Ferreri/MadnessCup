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
    public void addTeams(@RequestBody List<String> teamNames) {
        teamService.saveTeams(teamNames);
    }

    @GetMapping
    public Collection<String> getTeams() {
        return teamService.getAllTeams();
    }
}