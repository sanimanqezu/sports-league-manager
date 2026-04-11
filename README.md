# Sports League Manager

A production-ready RESTful API for managing sports leagues, teams, players, fixtures, and match results. Built with Spring Boot 3.2.5 as a portfolio project.

## Tech Stack

| Layer        | Technology                        |
|--------------|-----------------------------------|
| Language     | Java 17                           |
| Framework    | Spring Boot 3.2.5                 |
| Persistence  | Spring Data JPA + Hibernate       |
| Database     | PostgreSQL (runtime), H2 (tests)  |
| Migrations   | Flyway                            |
| Validation   | Jakarta Bean Validation           |
| Docs         | SpringDoc OpenAPI 2.3 (Swagger)   |
| Testing      | JUnit 5 + Mockito                 |
| Build        | Maven                             |
| Container    | Docker + Docker Compose           |

## Domain Model

```
League (1) ──< Team (1) ──< Player
                  |
                  └──< Fixture >── Result
                        └── League
```

- **League** — top-level container (name, season, sport)
- **Team** — belongs to a League
- **Player** — belongs to a Team
- **Fixture** — home vs away team within a league, has a match date
- **Result** — one-to-one with Fixture; records goals scored; auto-marks fixture as played

## Business Logic

When a result is recorded via `POST /api/results`:
1. The fixture is validated as not yet played.
2. `fixture.played` is set to `true`.
3. The result is persisted.

`GET /api/leagues/{id}/standings` computes live standings by aggregating all results for the league:
- Win = **3 pts**, Draw = **1 pt**, Loss = **0 pts**
- Sorted by: points (desc) → goal difference (desc) → goals for (desc)

## API Endpoints

| Method | Path                           | Description                              |
|--------|--------------------------------|------------------------------------------|
| GET    | /api/leagues                   | List all leagues                         |
| POST   | /api/leagues                   | Create a league                          |
| GET    | /api/leagues/{id}              | Get league by ID                         |
| PUT    | /api/leagues/{id}              | Update league                            |
| DELETE | /api/leagues/{id}              | Delete league                            |
| GET    | /api/leagues/{id}/standings    | Get computed standings table             |
| GET    | /api/teams                     | List all teams                           |
| POST   | /api/teams                     | Create a team                            |
| GET    | /api/teams/{id}                | Get team by ID                           |
| PUT    | /api/teams/{id}                | Update team                              |
| DELETE | /api/teams/{id}                | Delete team                              |
| GET    | /api/teams/{id}/players        | List all players for a team              |
| GET    | /api/teams/{id}/fixtures       | List all fixtures for a team             |
| GET    | /api/players                   | List all players                         |
| POST   | /api/players                   | Create a player                          |
| GET    | /api/players/{id}              | Get player by ID                         |
| PUT    | /api/players/{id}              | Update player                            |
| DELETE | /api/players/{id}              | Delete player                            |
| GET    | /api/fixtures                  | List all fixtures                        |
| POST   | /api/fixtures                  | Create a fixture                         |
| GET    | /api/fixtures/{id}             | Get fixture by ID                        |
| PUT    | /api/fixtures/{id}             | Update fixture (only if not played)      |
| DELETE | /api/fixtures/{id}             | Delete fixture                           |
| GET    | /api/results                   | List all results                         |
| POST   | /api/results                   | Record a match result                    |
| GET    | /api/results/{id}              | Get result by ID                         |

## Interactive API Documentation

Once the application is running, visit:

**Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

**OpenAPI JSON:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## Running with Docker Compose

The easiest way to run the full stack locally:

```bash
# Clone the repo
git clone https://github.com/sanele/sports-league-manager.git
cd sports-league-manager

# Build image and start app + PostgreSQL
docker compose up --build

# Stop and remove containers
docker compose down

# Stop and remove containers + volumes
docker compose down -v
```

The application will be available at `http://localhost:8080`.

## Running Locally (without Docker)

**Prerequisites:** Java 17, Maven, PostgreSQL running on port 5432

```bash
# Create the database
psql -U postgres -c "CREATE DATABASE sports_league;"

# Run the application
./mvnw spring-boot:run
```

Or with custom DB credentials:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=sports_league
export DB_USERNAME=myuser
export DB_PASSWORD=mypassword

./mvnw spring-boot:run
```

## Running Tests

Tests use H2 in-memory database — no PostgreSQL required.

```bash
./mvnw test
```

## Example Requests

**Create a League:**
```bash
curl -X POST http://localhost:8080/api/leagues \
  -H "Content-Type: application/json" \
  -d '{"name":"Premier League","season":"2024/25","sport":"Football"}'
```

**Create a Team:**
```bash
curl -X POST http://localhost:8080/api/teams \
  -H "Content-Type: application/json" \
  -d '{"name":"Arsenal","city":"London","leagueId":1}'
```

**Record a Result:**
```bash
curl -X POST http://localhost:8080/api/results \
  -H "Content-Type: application/json" \
  -d '{"fixtureId":1,"homeGoals":2,"awayGoals":1}'
```

**Get Standings:**
```bash
curl http://localhost:8080/api/leagues/1/standings
```

## Project Structure

```
src/
├── main/
│   ├── java/com/sanele/sportsleague/
│   │   ├── SportsLeagueApplication.java
│   │   ├── controller/       # REST controllers
│   │   ├── service/          # Business logic
│   │   ├── repository/       # Spring Data JPA repositories
│   │   ├── entity/           # JPA entities
│   │   ├── dto/              # Request/Response records
│   │   └── exception/        # Custom exceptions & global handler
│   └── resources/
│       ├── application.yml
│       └── db/migration/     # Flyway SQL migrations
└── test/
    ├── java/com/sanele/sportsleague/service/
    │   └── ResultServiceTest.java
    └── resources/
        └── application.yml   # H2 test config
```

## License

MIT
