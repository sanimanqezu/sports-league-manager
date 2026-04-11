package com.sanele.sportsleague.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TeamRequest(
        @NotBlank(message = "Team name is required") String name,
        @NotBlank(message = "City is required") String city,
        @NotNull(message = "League ID is required") Long leagueId
) {
}
