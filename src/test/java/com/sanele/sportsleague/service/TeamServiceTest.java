package com.sanele.sportsleague.service;

import com.sanele.sportsleague.dto.TeamRequest;
import com.sanele.sportsleague.dto.TeamResponse;
import com.sanele.sportsleague.entity.League;
import com.sanele.sportsleague.entity.Team;
import com.sanele.sportsleague.exception.ResourceNotFoundException;
import com.sanele.sportsleague.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeamService Tests")
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private LeagueService leagueService;

    @InjectMocks
    private TeamService teamService;

    private League league;
    private Team team;

    @BeforeEach
    void setUp() {
        league = League.builder().id(1L).name("Premier League").season("2024/25").sport("Football").build();
        team = Team.builder().id(1L).name("Arsenal").city("London").league(league).build();
    }

    @Test
    @DisplayName("findById - should return the team when it exists")
    void findById_success() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));

        TeamResponse response = teamService.findById(1L);

        assertThat(response.name()).isEqualTo("Arsenal");
        assertThat(response.leagueId()).isEqualTo(1L);
        assertThat(response.leagueName()).isEqualTo("Premier League");
    }

    @Test
    @DisplayName("findById - should throw ResourceNotFoundException when missing")
    void findById_notFound() {
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Team");
    }

    @Test
    @DisplayName("findByLeagueId - should map all teams of a league")
    void findByLeagueId_mapsAll() {
        when(teamRepository.findByLeagueId(1L)).thenReturn(List.of(team));

        List<TeamResponse> responses = teamService.findByLeagueId(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).city()).isEqualTo("London");
    }

    @Test
    @DisplayName("create - should resolve the league and persist the team")
    void create_success() {
        TeamRequest request = new TeamRequest("Chelsea", "London", 1L);
        when(leagueService.getLeagueOrThrow(1L)).thenReturn(league);
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        TeamResponse response = teamService.create(request);

        assertThat(response.name()).isEqualTo("Chelsea");
        ArgumentCaptor<Team> captor = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository).save(captor.capture());
        assertThat(captor.getValue().getLeague()).isSameAs(league);
    }

    @Test
    @DisplayName("create - should propagate not-found when the league is missing")
    void create_leagueNotFound() {
        TeamRequest request = new TeamRequest("Chelsea", "London", 99L);
        when(leagueService.getLeagueOrThrow(99L))
                .thenThrow(new ResourceNotFoundException("League", 99L));

        assertThatThrownBy(() -> teamService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(teamRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - should modify an existing team and save it")
    void update_success() {
        TeamRequest request = new TeamRequest("Arsenal FC", "North London", 1L);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(leagueService.getLeagueOrThrow(1L)).thenReturn(league);
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        TeamResponse response = teamService.update(1L, request);

        assertThat(response.name()).isEqualTo("Arsenal FC");
        assertThat(response.city()).isEqualTo("North London");
    }

    @Test
    @DisplayName("delete - should remove the team when it exists")
    void delete_success() {
        when(teamRepository.existsById(1L)).thenReturn(true);

        teamService.delete(1L);

        verify(teamRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete - should throw ResourceNotFoundException when missing")
    void delete_notFound() {
        when(teamRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> teamService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(teamRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("getTeamOrThrow - should throw ResourceNotFoundException when missing")
    void getTeamOrThrow_notFound() {
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.getTeamOrThrow(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
