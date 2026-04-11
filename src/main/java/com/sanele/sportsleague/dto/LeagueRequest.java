package com.sanele.sportsleague.dto;

import jakarta.validation.constraints.NotBlank;

public record LeagueRequest(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Season is required") String season,
        @NotBlank(message = "Sport is required") String sport
) {
}
