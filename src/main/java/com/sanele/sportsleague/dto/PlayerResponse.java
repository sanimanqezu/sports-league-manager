package com.sanele.sportsleague.dto;

public record PlayerResponse(
        Long id,
        String firstName,
        String lastName,
        String position,
        int jerseyNumber,
        Long teamId,
        String teamName
) {
}
