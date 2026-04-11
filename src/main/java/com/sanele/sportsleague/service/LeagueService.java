package com.sanele.sportsleague.service;

import com.sanele.sportsleague.dto.LeagueRequest;
import com.sanele.sportsleague.dto.LeagueResponse;
import com.sanele.sportsleague.entity.League;
import com.sanele.sportsleague.exception.ResourceNotFoundException;
import com.sanele.sportsleague.repository.LeagueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeagueService {

    private final LeagueRepository leagueRepository;

    public List<LeagueResponse> findAll() {
        return leagueRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public LeagueResponse findById(Long id) {
        return toResponse(getLeagueOrThrow(id));
    }

    @Transactional
    public LeagueResponse create(LeagueRequest request) {
        League league = League.builder()
                .name(request.name())
                .season(request.season())
                .sport(request.sport())
                .build();
        return toResponse(leagueRepository.save(league));
    }

    @Transactional
    public LeagueResponse update(Long id, LeagueRequest request) {
        League league = getLeagueOrThrow(id);
        league.setName(request.name());
        league.setSeason(request.season());
        league.setSport(request.sport());
        return toResponse(leagueRepository.save(league));
    }

    @Transactional
    public void delete(Long id) {
        if (!leagueRepository.existsById(id)) {
            throw new ResourceNotFoundException("League", id);
        }
        leagueRepository.deleteById(id);
    }

    public League getLeagueOrThrow(Long id) {
        return leagueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("League", id));
    }

    private LeagueResponse toResponse(League league) {
        return new LeagueResponse(
                league.getId(),
                league.getName(),
                league.getSeason(),
                league.getSport(),
                league.getCreatedAt()
        );
    }
}
