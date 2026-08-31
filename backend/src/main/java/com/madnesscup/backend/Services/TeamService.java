package com.madnesscup.backend;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TeamService {

    private final Map<String, String> teams = new HashMap<>();

    public void saveTeams(List<String> teamNames) {

        for (String teamName : teamNames) {
            teams.put(teamName, teamName);

            System.out.println("Saved team: " + teamName);
        }
    }

    public Collection<String> getAllTeams() {
        return teams.values();
    }
}