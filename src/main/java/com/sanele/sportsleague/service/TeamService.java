package com.sanele.sportsleague.service;

import com.sanele.sportsleague.dto.TeamRequest;
import com.sanele.sportsleague.dto.TeamResponse;
import com.sanele.sportsleague.entity.League;
import com.sanele.sportsleague.entity.Team;
import com.sanele.sportsleague.exception.ResourceNotFoundException;
import com.sanele.sportsleague.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final LeagueService leagueService;

    public List<TeamResponse> findAll() {
        return teamRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public TeamResponse findById(Long id) {
        return toResponse(getTeamOrThrow(id));
    }

    public List<TeamResponse> findByLeagueId(Long leagueId) {
        return teamRepository.findByLeagueId(leagueId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TeamResponse create(TeamRequest request) {
        League league = leagueService.getLeagueOrThrow(request.leagueId());
        Team team = Team.builder()
                .name(request.name())
                .city(request.city())
                .league(league)
                .build();
        return toResponse(teamRepository.save(team));
    }

    @Transactional
    public TeamResponse update(Long id, TeamRequest request) {
        Team team = getTeamOrThrow(id);
        League league = leagueService.getLeagueOrThrow(request.leagueId());
        team.setName(request.name());
        team.setCity(request.city());
        team.setLeague(league);
        return toResponse(teamRepository.save(team));
    }

    @Transactional
    public void delete(Long id) {
        if (!teamRepository.existsById(id)) {
            throw new ResourceNotFoundException("Team", id);
        }
        teamRepository.deleteById(id);
    }

    public Team getTeamOrThrow(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", id));
    }

    private TeamResponse toResponse(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getCity(),
                team.getLeague().getId(),
                team.getLeague().getName()
        );
    }
}
