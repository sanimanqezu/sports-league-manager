package com.sanele.sportsleague.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ResultRequest(
        @NotNull(message = "Fixture ID is required") Long fixtureId,
        @Min(value = 0, message = "Home goals cannot be negative") int homeGoals,
        @Min(value = 0, message = "Away goals cannot be negative") int awayGoals
) {
}
