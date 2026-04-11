package com.sanele.sportsleague.controller;

import com.sanele.sportsleague.dto.FixtureRequest;
import com.sanele.sportsleague.dto.FixtureResponse;
import com.sanele.sportsleague.service.FixtureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fixtures")
@RequiredArgsConstructor
@Tag(name = "Fixtures", description = "Fixture management endpoints")
public class FixtureController {

    private final FixtureService fixtureService;

    @GetMapping
    @Operation(summary = "Get all fixtures")
    public ResponseEntity<List<FixtureResponse>> getAll() {
        return ResponseEntity.ok(fixtureService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get fixture by ID")
    public ResponseEntity<FixtureResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(fixtureService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new fixture")
    public ResponseEntity<FixtureResponse> create(@Valid @RequestBody FixtureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fixtureService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a fixture (only if not yet played)")
    public ResponseEntity<FixtureResponse> update(@PathVariable Long id, @Valid @RequestBody FixtureRequest request) {
        return ResponseEntity.ok(fixtureService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a fixture")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fixtureService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
