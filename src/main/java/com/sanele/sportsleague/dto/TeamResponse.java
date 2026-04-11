package com.sanele.sportsleague.dto;

public record TeamResponse(
        Long id,
        String name,
        String city,
        Long leagueId,
        String leagueName
) {
}
