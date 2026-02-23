# Whalebone QA Assignment

Automated tests covering:

- **API** — `https://qa-assignment.dev1.whalebone.io/api/teams`
- **UI** — `http://uitestingplayground.com/`
- **Web scraping** — live NHL roster page via Playwright

**Tech stack:** Java 17 · Maven · TestNG · RestAssured · Playwright · Allure · Lombok

---

## Test coverage

### API — `/api/teams`  (`TeamsApiTests`)

| Test                                                                | Description                                                  |
|---------------------------------------------------------------------|--------------------------------------------------------------|
| `verifyTeamsCountIs32`                                              | Response contains exactly 32 teams                           |
| `verifyOldestTeamIsMontrealCanadiens`                               | Oldest team by `founded` is Montreal Canadiens (1909)        |
| `verifyThereIsACityWithMoreThanOneTeam_andVerifyNewYorkTeamsByName` | At least one multi-team city; New York = Islanders + Rangers |
| `verifyMetropolitanDivisionHas8Teams_byName`                        | Metropolitan division has exactly 8 named teams              |

### UI — UI Testing Playground  (`UiTestingPlaygroundTests`)

| Test                                           | Description                                                  |
|------------------------------------------------|--------------------------------------------------------------|
| `sampleApp_coverAllFunctionalities`            | Successful login → logout → invalid login                    |
| `loadDelay_pageLoadsInReasonableTime`          | Delayed button appears within 10 s                           |
| `progressBar_followScenario_waitTo75_thenStop` | Start → wait to ≥75% → stop → assert final value in [75, 77] |

### Scraping  (`OldestNHLTeamRosterScrapeTests`)

| Test                                                | Description                                                   |
|-----------------------------------------------------|---------------------------------------------------------------|
| `scrapeOldestTeamRoster_verifyMoreCanadiansThanUSA` | Oldest team from API → scrape roster → more CAN than USA born |

---

## Project structure

```
src/test/java/cz/whalebone/
  api/
    client/          # RestAssured client (happy + raw for status tests)
    model/           # DTOs with Lombok + Jackson annotations
  config/            # Config — reads config.properties, overridable via -Dkey=value
  context/           # TestContext (per-test state) + GUIContext (lazy page objects)
  pages/
    BasePage.java
    playground/      # UI Testing Playground page objects
    nhl/             # NHL roster scraping page object
  reporting/         # TestNG listeners: Allure, screenshot on failure, timeout→failure
  support/           # BaseUiTest — Playwright lifecycle (ThreadLocal, parallel-safe)
  tests/
    api/
    ui/
  util/              # Stopwatch, CountryUtil
src/test/resources/
  config.properties
pom.xml
testng.xml
Dockerfile
docker-compose.yml
```

---

## Requirements

- Java 17+
- Maven 3.9+
- Internet access (Playwright downloads browsers on first run)

---

## Configuration

`src/test/resources/config.properties` — every key can be overridden with `-Dkey=value`:

```properties
api.baseUrl=https://qa-assignment.dev1.whalebone.io
ui.baseUrl=http://uitestingplayground.com
ui.timeoutMs=30000
ui.browser=chromium        # chromium | firefox | webkit | chrome | msedge
ui.headed=false
ui.slowMoMs=0
ui.viewport.width=1920
ui.viewport.height=1080
```

---

## Install Playwright browsers

```bash
mvn -q com.microsoft.playwright:playwright-maven-plugin:install
```

---

## Run tests

```bash
# Full suite
mvn clean test

# Single class
mvn -Dtest=cz.whalebone.tests.api.TeamsApiTests test

# Single method
mvn -Dtest=UiTestingPlaygroundTests#progressBar_followScenario_waitTo75_thenStop test

# Headed browser (useful for local debugging)
mvn clean test -Dui.headed=true -Dui.browser=firefox
```

---

## Allure report

```bash
mvn clean test
mvn allure:serve
```

---

## Docker

```bash
# Build and run (headless)
docker compose up --build

# Or manually
docker build -t qa-tests .
docker run --rm qa-tests
```

---
---

## Reliability and debugging features

- **Playwright trace + video on failure** saved under `playwright-artifacts/` and attached to Allure for failed UI tests.
- **DOM snapshot and failing URL** attached to Allure for faster triage.
- **Configurable retries** via a TestNG annotation transformer:
  - `ui.retry.count` for UI tests
  - `api.retry.count` for API tests
- **API contract test** using RestAssured JSON Schema Validator (`schema/teams.schema.json`).
- **Repository hygiene**: `target/`, IDE files, Allure binaries, and artifacts are ignored via `.gitignore`.

### Useful flags

All values can be overridden via `-Dkey=value`.

- `artifacts.dir` (default: `playwright-artifacts`)
- `ui.trace.onFailure` (default: `true`)
- `ui.video.onFailure` (default: `true`)
- `ui.screenshot.onFailure` (default: `true`)
- `ui.retry.count` (default: `0`)
- `api.retry.count` (default: `0`)

