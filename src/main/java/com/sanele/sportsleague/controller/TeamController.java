package com.sanele.sportsleague.controller;

import com.sanele.sportsleague.dto.FixtureResponse;
import com.sanele.sportsleague.dto.PlayerResponse;
import com.sanele.sportsleague.dto.TeamRequest;
import com.sanele.sportsleague.dto.TeamResponse;
import com.sanele.sportsleague.service.FixtureService;
import com.sanele.sportsleague.service.PlayerService;
import com.sanele.sportsleague.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Tag(name = "Teams", description = "Team management endpoints")
public class TeamController {

    private final TeamService teamService;
    private final PlayerService playerService;
    private final FixtureService fixtureService;

    @GetMapping
    @Operation(summary = "Get all teams")
    public ResponseEntity<List<TeamResponse>> getAll() {
        return ResponseEntity.ok(teamService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get team by ID")
    public ResponseEntity<TeamResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(teamService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new team")
    public ResponseEntity<TeamResponse> create(@Valid @RequestBody TeamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a team")
    public ResponseEntity<TeamResponse> update(@PathVariable Long id, @Valid @RequestBody TeamRequest request) {
        return ResponseEntity.ok(teamService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a team")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teamService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/players")
    @Operation(summary = "Get all players for a team")
    public ResponseEntity<List<PlayerResponse>> getPlayers(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.findByTeamId(id));
    }

    @GetMapping("/{id}/fixtures")
    @Operation(summary = "Get all fixtures for a team")
    public ResponseEntity<List<FixtureResponse>> getFixtures(@PathVariable Long id) {
        return ResponseEntity.ok(fixtureService.findByTeamId(id));
    }
}
