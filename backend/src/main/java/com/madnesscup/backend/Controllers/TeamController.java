package com.madnesscup.backend;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/teams")
public class TeamController {

    private final List<String> teams = new ArrayList<>();

    @PostMapping
    public void addTeam(@RequestBody List<String> teamNames) {

        teams.addAll(teamNames);

        for (String team : teamNames) {
            System.out.println("Added team: " + team);
        }
    }

    @GetMapping
    public List<String> getTeams() {

        return teams;
    }
}