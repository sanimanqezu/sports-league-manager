package com.sanele.sportsleague.service;

import com.sanele.sportsleague.dto.FixtureRequest;
import com.sanele.sportsleague.dto.FixtureResponse;
import com.sanele.sportsleague.entity.Fixture;
import com.sanele.sportsleague.entity.League;
import com.sanele.sportsleague.entity.Team;
import com.sanele.sportsleague.exception.BadRequestException;
import com.sanele.sportsleague.exception.ResourceNotFoundException;
import com.sanele.sportsleague.repository.FixtureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FixtureService Tests")
class FixtureServiceTest {

    @Mock
    private FixtureRepository fixtureRepository;

    @Mock
    private TeamService teamService;

    @Mock
    private LeagueService leagueService;

    @InjectMocks
    private FixtureService fixtureService;

    private League league;
    private Team homeTeam;
    private Team awayTeam;
    private Fixture fixture;

    @BeforeEach
    void setUp() {
        league = League.builder().id(1L).name("Premier League").season("2024/25").sport("Football").build();
        homeTeam = Team.builder().id(1L).name("Arsenal").city("London").league(league).build();
        awayTeam = Team.builder().id(2L).name("Chelsea").city("London").league(league).build();
        fixture = Fixture.builder()
                .id(1L).homeTeam(homeTeam).awayTeam(awayTeam).league(league)
                .matchDate(LocalDateTime.now().plusDays(1)).played(false).build();
    }

    @Test
    @DisplayName("create - should persist a fixture with resolved teams and league")
    void create_success() {
        FixtureRequest request = new FixtureRequest(1L, 2L, 1L, LocalDateTime.now().plusDays(2));
        when(teamService.getTeamOrThrow(1L)).thenReturn(homeTeam);
        when(teamService.getTeamOrThrow(2L)).thenReturn(awayTeam);
        when(leagueService.getLeagueOrThrow(1L)).thenReturn(league);
        when(fixtureRepository.save(any(Fixture.class))).thenAnswer(inv -> inv.getArgument(0));

        FixtureResponse response = fixtureService.create(request);

        assertThat(response.homeTeamName()).isEqualTo("Arsenal");
        assertThat(response.awayTeamName()).isEqualTo("Chelsea");
        assertThat(response.played()).isFalse();
    }

    @Test
    @DisplayName("create - should reject a fixture where home and away team are identical")
    void create_sameTeam() {
        FixtureRequest request = new FixtureRequest(1L, 1L, 1L, LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> fixtureService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot be the same");
        verify(fixtureRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - should propagate not-found when a team is missing")
    void create_teamNotFound() {
        FixtureRequest request = new FixtureRequest(1L, 2L, 1L, LocalDateTime.now().plusDays(2));
        when(teamService.getTeamOrThrow(1L))
                .thenThrow(new ResourceNotFoundException("Team", 1L));

        assertThatThrownBy(() -> fixtureService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(fixtureRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - should modify a fixture that has not been played")
    void update_success() {
        FixtureRequest request = new FixtureRequest(1L, 2L, 1L, LocalDateTime.now().plusDays(3));
        when(fixtureRepository.findById(1L)).thenReturn(Optional.of(fixture));
        when(teamService.getTeamOrThrow(1L)).thenReturn(homeTeam);
        when(teamService.getTeamOrThrow(2L)).thenReturn(awayTeam);
        when(leagueService.getLeagueOrThrow(1L)).thenReturn(league);
        when(fixtureRepository.save(any(Fixture.class))).thenAnswer(inv -> inv.getArgument(0));

        FixtureResponse response = fixtureService.update(1L, request);

        assertThat(response).isNotNull();
        verify(fixtureRepository).save(fixture);
    }

    @Test
    @DisplayName("update - should reject updating a fixture that has already been played")
    void update_alreadyPlayed() {
        fixture.setPlayed(true);
        FixtureRequest request = new FixtureRequest(1L, 2L, 1L, LocalDateTime.now().plusDays(3));
        when(fixtureRepository.findById(1L)).thenReturn(Optional.of(fixture));

        assertThatThrownBy(() -> fixtureService.update(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already been played");
        verify(fixtureRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - should throw ResourceNotFoundException when the fixture is missing")
    void update_notFound() {
        FixtureRequest request = new FixtureRequest(1L, 2L, 1L, LocalDateTime.now().plusDays(3));
        when(fixtureRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fixtureService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("delete - should throw ResourceNotFoundException when the fixture is missing")
    void delete_notFound() {
        when(fixtureRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> fixtureService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(fixtureRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("getFixtureOrThrow - should return the entity when present")
    void getFixtureOrThrow_success() {
        when(fixtureRepository.findById(1L)).thenReturn(Optional.of(fixture));

        Fixture found = fixtureService.getFixtureOrThrow(1L);

        assertThat(found).isSameAs(fixture);
    }
}
