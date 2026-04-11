package com.sanele.sportsleague.dto;

import java.time.LocalDateTime;

public record FixtureResponse(
        Long id,
        Long homeTeamId,
        String homeTeamName,
        Long awayTeamId,
        String awayTeamName,
        Long leagueId,
        String leagueName,
        LocalDateTime matchDate,
        boolean played
) {
}
