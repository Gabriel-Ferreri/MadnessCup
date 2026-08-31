package com.madnesscup.backend;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class PlayerDTO {

    private UUID uuid;
    private String name;
    private String team;
    private Integer coins;
    private Integer kills;
    private Integer deaths;
    private Integer wins;
}