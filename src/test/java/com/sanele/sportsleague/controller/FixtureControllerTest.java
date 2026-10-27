package com.sanele.sportsleague.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanele.sportsleague.dto.FixtureRequest;
import com.sanele.sportsleague.dto.FixtureResponse;
import com.sanele.sportsleague.exception.BadRequestException;
import com.sanele.sportsleague.exception.ResourceNotFoundException;
import com.sanele.sportsleague.service.FixtureService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FixtureController.class)
@DisplayName("FixtureController Slice Tests")
class FixtureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FixtureService fixtureService;

    @Test
    @DisplayName("POST /api/fixtures - returns 201 when the request is valid")
    void create_returnsCreated() throws Exception {
        FixtureRequest request = new FixtureRequest(1L, 2L, 1L, LocalDateTime.now().plusDays(1));
        FixtureResponse response = new FixtureResponse(
                10L, 1L, "Arsenal", 2L, "Chelsea", 1L, "Premier League", request.matchDate(), false);
        when(fixtureService.create(any(FixtureRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/fixtures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.homeTeamName").value("Arsenal"));
    }

    @Test
    @DisplayName("POST /api/fixtures - maps BadRequestException to 400")
    void create_businessRuleViolation() throws Exception {
        FixtureRequest request = new FixtureRequest(1L, 2L, 1L, LocalDateTime.now().plusDays(1));
        when(fixtureService.create(any(FixtureRequest.class)))
                .thenThrow(new BadRequestException("Home team and away team cannot be the same"));

        mockMvc.perform(post("/api/fixtures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }

    @Test
    @DisplayName("POST /api/fixtures - returns 400 when required fields are missing")
    void create_validationFails() throws Exception {
        FixtureRequest request = new FixtureRequest(null, null, null, null);

        mockMvc.perform(post("/api/fixtures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"));

        verify(fixtureService, never()).create(any());
    }

    @Test
    @DisplayName("GET /api/fixtures/{id} - maps ResourceNotFoundException to 404")
    void getById_notFound() throws Exception {
        when(fixtureService.findById(99L)).thenThrow(new ResourceNotFoundException("Fixture", 99L));

        mockMvc.perform(get("/api/fixtures/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }
}
