package com.sanele.sportsleague.repository;

import com.sanele.sportsleague.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {

    Optional<Result> findByFixtureId(Long fixtureId);

    boolean existsByFixtureId(Long fixtureId);

    @Query("""
            SELECT r FROM Result r
            JOIN FETCH r.fixture f
            JOIN FETCH f.homeTeam
            JOIN FETCH f.awayTeam
            WHERE f.league.id = :leagueId
            """)
    List<Result> findAllByLeagueId(@Param("leagueId") Long leagueId);
}
