# PhantomHunt

![PhantomHunt gameplay screenshot](outreach/screenshot.png)

**PhantomHunt** is a Java/JavaFX multiplayer game developed for the CS108 Programming Project at the University of Basel in 2026.

It is an asymmetric **3-vs-1 chase game** set in a haunted castle: one player tries to survive as the human, while three ghosts coordinate to hunt them down.

This repository is my personal GitHub version of the original team project. It contains the final Java/JavaFX multiplayer game and also includes my early browser-based mockup in `mockup/`, which I built at the start of the project to test whether the core game idea felt playable.

---

## Media

- [Gameplay video](outreach/gameplay.mp4)
- [Trailer / outreach video](outreach/video.mp4)
- [Game manual PDF](outreach/manual.pdf)

> If the videos do not preview directly in GitHub, download them from the `outreach/` folder.

---

## My Contributions

My main work focused on the networking, backend, and game-state side of the project.

I worked on:

- client-server communication
- lobby and game handling
- registry and player name logic
- chat, whisper, and lobby command parsing
- spectator support
- round handling and role rotation
- scoring and persistent highscore logic
- game rules and game-state synchronization
- sound synchronization and menu music behavior
- latency and stability fixes
- protocol work
- CI/build fixes
- this personal GitHub version with the integrated mockup

The commit history shows that my largest areas of work were the networking and game-backend side: lobby state handling, registry behavior, player names, whisper/lobby parsing, spectator support, round handling, highscore handling, game rules, sound synchronization, and several latency/stability fixes.

---

## About the Game

PhantomHunt is an asymmetric **3 vs. 1 multiplayer game**.

One player is the **human**. The human tries to survive for as long as possible, avoid the ghosts, and collect points.

The other three players are **ghosts**. Their goal is to coordinate, hunt down the human, and catch them before the round ends.

A full match consists of four rounds. Roles rotate after each round so every player gets one turn as the human. At the end, the player with the highest individual score wins.

---

## Features

- 3 vs. 1 asymmetric multiplayer gameplay
- Java client-server architecture
- JavaFX GUI
- lobby creation and joining
- global chat and whisper messages
- spectator support
- role rotation across four rounds
- scoring system for humans and ghosts
- persistent highscore handling
- keyboard controls and configurable key bindings
- controller support
- sound effects and synchronized menu music
- custom map, player sprites, ghost sprites, and intro assets
- Wisdom mechanic and Wisdom Blessing ability
- automated tests with JaCoCo coverage reports

---

## Tech Stack

- Java
- JavaFX
- Gradle
- JUnit
- JaCoCo
- HTML5 Canvas / JavaScript prototype in `mockup/`

---

## Requirements

- Java 21 or newer
- Gradle wrapper included

---

## How to Build

From the project root, run:

```bash
./gradlew jar
```

The executable JAR will be created under:

```text
build/libs/phantom-hunt.jar
```

---

## How to Run

PhantomHunt is a multiplayer game. One instance must run as the server, and players connect as clients.

Start a server:

```bash
java -jar build/libs/phantom-hunt.jar server 2222
```

Start a client:

```bash
java -jar build/libs/phantom-hunt.jar client 127.0.0.1:2222
```

For multiplayer over a network, replace `127.0.0.1` with the host machine's IP address.

---

## Controls

Default movement uses keyboard controls. The game also includes configurable key bindings and controller support.

| Action | Key |
|---|---|
| Move up | W |
| Move left | A |
| Move down | S |
| Move right | D |
| Wisdom Blessing | Shift + R |

---

## Testing

Run the Java test suite with:

```bash
./gradlew test
```

After running tests, reports are generated at:

```text
build/reports/tests/test/index.html
build/reports/jacoco/test/html/index.html
```

The mockup has its own JavaScript test setup inside `mockup/`:

```bash
cd mockup
npm install
npm test
```

---

## Early Browser Mockup

The `mockup/` folder contains a small HTML5 Canvas prototype that I built before the final Java implementation existed.

It was not the final game, but it helped explore the basic idea of a grid-based chase game early in the project.

The mockup includes:

- browser-based movement on a tile map
- a player character and ghost enemies
- wall collision
- simple restart behavior
- experimental ghost pathfinding using A*, BFS, and an ACO-inspired approach
- Jest tests for some core logic

You can run the mockup by opening:

```text
mockup/index.html
```

The original standalone mockup repository was:

```text
https://github.com/HerRei/mockup
```

---

## Team

PhantomHunt was built by the CS108 team **ProgrammierLoewen**.

- **Hermes Reisner** — client-server logic, lobby/game handling, networking fixes, sound system work, scoring/highscore logic, protocol work, CI/build fixes, and this personal GitHub version with the mockup
- **Jan Valentin Haag** — early server/client architecture, registry and client handling, chat and UI integration, GameState logic, lobby reset handling, controller support, and Wisdom Blessing logic
- **Silas Weber** — JavaFX frontend, scenes, GUI structure, lobby/game/end screens, key bindings, fullscreen support, highscore scene, movement/gameplay integration, and visual consistency
- **Ismail Djemaili** — artwork and visual assets, map and sprite work, game manual/outreach material, intro/logo work, lobby and map-related features, and executable build/pipeline setup
- **Vera Bitterlin** — test infrastructure, unit tests, JaCoCo coverage, QA reports, refactoring, JavaFX/backend test separation, static analysis cleanup, and stability fixes

---

## Project Structure

```text
src/main/java/        Java source code for the final game
src/main/resources/   game assets, audio, sprites, intro media
src/test/java/        Java unit tests
documentations/       project documentation and milestone material
outreach/             screenshot, trailer/gameplay video, manual, kiosk metadata
mockup/               early HTML5 Canvas prototype
```

---

## Project Context

The original project was developed as a team project on the University of Basel SciCore GitLab server.

This GitHub repository is my own personal archive and showcase version of that project. The main game remains a team project, while the added `mockup/` folder documents my early design and prototyping work before the final Java version took shape.

---

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.
