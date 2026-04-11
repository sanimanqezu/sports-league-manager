package com.sanele.sportsleague.service;

import com.sanele.sportsleague.dto.ResultRequest;
import com.sanele.sportsleague.dto.ResultResponse;
import com.sanele.sportsleague.dto.StandingEntry;
import com.sanele.sportsleague.entity.Fixture;
import com.sanele.sportsleague.entity.Result;
import com.sanele.sportsleague.entity.Team;
import com.sanele.sportsleague.exception.BadRequestException;
import com.sanele.sportsleague.exception.ResourceNotFoundException;
import com.sanele.sportsleague.repository.ResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResultService {

    private final ResultRepository resultRepository;
    private final FixtureService fixtureService;

    public List<ResultResponse> findAll() {
        return resultRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ResultResponse findById(Long id) {
        Result result = resultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Result", id));
        return toResponse(result);
    }

    @Transactional
    public ResultResponse recordResult(ResultRequest request) {
        Fixture fixture = fixtureService.getFixtureOrThrow(request.fixtureId());

        if (fixture.isPlayed()) {
            throw new BadRequestException("A result has already been recorded for fixture id " + request.fixtureId());
        }

        if (resultRepository.existsByFixtureId(request.fixtureId())) {
            throw new BadRequestException("A result already exists for fixture id " + request.fixtureId());
        }

        fixture.setPlayed(true);

        Result result = Result.builder()
                .fixture(fixture)
                .homeGoals(request.homeGoals())
                .awayGoals(request.awayGoals())
                .build();

        return toResponse(resultRepository.save(result));
    }

    public List<StandingEntry> computeStandings(Long leagueId) {
        List<Result> results = resultRepository.findAllByLeagueId(leagueId);

        // Map: teamId -> mutable stats array [played, won, drawn, lost, gf, ga]
        Map<Long, int[]> statsMap = new LinkedHashMap<>();
        Map<Long, String> teamNames = new LinkedHashMap<>();

        for (Result result : results) {
            Fixture fixture = result.getFixture();
            Team home = fixture.getHomeTeam();
            Team away = fixture.getAwayTeam();

            statsMap.putIfAbsent(home.getId(), new int[6]);
            statsMap.putIfAbsent(away.getId(), new int[6]);
            teamNames.putIfAbsent(home.getId(), home.getName());
            teamNames.putIfAbsent(away.getId(), away.getName());

            int[] homeStats = statsMap.get(home.getId());
            int[] awayStats = statsMap.get(away.getId());

            int hg = result.getHomeGoals();
            int ag = result.getAwayGoals();

            // played
            homeStats[0]++;
            awayStats[0]++;

            // goals for / against
            homeStats[4] += hg;
            homeStats[5] += ag;
            awayStats[4] += ag;
            awayStats[5] += hg;

            if (hg > ag) {
                // home win
                homeStats[1]++;
                awayStats[3]++;
            } else if (hg == ag) {
                // draw
                homeStats[2]++;
                awayStats[2]++;
            } else {
                // away win
                awayStats[1]++;
                homeStats[3]++;
            }
        }

        List<StandingEntry> standings = new ArrayList<>();
        for (Map.Entry<Long, int[]> entry : statsMap.entrySet()) {
            Long teamId = entry.getKey();
            int[] s = entry.getValue();
            int played = s[0];
            int won = s[1];
            int drawn = s[2];
            int lost = s[3];
            int gf = s[4];
            int ga = s[5];
            int gd = gf - ga;
            int points = (won * 3) + drawn;
            standings.add(new StandingEntry(teamId, teamNames.get(teamId), played, won, drawn, lost, gf, ga, gd, points));
        }

        // Sort: points desc, then goal difference desc, then goals for desc
        standings.sort(
                Comparator.comparingInt(StandingEntry::points).reversed()
                        .thenComparing(Comparator.comparingInt(StandingEntry::goalDifference).reversed())
                        .thenComparing(Comparator.comparingInt(StandingEntry::goalsFor).reversed()));

        return standings;
    }

    private ResultResponse toResponse(Result result) {
        return new ResultResponse(
                result.getId(),
                result.getFixture().getId(),
                result.getFixture().getHomeTeam().getName(),
                result.getFixture().getAwayTeam().getName(),
                result.getHomeGoals(),
                result.getAwayGoals(),
                result.getRecordedAt()
        );
    }
}
