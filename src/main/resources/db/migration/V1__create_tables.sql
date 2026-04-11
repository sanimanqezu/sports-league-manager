-- V1__create_tables.sql
-- Sports League Manager schema

CREATE TABLE IF NOT EXISTS leagues (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    season     VARCHAR(100) NOT NULL,
    sport      VARCHAR(100) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS teams (
    id        BIGSERIAL PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    city      VARCHAR(255) NOT NULL,
    league_id BIGINT       NOT NULL,
    CONSTRAINT fk_teams_league FOREIGN KEY (league_id) REFERENCES leagues (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS players (
    id            BIGSERIAL PRIMARY KEY,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    position      VARCHAR(100) NOT NULL,
    jersey_number INT          NOT NULL CHECK (jersey_number > 0),
    team_id       BIGINT       NOT NULL,
    CONSTRAINT fk_players_team FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS fixtures (
    id           BIGSERIAL PRIMARY KEY,
    home_team_id BIGINT    NOT NULL,
    away_team_id BIGINT    NOT NULL,
    league_id    BIGINT    NOT NULL,
    match_date   TIMESTAMP NOT NULL,
    played       BOOLEAN   NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_fixtures_home_team FOREIGN KEY (home_team_id) REFERENCES teams (id) ON DELETE CASCADE,
    CONSTRAINT fk_fixtures_away_team FOREIGN KEY (away_team_id) REFERENCES teams (id) ON DELETE CASCADE,
    CONSTRAINT fk_fixtures_league    FOREIGN KEY (league_id)    REFERENCES leagues (id) ON DELETE CASCADE,
    CONSTRAINT chk_different_teams   CHECK (home_team_id <> away_team_id)
);

CREATE TABLE IF NOT EXISTS results (
    id          BIGSERIAL PRIMARY KEY,
    fixture_id  BIGINT    NOT NULL UNIQUE,
    home_goals  INT       NOT NULL CHECK (home_goals >= 0),
    away_goals  INT       NOT NULL CHECK (away_goals >= 0),
    recorded_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_results_fixture FOREIGN KEY (fixture_id) REFERENCES fixtures (id) ON DELETE CASCADE
);

-- Indexes for common lookups
CREATE INDEX IF NOT EXISTS idx_teams_league_id        ON teams (league_id);
CREATE INDEX IF NOT EXISTS idx_players_team_id        ON players (team_id);
CREATE INDEX IF NOT EXISTS idx_fixtures_home_team_id  ON fixtures (home_team_id);
CREATE INDEX IF NOT EXISTS idx_fixtures_away_team_id  ON fixtures (away_team_id);
CREATE INDEX IF NOT EXISTS idx_fixtures_league_id     ON fixtures (league_id);
CREATE INDEX IF NOT EXISTS idx_results_fixture_id     ON results (fixture_id);
