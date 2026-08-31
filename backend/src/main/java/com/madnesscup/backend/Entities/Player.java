package com.madnesscup.backend;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class Player {

    @Id
    private UUID uuid;

    private String name;
    private String team;
    private Integer coins;
    private Integer kills;
    private Integer deaths;
    private Integer wins;
}