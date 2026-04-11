package com.sanele.sportsleague.service;

import com.sanele.sportsleague.dto.ResultRequest;
import com.sanele.sportsleague.dto.ResultResponse;
import com.sanele.sportsleague.dto.StandingEntry;
import com.sanele.sportsleague.entity.Fixture;
import com.sanele.sportsleague.entity.League;
import com.sanele.sportsleague.entity.Result;
import com.sanele.sportsleague.entity.Team;
import com.sanele.sportsleague.exception.BadRequestException;
import com.sanele.sportsleague.exception.ResourceNotFoundException;
import com.sanele.sportsleague.repository.ResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResultService Tests")
class ResultServiceTest {

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private FixtureService fixtureService;

    @InjectMocks
    private ResultService resultService;

    private League league;
    private Team homeTeam;
    private Team awayTeam;
    private Fixture fixture;

    @BeforeEach
    void setUp() {
        league = League.builder()
                .id(1L)
                .name("Premier League")
                .season("2024/25")
                .sport("Football")
                .createdAt(LocalDateTime.now())
                .build();

        homeTeam = Team.builder()
                .id(1L)
                .name("Arsenal")
                .city("London")
                .league(league)
                .build();

        awayTeam = Team.builder()
                .id(2L)
                .name("Chelsea")
                .city("London")
                .league(league)
                .build();

        fixture = Fixture.builder()
                .id(1L)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .league(league)
                .matchDate(LocalDateTime.now().minusDays(1))
                .played(false)
                .build();
    }

    @Test
    @DisplayName("recordResult - should successfully record a result and mark fixture as played")
    void recordResult_success() {
        ResultRequest request = new ResultRequest(1L, 2, 1);

        when(fixtureService.getFixtureOrThrow(1L)).thenReturn(fixture);
        when(resultRepository.existsByFixtureId(1L)).thenReturn(false);

        Result savedResult = Result.builder()
                .id(1L)
                .fixture(fixture)
                .homeGoals(2)
                .awayGoals(1)
                .recordedAt(LocalDateTime.now())
                .build();
        when(resultRepository.save(any(Result.class))).thenReturn(savedResult);

        ResultResponse response = resultService.recordResult(request);

        assertThat(response).isNotNull();
        assertThat(response.homeGoals()).isEqualTo(2);
        assertThat(response.awayGoals()).isEqualTo(1);
        assertThat(response.homeTeamName()).isEqualTo("Arsenal");
        assertThat(response.awayTeamName()).isEqualTo("Chelsea");
        assertThat(fixture.isPlayed()).isTrue();

        verify(resultRepository).save(any(Result.class));
    }

    @Test
    @DisplayName("recordResult - should throw BadRequestException when fixture already played")
    void recordResult_fixtureAlreadyPlayed() {
        fixture.setPlayed(true);
        ResultRequest request = new ResultRequest(1L, 1, 0);

        when(fixtureService.getFixtureOrThrow(1L)).thenReturn(fixture);

        assertThatThrownBy(() -> resultService.recordResult(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("result has already been recorded");

        verify(resultRepository, never()).save(any());
    }

    @Test
    @DisplayName("recordResult - should throw BadRequestException when result already exists for fixture")
    void recordResult_resultAlreadyExists() {
        ResultRequest request = new ResultRequest(1L, 3, 3);

        when(fixtureService.getFixtureOrThrow(1L)).thenReturn(fixture);
        when(resultRepository.existsByFixtureId(1L)).thenReturn(true);

        assertThatThrownBy(() -> resultService.recordResult(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("result already exists");

        verify(resultRepository, never()).save(any());
    }

    @Test
    @DisplayName("computeStandings - should award 3 points to the winning team")
    void computeStandings_homeWin() {
        Result result = buildResult(1L, homeTeam, awayTeam, 3, 1);
        when(resultRepository.findAllByLeagueId(1L)).thenReturn(List.of(result));

        List<StandingEntry> standings = resultService.computeStandings(1L);

        assertThat(standings).hasSize(2);
        StandingEntry winner = standings.get(0);
        assertThat(winner.teamName()).isEqualTo("Arsenal");
        assertThat(winner.points()).isEqualTo(3);
        assertThat(winner.won()).isEqualTo(1);
        assertThat(winner.lost()).isEqualTo(0);

        StandingEntry loser = standings.get(1);
        assertThat(loser.teamName()).isEqualTo("Chelsea");
        assertThat(loser.points()).isEqualTo(0);
        assertThat(loser.lost()).isEqualTo(1);
    }

    @Test
    @DisplayName("computeStandings - should award 1 point each for a draw")
    void computeStandings_draw() {
        Result result = buildResult(1L, homeTeam, awayTeam, 2, 2);
        when(resultRepository.findAllByLeagueId(1L)).thenReturn(List.of(result));

        List<StandingEntry> standings = resultService.computeStandings(1L);

        assertThat(standings).hasSize(2);
        standings.forEach(entry -> {
            assertThat(entry.points()).isEqualTo(1);
            assertThat(entry.drawn()).isEqualTo(1);
            assertThat(entry.won()).isEqualTo(0);
            assertThat(entry.lost()).isEqualTo(0);
        });
    }

    @Test
    @DisplayName("computeStandings - should correctly sort by points descending")
    void computeStandings_sortedByPointsDescending() {
        Team thirdTeam = Team.builder().id(3L).name("Manchester City").city("Manchester").league(league).build();

        Fixture fixture2 = Fixture.builder()
                .id(2L).homeTeam(awayTeam).awayTeam(thirdTeam)
                .league(league).matchDate(LocalDateTime.now().minusDays(1)).played(true).build();

        Result r1 = buildResult(1L, homeTeam, awayTeam, 2, 0);  // Arsenal 3pts
        Result r2 = buildResult(2L, awayTeam, thirdTeam, fixture2, 1, 0);  // Chelsea 3pts
        // Arsenal: W=1 (3pts), Chelsea: W=1 L=1 (3pts), ManCity: L=1 (0pts)

        when(resultRepository.findAllByLeagueId(1L)).thenReturn(List.of(r1, r2));

        List<StandingEntry> standings = resultService.computeStandings(1L);

        assertThat(standings).hasSize(3);
        assertThat(standings.get(standings.size() - 1).teamName()).isEqualTo("Manchester City");
        assertThat(standings.get(standings.size() - 1).points()).isEqualTo(0);
    }

    @Test
    @DisplayName("computeStandings - should return empty list when no results exist")
    void computeStandings_noResults() {
        when(resultRepository.findAllByLeagueId(99L)).thenReturn(List.of());

        List<StandingEntry> standings = resultService.computeStandings(99L);

        assertThat(standings).isEmpty();
    }

    @Test
    @DisplayName("computeStandings - should correctly track goals for and against")
    void computeStandings_goalTracking() {
        Result result = buildResult(1L, homeTeam, awayTeam, 4, 2);
        when(resultRepository.findAllByLeagueId(1L)).thenReturn(List.of(result));

        List<StandingEntry> standings = resultService.computeStandings(1L);

        StandingEntry arsenalStanding = standings.stream()
                .filter(s -> s.teamName().equals("Arsenal"))
                .findFirst()
                .orElseThrow();

        assertThat(arsenalStanding.goalsFor()).isEqualTo(4);
        assertThat(arsenalStanding.goalsAgainst()).isEqualTo(2);
        assertThat(arsenalStanding.goalDifference()).isEqualTo(2);

        StandingEntry chelseaStanding = standings.stream()
                .filter(s -> s.teamName().equals("Chelsea"))
                .findFirst()
                .orElseThrow();

        assertThat(chelseaStanding.goalsFor()).isEqualTo(2);
        assertThat(chelseaStanding.goalsAgainst()).isEqualTo(4);
        assertThat(chelseaStanding.goalDifference()).isEqualTo(-2);
    }

    @Test
    @DisplayName("computeStandings - should sort teams with equal points by goal difference")
    void computeStandings_tieBreakByGoalDifference() {
        Team thirdTeam = Team.builder().id(3L).name("Liverpool").city("Liverpool").league(league).build();

        // Arsenal beats Chelsea 3-0 (GD: Arsenal +3, Chelsea -3)
        Result r1 = buildResult(1L, homeTeam, awayTeam, 3, 0);
        // Liverpool beats Chelsea 1-0 (Liverpool +1, Chelsea -1 total from this game)
        Fixture f2 = Fixture.builder().id(2L).homeTeam(thirdTeam).awayTeam(awayTeam)
                .league(league).matchDate(LocalDateTime.now().minusDays(1)).played(true).build();
        Result r2 = buildResult(2L, thirdTeam, awayTeam, f2, 1, 0);

        when(resultRepository.findAllByLeagueId(1L)).thenReturn(List.of(r1, r2));

        List<StandingEntry> standings = resultService.computeStandings(1L);

        // Both Arsenal and Liverpool have 3pts; Arsenal GD=+3, Liverpool GD=+1
        assertThat(standings.get(0).teamName()).isEqualTo("Arsenal");
        assertThat(standings.get(1).teamName()).isEqualTo("Liverpool");
    }

    @Test
    @DisplayName("findById - should throw ResourceNotFoundException when result not found")
    void findById_notFound() {
        when(resultRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resultService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ---- Helpers ----

    private Result buildResult(long resultId, Team home, Team away, int homeGoals, int awayGoals) {
        Fixture f = Fixture.builder()
                .id(resultId)
                .homeTeam(home)
                .awayTeam(away)
                .league(league)
                .matchDate(LocalDateTime.now().minusDays(1))
                .played(true)
                .build();
        return Result.builder()
                .id(resultId)
                .fixture(f)
                .homeGoals(homeGoals)
                .awayGoals(awayGoals)
                .recordedAt(LocalDateTime.now())
                .build();
    }

    private Result buildResult(long resultId, Team home, Team away, Fixture f, int homeGoals, int awayGoals) {
        return Result.builder()
                .id(resultId)
                .fixture(f)
                .homeGoals(homeGoals)
                .awayGoals(awayGoals)
                .recordedAt(LocalDateTime.now())
                .build();
    }
}
