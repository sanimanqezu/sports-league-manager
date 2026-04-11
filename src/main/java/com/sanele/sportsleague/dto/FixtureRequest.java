package com.sanele.sportsleague.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record FixtureRequest(
        @NotNull(message = "Home team ID is required") Long homeTeamId,
        @NotNull(message = "Away team ID is required") Long awayTeamId,
        @NotNull(message = "League ID is required") Long leagueId,
        @NotNull(message = "Match date is required") LocalDateTime matchDate
) {
}
