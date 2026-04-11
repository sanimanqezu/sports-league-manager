package com.sanele.sportsleague.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PlayerRequest(
        @NotBlank(message = "First name is required") String firstName,
        @NotBlank(message = "Last name is required") String lastName,
        @NotBlank(message = "Position is required") String position,
        @Positive(message = "Jersey number must be positive") int jerseyNumber,
        @NotNull(message = "Team ID is required") Long teamId
) {
}
