# README

## PhantomHunt - Multiplayer Game ##

PhantomHunt is a strategic, turn-based 2D multiplayer game developed as part of the CS108 programming project. Experience thrilling chases inside a cursed castle where teamwork meets pure survival instinct.

## The Game Concept ##

In PhantomHunt, two unequal teams compete against each other: three ghosts versus one human. The setting: an unsuspecting human finds themselves inside a haunted castle. While the ghosts try to drive the intruder into madness, every second counts for the human trying to escape.

## Core Mechanics ##

**Asymmetric Gameplay:**
3 vs. 1 – each role has different abilities and objectives.

**Scoring System:**
- The human earns points by surviving as long as possible and successfully finishing a round.
- The ghosts earn points by locating and scaring the human.

**Fairness through rotation:**
A match consists of four rounds. Each player takes the role of the human exactly once. After the final round, the player with the highest total score wins the match.

## How to Start the Game

**Build the Executable Jar**
Before running the game, you need to compile the code and build the executable .jar file. Open your terminal in the root directory of the project and run:

**./gradlew jar**

**Run the Application**
Once the build is successful you can start the game via the terminal using the following syntax:

java -jar build/libs/phantom-hunt.jar **mode address/address:port**

**Starting as a Server:**
To host a server, set the mode to server and specify the port you want to open.
For example:
**java -jar build/libs/phantom-hunt.jar server 2222**

**Starting as a Client:**
To join as a client, set the mode to client and provide the IP address and port of the host server.
For example:
**java -jar build/libs/phantom-hunt.jar client 192.168.1.9:2222**