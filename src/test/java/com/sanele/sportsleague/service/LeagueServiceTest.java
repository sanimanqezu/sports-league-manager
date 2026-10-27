package com.sanele.sportsleague.service;

import com.sanele.sportsleague.dto.LeagueRequest;
import com.sanele.sportsleague.dto.LeagueResponse;
import com.sanele.sportsleague.entity.League;
import com.sanele.sportsleague.exception.ResourceNotFoundException;
import com.sanele.sportsleague.repository.LeagueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeagueService Tests")
class LeagueServiceTest {

    @Mock
    private LeagueRepository leagueRepository;

    @InjectMocks
    private LeagueService leagueService;

    private League league;

    @BeforeEach
    void setUp() {
        league = League.builder()
                .id(1L)
                .name("Premier League")
                .season("2024/25")
                .sport("Football")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("findAll - should map every league to a response")
    void findAll_mapsAll() {
        when(leagueRepository.findAll()).thenReturn(List.of(league));

        List<LeagueResponse> responses = leagueService.findAll();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).name()).isEqualTo("Premier League");
        assertThat(responses.get(0).season()).isEqualTo("2024/25");
    }

    @Test
    @DisplayName("findById - should return the league when it exists")
    void findById_success() {
        when(leagueRepository.findById(1L)).thenReturn(Optional.of(league));

        LeagueResponse response = leagueService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.sport()).isEqualTo("Football");
    }

    @Test
    @DisplayName("findById - should throw ResourceNotFoundException when missing")
    void findById_notFound() {
        when(leagueRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leagueService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("League")
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("create - should persist a league built from the request")
    void create_success() {
        LeagueRequest request = new LeagueRequest("La Liga", "2024/25", "Football");
        when(leagueRepository.save(any(League.class))).thenReturn(league);

        LeagueResponse response = leagueService.create(request);

        assertThat(response).isNotNull();
        ArgumentCaptor<League> captor = ArgumentCaptor.forClass(League.class);
        verify(leagueRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("La Liga");
        assertThat(captor.getValue().getSport()).isEqualTo("Football");
    }

    @Test
    @DisplayName("update - should modify an existing league and save it")
    void update_success() {
        LeagueRequest request = new LeagueRequest("Serie A", "2025/26", "Football");
        when(leagueRepository.findById(1L)).thenReturn(Optional.of(league));
        when(leagueRepository.save(any(League.class))).thenAnswer(inv -> inv.getArgument(0));

        LeagueResponse response = leagueService.update(1L, request);

        assertThat(response.name()).isEqualTo("Serie A");
        assertThat(response.season()).isEqualTo("2025/26");
        assertThat(league.getName()).isEqualTo("Serie A");
    }

    @Test
    @DisplayName("update - should throw ResourceNotFoundException when league is missing")
    void update_notFound() {
        LeagueRequest request = new LeagueRequest("Serie A", "2025/26", "Football");
        when(leagueRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leagueService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(leagueRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete - should remove the league when it exists")
    void delete_success() {
        when(leagueRepository.existsById(1L)).thenReturn(true);

        leagueService.delete(1L);

        verify(leagueRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete - should throw ResourceNotFoundException and not delete when missing")
    void delete_notFound() {
        when(leagueRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> leagueService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(leagueRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("getLeagueOrThrow - should return the entity when present")
    void getLeagueOrThrow_success() {
        when(leagueRepository.findById(1L)).thenReturn(Optional.of(league));

        League found = leagueService.getLeagueOrThrow(1L);

        assertThat(found).isSameAs(league);
    }
}
