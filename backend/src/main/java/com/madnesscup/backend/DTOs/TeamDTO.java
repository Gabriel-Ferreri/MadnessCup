package com.madnesscup.backend;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class TeamDTO {

    private String teamName;
    private List<UUID> players;
    private String teamColor;
    private Integer teamCoins;
    private Integer teamLimit;
}