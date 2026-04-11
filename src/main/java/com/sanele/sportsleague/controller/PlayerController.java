package com.sanele.sportsleague.controller;

import com.sanele.sportsleague.dto.PlayerRequest;
import com.sanele.sportsleague.dto.PlayerResponse;
import com.sanele.sportsleague.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@Tag(name = "Players", description = "Player management endpoints")
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping
    @Operation(summary = "Get all players")
    public ResponseEntity<List<PlayerResponse>> getAll() {
        return ResponseEntity.ok(playerService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get player by ID")
    public ResponseEntity<PlayerResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new player")
    public ResponseEntity<PlayerResponse> create(@Valid @RequestBody PlayerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playerService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a player")
    public ResponseEntity<PlayerResponse> update(@PathVariable Long id, @Valid @RequestBody PlayerRequest request) {
        return ResponseEntity.ok(playerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a player")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        playerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
