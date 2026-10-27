package com.sanele.sportsleague.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanele.sportsleague.dto.LeagueRequest;
import com.sanele.sportsleague.dto.LeagueResponse;
import com.sanele.sportsleague.dto.StandingEntry;
import com.sanele.sportsleague.exception.ResourceNotFoundException;
import com.sanele.sportsleague.service.LeagueService;
import com.sanele.sportsleague.service.ResultService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LeagueController.class)
@DisplayName("LeagueController Slice Tests")
class LeagueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LeagueService leagueService;

    @MockitoBean
    private ResultService resultService;

    @Test
    @DisplayName("GET /api/leagues/{id} - returns 200 with the league")
    void getById_returnsLeague() throws Exception {
        LeagueResponse response = new LeagueResponse(1L, "Premier League", "2024/25", "Football", LocalDateTime.now());
        when(leagueService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/leagues/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Premier League"));
    }

    @Test
    @DisplayName("GET /api/leagues/{id} - maps ResourceNotFoundException to 404")
    void getById_notFound() throws Exception {
        when(leagueService.findById(99L)).thenThrow(new ResourceNotFoundException("League", 99L));

        mockMvc.perform(get("/api/leagues/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("99")));
    }

    @Test
    @DisplayName("POST /api/leagues - returns 201 and delegates to the service")
    void create_returnsCreated() throws Exception {
        LeagueRequest request = new LeagueRequest("La Liga", "2024/25", "Football");
        LeagueResponse response = new LeagueResponse(5L, "La Liga", "2024/25", "Football", LocalDateTime.now());
        when(leagueService.create(any(LeagueRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/leagues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("La Liga"));

        verify(leagueService).create(any(LeagueRequest.class));
    }

    @Test
    @DisplayName("POST /api/leagues - returns 400 when the body is invalid")
    void create_validationFails() throws Exception {
        LeagueRequest request = new LeagueRequest("", "", "");

        mockMvc.perform(post("/api/leagues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"));

        verify(leagueService, never()).create(any());
    }

    @Test
    @DisplayName("GET /api/leagues/{id}/standings - returns 200 with the standings table")
    void getStandings_returnsTable() throws Exception {
        StandingEntry entry = new StandingEntry(1L, "Arsenal", 1, 1, 0, 0, 3, 1, 2, 3);
        when(resultService.computeStandings(1L)).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/leagues/1/standings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("Arsenal"))
                .andExpect(jsonPath("$[0].points").value(3));
    }

    @Test
    @DisplayName("DELETE /api/leagues/{id} - returns 204")
    void delete_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/leagues/1"))
                .andExpect(status().isNoContent());

        verify(leagueService).delete(eq(1L));
    }
}
