package com.sanele.sportsleague.service;

import com.sanele.sportsleague.dto.FixtureRequest;
import com.sanele.sportsleague.dto.FixtureResponse;
import com.sanele.sportsleague.entity.Fixture;
import com.sanele.sportsleague.entity.League;
import com.sanele.sportsleague.entity.Team;
import com.sanele.sportsleague.exception.BadRequestException;
import com.sanele.sportsleague.exception.ResourceNotFoundException;
import com.sanele.sportsleague.repository.FixtureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FixtureService {

    private final FixtureRepository fixtureRepository;
    private final TeamService teamService;
    private final LeagueService leagueService;

    public List<FixtureResponse> findAll() {
        return fixtureRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public FixtureResponse findById(Long id) {
        return toResponse(getFixtureOrThrow(id));
    }

    public List<FixtureResponse> findByTeamId(Long teamId) {
        return fixtureRepository.findAllByTeamId(teamId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public FixtureResponse create(FixtureRequest request) {
        if (request.homeTeamId().equals(request.awayTeamId())) {
            throw new BadRequestException("Home team and away team cannot be the same");
        }
        Team homeTeam = teamService.getTeamOrThrow(request.homeTeamId());
        Team awayTeam = teamService.getTeamOrThrow(request.awayTeamId());
        League league = leagueService.getLeagueOrThrow(request.leagueId());

        Fixture fixture = Fixture.builder()
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .league(league)
                .matchDate(request.matchDate())
                .build();
        return toResponse(fixtureRepository.save(fixture));
    }

    @Transactional
    public FixtureResponse update(Long id, FixtureRequest request) {
        if (request.homeTeamId().equals(request.awayTeamId())) {
            throw new BadRequestException("Home team and away team cannot be the same");
        }
        Fixture fixture = getFixtureOrThrow(id);
        if (fixture.isPlayed()) {
            throw new BadRequestException("Cannot update a fixture that has already been played");
        }
        Team homeTeam = teamService.getTeamOrThrow(request.homeTeamId());
        Team awayTeam = teamService.getTeamOrThrow(request.awayTeamId());
        League league = leagueService.getLeagueOrThrow(request.leagueId());

        fixture.setHomeTeam(homeTeam);
        fixture.setAwayTeam(awayTeam);
        fixture.setLeague(league);
        fixture.setMatchDate(request.matchDate());
        return toResponse(fixtureRepository.save(fixture));
    }

    @Transactional
    public void delete(Long id) {
        if (!fixtureRepository.existsById(id)) {
            throw new ResourceNotFoundException("Fixture", id);
        }
        fixtureRepository.deleteById(id);
    }

    public Fixture getFixtureOrThrow(Long id) {
        return fixtureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fixture", id));
    }

    private FixtureResponse toResponse(Fixture fixture) {
        return new FixtureResponse(
                fixture.getId(),
                fixture.getHomeTeam().getId(),
                fixture.getHomeTeam().getName(),
                fixture.getAwayTeam().getId(),
                fixture.getAwayTeam().getName(),
                fixture.getLeague().getId(),
                fixture.getLeague().getName(),
                fixture.getMatchDate(),
                fixture.isPlayed()
        );
    }
}
