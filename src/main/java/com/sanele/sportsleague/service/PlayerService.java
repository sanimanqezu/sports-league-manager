package com.sanele.sportsleague.service;

import com.sanele.sportsleague.dto.PlayerRequest;
import com.sanele.sportsleague.dto.PlayerResponse;
import com.sanele.sportsleague.entity.Player;
import com.sanele.sportsleague.entity.Team;
import com.sanele.sportsleague.exception.ResourceNotFoundException;
import com.sanele.sportsleague.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final TeamService teamService;

    public List<PlayerResponse> findAll() {
        return playerRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public PlayerResponse findById(Long id) {
        return toResponse(getPlayerOrThrow(id));
    }

    public List<PlayerResponse> findByTeamId(Long teamId) {
        return playerRepository.findByTeamId(teamId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PlayerResponse create(PlayerRequest request) {
        Team team = teamService.getTeamOrThrow(request.teamId());
        Player player = Player.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .position(request.position())
                .jerseyNumber(request.jerseyNumber())
                .team(team)
                .build();
        return toResponse(playerRepository.save(player));
    }

    @Transactional
    public PlayerResponse update(Long id, PlayerRequest request) {
        Player player = getPlayerOrThrow(id);
        Team team = teamService.getTeamOrThrow(request.teamId());
        player.setFirstName(request.firstName());
        player.setLastName(request.lastName());
        player.setPosition(request.position());
        player.setJerseyNumber(request.jerseyNumber());
        player.setTeam(team);
        return toResponse(playerRepository.save(player));
    }

    @Transactional
    public void delete(Long id) {
        if (!playerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Player", id);
        }
        playerRepository.deleteById(id);
    }

    private Player getPlayerOrThrow(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player", id));
    }

    private PlayerResponse toResponse(Player player) {
        return new PlayerResponse(
                player.getId(),
                player.getFirstName(),
                player.getLastName(),
                player.getPosition(),
                player.getJerseyNumber(),
                player.getTeam().getId(),
                player.getTeam().getName()
        );
    }
}
