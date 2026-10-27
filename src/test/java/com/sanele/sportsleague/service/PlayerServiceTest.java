package com.sanele.sportsleague.service;

import com.sanele.sportsleague.dto.PlayerRequest;
import com.sanele.sportsleague.dto.PlayerResponse;
import com.sanele.sportsleague.entity.League;
import com.sanele.sportsleague.entity.Player;
import com.sanele.sportsleague.entity.Team;
import com.sanele.sportsleague.exception.ResourceNotFoundException;
import com.sanele.sportsleague.repository.PlayerRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlayerService Tests")
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private TeamService teamService;

    @InjectMocks
    private PlayerService playerService;

    private Team team;
    private Player player;

    @BeforeEach
    void setUp() {
        League league = League.builder().id(1L).name("Premier League").season("2024/25").sport("Football").build();
        team = Team.builder().id(1L).name("Arsenal").city("London").league(league).build();
        player = Player.builder()
                .id(1L).firstName("Bukayo").lastName("Saka").position("Winger").jerseyNumber(7).team(team).build();
    }

    @Test
    @DisplayName("findById - should return the player when it exists")
    void findById_success() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));

        PlayerResponse response = playerService.findById(1L);

        assertThat(response.firstName()).isEqualTo("Bukayo");
        assertThat(response.jerseyNumber()).isEqualTo(7);
        assertThat(response.teamName()).isEqualTo("Arsenal");
    }

    @Test
    @DisplayName("findById - should throw ResourceNotFoundException when missing")
    void findById_notFound() {
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Player");
    }

    @Test
    @DisplayName("findByTeamId - should map all players of a team")
    void findByTeamId_mapsAll() {
        when(playerRepository.findByTeamId(1L)).thenReturn(List.of(player));

        List<PlayerResponse> responses = playerService.findByTeamId(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).lastName()).isEqualTo("Saka");
    }

    @Test
    @DisplayName("create - should resolve the team and persist the player")
    void create_success() {
        PlayerRequest request = new PlayerRequest("Martin", "Odegaard", "Midfielder", 8, 1L);
        when(teamService.getTeamOrThrow(1L)).thenReturn(team);
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));

        PlayerResponse response = playerService.create(request);

        assertThat(response.firstName()).isEqualTo("Martin");
        ArgumentCaptor<Player> captor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository).save(captor.capture());
        assertThat(captor.getValue().getJerseyNumber()).isEqualTo(8);
        assertThat(captor.getValue().getTeam()).isSameAs(team);
    }

    @Test
    @DisplayName("create - should propagate not-found when the team is missing")
    void create_teamNotFound() {
        PlayerRequest request = new PlayerRequest("Martin", "Odegaard", "Midfielder", 8, 99L);
        when(teamService.getTeamOrThrow(99L))
                .thenThrow(new ResourceNotFoundException("Team", 99L));

        assertThatThrownBy(() -> playerService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(playerRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - should modify an existing player and save it")
    void update_success() {
        PlayerRequest request = new PlayerRequest("Bukayo", "Saka", "Forward", 7, 1L);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(teamService.getTeamOrThrow(1L)).thenReturn(team);
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));

        PlayerResponse response = playerService.update(1L, request);

        assertThat(response.position()).isEqualTo("Forward");
        assertThat(player.getPosition()).isEqualTo("Forward");
    }

    @Test
    @DisplayName("update - should throw ResourceNotFoundException when the player is missing")
    void update_notFound() {
        PlayerRequest request = new PlayerRequest("Bukayo", "Saka", "Forward", 7, 1L);
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(playerRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete - should throw ResourceNotFoundException when missing")
    void delete_notFound() {
        when(playerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> playerService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(playerRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete - should remove the player when it exists")
    void delete_success() {
        when(playerRepository.existsById(1L)).thenReturn(true);

        playerService.delete(1L);

        verify(playerRepository).deleteById(1L);
    }
}
