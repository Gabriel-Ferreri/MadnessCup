package com.madnesscup.backend;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Team {

    @Id
    private String teamName;

    private String teamColor;
    private String customizeColor;
    private Integer teamCoins;
    private Integer teamLimit;
}