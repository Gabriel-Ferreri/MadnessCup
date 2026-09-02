package com.madnesscup.backend;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Entity
@Data
public class Team {

    @Id
    private String teamName;

    private List<UUID> players;
    private String teamColor;
    private Integer customizeColor;
    private Integer teamCoins;
    private Integer teamLimit;
}