package com.sanele.sportsleague.dto;

import java.time.LocalDateTime;

public record ResultResponse(
        Long id,
        Long fixtureId,
        String homeTeamName,
        String awayTeamName,
        int homeGoals,
        int awayGoals,
        LocalDateTime recordedAt
) {
}
