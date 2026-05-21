# JS Mockup-Gameversion for Testing
A simple Game implementation using HTML5 Canvas and Vanilla JS.

Here is the demo version: https://herrei.github.io/mockup/

I built this as a mockup for cs108 at unibasel to get an Idea if the game idea of our team is playable. i mainly used these rescources:
cs50web that i took and got familiar with js and css
cs50Ai that i took and got the ideas for bfs a* for
My work on the ACO-Algorithm, if intrested in the code of this project, its on my account.
This youtube toutorial due to the inherit similarieties of this as a 2D map and the idea of the game pacman, 
this is also why the file is called pacman.js as i zoned out when doign this lol
:https://www.youtube.com/watch?v=WxeTMsaSOaA&t=3774s
Also i used ai Autocomplete with continue and qwen 8b, but its really useless tbh

# Ghost AI
Different ghosts use different algorithms to navigate the grid:
Red (G):Uses A (A-Star). It's the most aggressive and takes the shortest path.
Pink (B): Uses BFS (Breadth-First Search). Similar to A but explores differently.
Orange (C): Uses ACO. This is experimental; it leaves pheromones on paths. It's a bit chaotic but works.

# How to Run
No build step required for the game itself.
Clone the repo.
Open `index.html` in browser.

# Controls
- Arrow Keys: Move
- Enter: Restart game when dead

# Development / Testing
I used Jest for unit testing the physics and pathfinding logic.
