# Sports League Manager

[![CI](https://github.com/sanimanqezu/sports-league-manager/actions/workflows/ci.yml/badge.svg)](https://github.com/sanimanqezu/sports-league-manager/actions/workflows/ci.yml)

A RESTful API for managing sports leagues, teams, players, fixtures, and match results, with live league standings computed from recorded results.

## Tech Stack

| Layer        | Technology                           |
|--------------|--------------------------------------|
| Language     | Java 25                              |
| Framework    | Spring Boot 3.5.16                   |
| Persistence  | Spring Data JPA + Hibernate          |
| Database     | PostgreSQL (runtime), H2 (tests)     |
| Migrations   | Flyway                               |
| Validation   | Jakarta Bean Validation              |
| Docs         | SpringDoc OpenAPI 2.3 (Swagger)      |
| Testing      | JUnit 5, Mockito, AssertJ, MockMvc   |
| Build        | Maven                               |
| Container    | Docker + Docker Compose              |

## Key Design Points

- **Layered architecture** — thin REST controllers delegate to transactional services; repositories are pure Spring Data JPA.
- **DTOs as records** — request/response payloads are immutable Java records with Jakarta Bean Validation on the inbound side; entities never leak across the API boundary.
- **Centralized exception handling** — a single `@RestControllerAdvice` maps `ResourceNotFoundException` to 404, `BadRequestException` to 400, and bean-validation failures to 400, all rendered as RFC 7807 `ProblemDetail` responses.
- **Read-optimized transactions** — services are `@Transactional(readOnly = true)` by default, with write methods promoted to read-write transactions.
- **Standings computed on read** — the table is aggregated from persisted results on each request (3/1/0 points, tie-broken by goal difference then goals for), so no derived state has to be kept in sync.
- **Database migrations as code** — schema is owned by Flyway; Hibernate runs in `ddl-auto: validate` against the migrated schema.

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

## Running Locally

This service connects to a **centralized PostgreSQL** shared across the portfolio rather than its own database. The connection is driven entirely by environment variables (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`) — see `.env.example`.

**Prerequisites:** Java 25, Maven, and the shared Postgres container running.

```bash
# 1. Start the centralized Postgres once, from the portfolio root
docker compose up -d

# 2. Run the application (defaults connect to localhost:5432/sports_league)
mvn spring-boot:run
```

Override any of the defaults via environment variables when the central DB lives elsewhere:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=sports_league
export DB_USERNAME=postgres
export DB_PASSWORD=postgres

mvn spring-boot:run
```

The `docker-compose.yml` in this repo builds the app image and attaches it to the external `portfolio-net` network so it can reach the shared database:

```bash
docker compose up --build
```

The application will be available at `http://localhost:8080`.

## Tests

```bash
mvn test
```

The suite is **59 tests** and needs no database or network — it runs against mocks and an in-memory H2 schema.

- **Service-layer unit tests** (`@ExtendWith(MockitoExtension.class)`, `@Mock` repositories, `@InjectMocks` service) cover each service's happy path, not-found handling, and domain rules — including result recording, duplicate/played-fixture rejection, same-team fixture rejection, and the standings computation (points, sorting, goal-difference tie-breaks, goal tracking).
- **Controller slice tests** (`@WebMvcTest`, `@MockitoBean` service) verify status codes and request delegation, that invalid payloads return **400**, and that the `GlobalExceptionHandler` maps `ResourceNotFoundException` to **404** and `BadRequestException` to **400** as `ProblemDetail` bodies.

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
    ├── java/com/sanele/sportsleague/
    │   ├── controller/       # @WebMvcTest slice tests
    │   └── service/          # Mockito unit tests
    └── resources/
        └── application.yml   # H2 test config
```

## License

MIT
