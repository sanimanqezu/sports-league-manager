package com.sanele.sportsleague.controller;

import com.sanele.sportsleague.dto.ResultRequest;
import com.sanele.sportsleague.dto.ResultResponse;
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
@RequestMapping("/api/results")
@RequiredArgsConstructor
@Tag(name = "Results", description = "Match result recording endpoints")
public class ResultController {

    private final ResultService resultService;

    @GetMapping
    @Operation(summary = "Get all recorded results")
    public ResponseEntity<List<ResultResponse>> getAll() {
        return ResponseEntity.ok(resultService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get result by ID")
    public ResponseEntity<ResultResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(resultService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Record a match result (marks fixture as played)")
    public ResponseEntity<ResultResponse> recordResult(@Valid @RequestBody ResultRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resultService.recordResult(request));
    }
}
