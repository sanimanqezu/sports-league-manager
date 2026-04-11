package com.sanele.sportsleague.repository;

import com.sanele.sportsleague.entity.Fixture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FixtureRepository extends JpaRepository<Fixture, Long> {

    @Query("SELECT f FROM Fixture f WHERE f.homeTeam.id = :teamId OR f.awayTeam.id = :teamId")
    List<Fixture> findAllByTeamId(@Param("teamId") Long teamId);

    List<Fixture> findByLeagueId(Long leagueId);

    List<Fixture> findByLeagueIdAndPlayedTrue(Long leagueId);
}
