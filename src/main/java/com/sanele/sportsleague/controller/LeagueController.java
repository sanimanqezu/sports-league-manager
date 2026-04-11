package com.sanele.sportsleague.controller;

import com.sanele.sportsleague.dto.LeagueRequest;
import com.sanele.sportsleague.dto.LeagueResponse;
import com.sanele.sportsleague.dto.StandingEntry;
import com.sanele.sportsleague.service.LeagueService;
import com.sanele.sportsleague.service.ResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leagues")
@RequiredArgsConstructor
@Tag(name = "Leagues", description = "League management endpoints")
public class LeagueController {

    private final LeagueService leagueService;
    private final ResultService resultService;

    @GetMapping
    @Operation(summary = "Get all leagues")
    public ResponseEntity<List<LeagueResponse>> getAll() {
        return ResponseEntity.ok(leagueService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get league by ID")
    public ResponseEntity<LeagueResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(leagueService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new league")
    public ResponseEntity<LeagueResponse> create(@Valid @RequestBody LeagueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leagueService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a league")
    public ResponseEntity<LeagueResponse> update(@PathVariable Long id, @Valid @RequestBody LeagueRequest request) {
        return ResponseEntity.ok(leagueService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a league")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        leagueService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/standings")
    @Operation(summary = "Get league standings sorted by points and goal difference")
    public ResponseEntity<List<StandingEntry>> getStandings(@PathVariable Long id) {
        return ResponseEntity.ok(resultService.computeStandings(id));
    }
}
