//canvas setpup
let board, ctx;

const TILE = 32;
const SPEED = 4;
const FPS = 20;

let isGameOver = false;

// X = wall
// P = player
// G = A* ghost, B = BFS ghost, C = ACO ghost (added because i worked with aco last semester)
const tileMap = [
  "XXXXXXXXXXXXXXXXXXX",
  "X        X        X",
  "X XX XXX X XXX XX X",
  "X        C        X",
  "X XX X XXXXX X XX X",
  "X    X       X    X",
  "XXXX XXXX XXXX XXXX",
  "OOOX X       X XOOO",
  "XXXX X XXrXX X XXXX",
  "X        G        X",
  "XXXX X XXXXX X XXXX",
  "XOO      B      OOX",
  "XXXX X XXXXX X XXXX",
  "X        X        X",
  "X XX XXX X XXX XX X",
  "X  X     P     X  X",
  "XX X X XXXXX X X XX",
  "X    X   X   X    X",
  "X XXXXXX X XXXXXX X",
  "X                 X",
  "XXXXXXXXXXXXXXXXXXX"
];

const rows = tileMap.length;
const cols = Math.max(...tileMap.map((r) => r.length));
const boardWidth = TILE * cols;
const boardHeight = TILE * rows;

class Block {
  constructor(image, x, y, w, h) {
    this.image = image;
    this.x = x;
    this.y = y;
    this.width = w;
    this.height = h;

    // for the resets
    this.startX = x;
    this.startY = y;

    this.velocityX = 0;
    this.velocityY = 0;

    // ghosts
    this.ai = undefined; // "astar" | "bfs" | "aco"
    this.pheromone = undefined; 
  }
}

const walls = new Set();
const ghosts = new Set();
let pacman = null;

//global inputs
const keys = { ArrowUp: false, ArrowDown: false, ArrowLeft: false, ArrowRight: false };
let lastKey = "";

window.addEventListener(
  "keydown",
  (e) => {
    if (keys[e.key] !== undefined) {
      e.preventDefault();
      keys[e.key] = true;
      lastKey = e.key;
    }

    //restart (afterthought)
    if (isGameOver && e.key === "Enter") location.reload();
  },
  { passive: false }
);

window.addEventListener("keyup", (e) => {
  if (keys[e.key] !== undefined) keys[e.key] = false;
});

// self explanitory
function randInt(n) {
  return (Math.random() * n) | 0;
}

function loadImage(src) {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.onload = () => resolve(img);
    img.onerror = () => reject(new Error("couldn't load " + src));
    img.src = src;
  });
}

window.onload = async () => {
  board = document.getElementById("board");
  board.width = boardWidth;
  board.height = boardHeight;

  ctx = board.getContext("2d");
  ctx.imageSmoothingEnabled = false;

  try {
    const [playerImg, ghostImg, wallImg] = await Promise.all([
      loadImage("standing-up-man-.png"),
      loadImage("ghost.png"),
      loadImage("wall.png")
    ]);

    buildMap(playerImg, ghostImg, wallImg);
    gameLoop();
  } catch (err) {
    console.error("asset loading failed:", err);
  }
};

function buildMap(playerImg, ghostImg, wallImg) {
  walls.clear();
  ghosts.clear();
  pacman = null;

  const aiByChar = { G: "astar", B: "bfs", C: "aco" };

  for (let r = 0; r < rows; r++) {
    for (let c = 0; c < cols; c++) {
      const ch = tileMap[r][c] || " ";
      const x = c * TILE;
      const y = r * TILE;

      if (ch === "X") {
        walls.add(new Block(wallImg, x, y, TILE, TILE));
      } else if (ch === "P") {
        pacman = new Block(playerImg, x, y, TILE, TILE);
      } else if (aiByChar[ch]) {
        const g = new Block(ghostImg, x, y, TILE, TILE);
        g.ai = aiByChar[ch];
        if (g.ai === "aco") g.pheromone = new Map();
        ghosts.add(g);
      }
    }
  }

  // fic the ghosts just sitting there by giving them a direction to start
  ghosts.forEach((g) => {
    const moves = getValidMoves(g);
    if (!moves.length) return;
    const pick = moves[randInt(moves.length)];
    g.velocityX = pick.x;
    g.velocityY = pick.y;
  });
}

function gameLoop() {
  if (!isGameOver) {
    updatePlayer();

    ghosts.forEach((g) => {
      moveGhost(g);
      if (pacman && checkCollision(pacman, g)) {
        // console.log("ded");
        isGameOver = true;
      }
    });
  }

  draw();

  if (!isGameOver) setTimeout(gameLoop, 1000 / FPS);
}

function updatePlayer() {
  if (!pacman) return;

  // just the directions really
  if (keys.ArrowUp && lastKey === "ArrowUp") tryMove(pacman, 0, -SPEED);
  else if (keys.ArrowDown && lastKey === "ArrowDown") tryMove(pacman, 0, SPEED);
  else if (keys.ArrowLeft && lastKey === "ArrowLeft") tryMove(pacman, -SPEED, 0);
  else if (keys.ArrowRight && lastKey === "ArrowRight") tryMove(pacman, SPEED, 0);

  if (hitsWall(pacman, pacman.velocityX, pacman.velocityY)) {
    pacman.velocityX = 0;
    pacman.velocityY = 0;
  }

  pacman.x += pacman.velocityX;
  pacman.y += pacman.velocityY;
}

function tryMove(ent, vx, vy) {
  // snap-to-grid turning mechanism for simplicity
  if (vx === 0) {
    const snapX = Math.round(ent.x / TILE) * TILE;
    if (Math.abs(ent.x - snapX) <= SPEED) ent.x = snapX;
    else return;
  } else {
    const snapY = Math.round(ent.y / TILE) * TILE;
    if (Math.abs(ent.y - snapY) <= SPEED) ent.y = snapY;
    else return;
  }

  if (!hitsWall(ent, vx, vy)) {
    ent.velocityX = vx;
    ent.velocityY = vy;
  }
}

function getValidMoves(ent) {
  const dirs = [
    { x: SPEED, y: 0 },
    { x: -SPEED, y: 0 },
    { x: 0, y: SPEED },
    { x: 0, y: -SPEED }
  ];
  return dirs.filter((v) => !hitsWall(ent, v.x, v.y));
}

function hitsWall(ent, vx, vy) {
  const nx = ent.x + vx;
  const ny = ent.y + vy;

  for (const w of walls) {
    if (
      nx < w.x + w.width &&
      nx + ent.width > w.x &&
      ny < w.y + w.height &&
      ny + ent.height > w.y
    ) {
      return true;
    }
  }
  return false;
}

function checkCollision(a, b) {
  return (
    a.x < b.x + b.width &&
    a.x + a.width > b.x &&
    a.y < b.y + b.height &&
    a.y + a.height > b.y
  );
}

//grid helper functions
function getTileAt(r, c) {
  return tileMap[r]?.[c] || "X";
}

function isWalkable(r, c) {
  return getTileAt(r, c) !== "X";
}

function keyOf(r, c) {
  return `${r},${c}`;
}

function getEntityTile(ent) {
  const c = Math.floor((ent.x + ent.width / 2) / TILE);
  const r = Math.floor((ent.y + ent.height / 2) / TILE);
  return { r, c };
}

function manhattan(a, b) {
  return Math.abs(a.r - b.r) + Math.abs(a.c - b.c);
}

function getNeighbors(t) {
  const n = [
    { r: t.r + 1, c: t.c },
    { r: t.r - 1, c: t.c },
    { r: t.r, c: t.c + 1 },
    { r: t.r, c: t.c - 1 }
  ];
  return n.filter((p) => isWalkable(p.r, p.c));
}

function buildPath(cameFrom, goalKey) {
  const path = [];
  let cur = goalKey;

  while (cur) {
    const [r, c] = cur.split(",").map(Number);
    path.push({ r, c });
    cur = cameFrom.get(cur);
  }

  path.reverse();
  return path;
}
//ghost AI logic, heavily inspiered  by cs50ai's version of a* searcvh in ps2? or 1? idk
function findAStar(start, goal) {
  const startKey = keyOf(start.r, start.c);
  const goalKey = keyOf(goal.r, goal.c);
  if (startKey === goalKey) return [start];

  // TODO: priority queue
  const open = [startKey];
  const openSet = new Set([startKey]);

  const cameFrom = new Map();
  const gScore = new Map([[startKey, 0]]);
  const fScore = new Map([[startKey, manhattan(start, goal)]]);

  while (open.length) {
    let bestIdx = 0;
    let bestF = fScore.get(open[0]) ?? Infinity;

    //this is really wierd? but somehow it works?
    for (let i = 1; i < open.length; i++) {
      const f = fScore.get(open[i]) ?? Infinity;
      if (f < bestF) {
        bestF = f;
        bestIdx = i;
      }
    }

    const currentKey = open.splice(bestIdx, 1)[0];
    openSet.delete(currentKey);

    if (currentKey === goalKey) return buildPath(cameFrom, goalKey);

    const [r, c] = currentKey.split(",").map(Number);
    const curG = gScore.get(currentKey) ?? Infinity;

    for (const nb of getNeighbors({ r, c })) {
      const nbKey = keyOf(nb.r, nb.c);
      const tentative = curG + 1;

      if (tentative < (gScore.get(nbKey) ?? Infinity)) {
        cameFrom.set(nbKey, currentKey);
        gScore.set(nbKey, tentative);
        fScore.set(nbKey, tentative + manhattan(nb, goal));

        if (!openSet.has(nbKey)) {
          open.push(nbKey);
          openSet.add(nbKey);
        }
      }
    }
  }

  return null;
}

// bfs, again pretty much the thing from cs50ai
function findBfs(start, goal) {
  const startKey = keyOf(start.r, start.c);
  const goalKey = keyOf(goal.r, goal.c);
  if (startKey === goalKey) return [start];

  const queue = [start];
  let i = 0;

  const visited = new Set([startKey]);
  const cameFrom = new Map();

  while (i < queue.length) {
    const cur = queue[i++];
    const curKey = keyOf(cur.r, cur.c);

    if (curKey === goalKey) return buildPath(cameFrom, goalKey);

    for (const nb of getNeighbors(cur)) {
      const nbKey = keyOf(nb.r, nb.c);
      if (visited.has(nbKey)) continue;

      visited.add(nbKey);
      cameFrom.set(nbKey, curKey);
      queue.push(nb);
    }
  }

  return null;
}

// tried to make ACO by using the java code from last semester but its not really what i wante dthis to be, but i had fun lol
function findAco(ghost, start, goal) {
  // pheromones per ghost
  const pher = ghost.pheromone || (ghost.pheromone = new Map());

  // magic numbres go brr
  const weight = 1.0;     
  const distWeight = 2.8; 
  const decay = 0.22;    
  const Q = 35;

  const startKey = keyOf(start.r, start.c);
  const goalKey = keyOf(goal.r, goal.c);
  if (startKey === goalKey) return [start];

  const edgeKey = (a, b) => `${a}|${b}`;
  const getPheromone = (a, b) => pher.get(edgeKey(a, b)) || 1.0;

  function evaporate() {
    for (const [k, v] of pher.entries()) {
      const nv = v * (1 - decay);
      if (nv < 0.01) pher.delete(k);
      else pher.set(k, nv);
    }
  }

  function deposit(path) {
    const cost = path.length - 1;
    if (cost <= 0) return;

    const add = Q / cost;
    for (let j = 0; j < path.length - 1; j++) {
      const aKey = keyOf(path[j].r, path[j].c);
      const bKey = keyOf(path[j + 1].r, path[j + 1].c);
      const k = edgeKey(aKey, bKey);
      pher.set(k, (pher.get(k) || 1.0) + add);
    }
  }

  function pickNext(cur, tgt, seen) {
    const nbs = getNeighbors(cur);
    if (!nbs.length) return null;

    const curK = keyOf(cur.r, cur.c);
    let sum = 0;
    const bag = [];

    for (const nb of nbs) {
      const nbK = keyOf(nb.r, nb.c);

      // move closer
      const eta = 1 / (manhattan(nb, tgt) + 1);

      // mostly copied this mfrom my work last semester, should be right tho
      let w = Math.pow(getPheromone(curK, nbK), weight) * Math.pow(eta, distWeight);
      if (seen.has(nbK)) w *= 0.05; 

      bag.push({ nb, w });
      sum += w;
    }

    if (sum <= 0) return nbs[randInt(nbs.length)];

    let r = Math.random() * sum;
    for (const it of bag) {
      r -= it.w;
      if (r <= 0) return it.nb;
    }
    return bag[bag.length - 1].nb;
  }

  let bestPath = null;
  let bestCost = Infinity;

  // 6 iterations feel alright, about 12mb of memory in firefox on my mac
  for (let it = 0; it < 6; it++) {
    let iterBest = null;
    let iterCost = Infinity;

    for (let ant = 0; ant < 16; ant++) {
      let cur = start;
      const path = [cur];
      const seen = new Set([startKey]);
      let reached = false;

      // hard cap
      for (let step = 0; step < 80; step++) {
        const curK = keyOf(cur.r, cur.c);
        if (curK === goalKey) {
          reached = true;
          break;
        }

        const next = pickNext(cur, goal, seen);
        if (!next) break;

        cur = next;
        const nk = keyOf(cur.r, cur.c);
        path.push(cur);
        seen.add(nk);

        if (nk === goalKey) {
          reached = true;
          break;
        }
      }

      if (reached) {
        const cost = path.length - 1;
        if (cost < iterCost) {
          iterCost = cost;
          iterBest = path.slice();
        }
      }
    }

    evaporate();
    if (iterBest) deposit(iterBest);

    if (iterBest && iterCost < bestCost) {
      bestCost = iterCost;
      bestPath = iterBest;
    }
  }

  return bestPath;
}

// ghost movement
function moveGhost(ghost) {
  // decide at intersections
  if (ghost.x % TILE === 0 && ghost.y % TILE === 0) {
    let decided = false;

    if (pacman) {
      const start = getEntityTile(ghost);
      const goal = getEntityTile(pacman);

      let path = null;
      if (ghost.ai === "bfs") path = findBfs(start, goal);
      else if (ghost.ai === "aco") path = findAco(ghost, start, goal) || findBfs(start, goal);
      else path = findAStar(start, goal);

      if (path && path.length > 1) {
        const next = path[1];
        const dx = next.c - start.c;
        const dy = next.r - start.r;

        tryMove(ghost, dx * SPEED, dy * SPEED);
        decided = true;
      }
    }

    // fallback, this was a fix - ive not seen it use this so far
    if (!decided) {
      const options = getValidMoves(ghost);
      if (options.length) {
        const revX = -ghost.velocityX;
        const revY = -ghost.velocityY;

        // dont reverse its just dumb to look at
        let candidates = options;
        if (options.length > 1) {
          const filtered = options.filter((v) => !(v.x === revX && v.y === revY));
          if (filtered.length) candidates = filtered;
        }

        const pick = candidates[randInt(candidates.length)];
        tryMove(ghost, pick.x, pick.y);
      }
    }
  }

  if (hitsWall(ghost, ghost.velocityX, ghost.velocityY)) {
    ghost.velocityX = 0;
    ghost.velocityY = 0;
    return;
  }

  ghost.x += ghost.velocityX;
  ghost.y += ghost.velocityY;
}

// render funciton, mostly got this from a yt video 
function draw() {
  // background
  ctx.fillStyle = "rgb(123, 112, 112)";
  ctx.fillRect(0, 0, boardWidth, boardHeight);

  walls.forEach((w) => ctx.drawImage(w.image, w.x, w.y, w.width, w.height));
  ghosts.forEach((g) => ctx.drawImage(g.image, g.x, g.y, g.width, g.height));
  if (pacman) ctx.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height);

  if (!isGameOver) return;

  ctx.fillStyle = "rgba(0,0,0,0.6)";
  ctx.fillRect(0, 0, boardWidth, boardHeight);

  ctx.fillStyle = "white";
  ctx.textAlign = "center";

  ctx.font = "48px sans-serif";
  ctx.fillText("GAME OVER", boardWidth / 2, boardHeight / 2);

  ctx.font = "20px sans-serif";
  ctx.fillText("Press Enter to restart", boardWidth / 2, boardHeight / 2 + 40);
}


//test tihs mess
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
      checkCollision,
      manhattan,
      getValidMoves,
      findAStar, 
      Block,  
      walls,     
    };
  }