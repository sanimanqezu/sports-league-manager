package com.sanele.sportsleague.dto;

import java.time.LocalDateTime;

public record LeagueResponse(
        Long id,
        String name,
        String season,
        String sport,
        LocalDateTime createdAt
) {
}
