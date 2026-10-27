package com.sanele.sportsleague.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanele.sportsleague.dto.ResultRequest;
import com.sanele.sportsleague.dto.ResultResponse;
import com.sanele.sportsleague.exception.BadRequestException;
import com.sanele.sportsleague.exception.ResourceNotFoundException;
import com.sanele.sportsleague.service.ResultService;
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

@WebMvcTest(ResultController.class)
@DisplayName("ResultController Slice Tests")
class ResultControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ResultService resultService;

    @Test
    @DisplayName("POST /api/results - returns 201 when the result is recorded")
    void recordResult_returnsCreated() throws Exception {
        ResultRequest request = new ResultRequest(1L, 2, 1);
        ResultResponse response = new ResultResponse(1L, 1L, "Arsenal", "Chelsea", 2, 1, LocalDateTime.now());
        when(resultService.recordResult(any(ResultRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.homeGoals").value(2))
                .andExpect(jsonPath("$.awayGoals").value(1));
    }

    @Test
    @DisplayName("POST /api/results - maps BadRequestException to 400")
    void recordResult_businessRuleViolation() throws Exception {
        ResultRequest request = new ResultRequest(1L, 2, 1);
        when(resultService.recordResult(any(ResultRequest.class)))
                .thenThrow(new BadRequestException("A result already exists for fixture id 1"));

        mockMvc.perform(post("/api/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }

    @Test
    @DisplayName("POST /api/results - returns 400 when fixture id is missing")
    void recordResult_validationFails() throws Exception {
        ResultRequest request = new ResultRequest(null, 2, 1);

        mockMvc.perform(post("/api/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"));

        verify(resultService, never()).recordResult(any());
    }

    @Test
    @DisplayName("GET /api/results/{id} - maps ResourceNotFoundException to 404")
    void getById_notFound() throws Exception {
        when(resultService.findById(99L)).thenThrow(new ResourceNotFoundException("Result", 99L));

        mockMvc.perform(get("/api/results/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }
}
