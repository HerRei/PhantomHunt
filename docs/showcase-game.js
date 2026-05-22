const workDetails = {
  backend: {
    title: "Backend and game-state logic",
    items: [
      "Lobby and match handling",
      "Round lifecycle and role rotation",
      "Scoring and persistent highscore behavior",
      "Game rules and state synchronization",
      "Registry and generated player names"
    ]
  },
  networking: {
    title: "Client-server and protocol work",
    items: [
      "TCP client/server communication",
      "Lobby command parsing",
      "Chat and private whisper handling",
      "Spectator support",
      "Latency and stability fixes"
    ]
  },
  gameplay: {
    title: "Gameplay systems",
    items: [
      "Wisdom Blessing flow",
      "Ability and scoring interactions",
      "Sound and menu music synchronization",
      "Controller and keyboard integration support",
      "Final game showcase integration"
    ]
  },
  quality: {
    title: "Project quality",
    items: [
      "CI and build fixes",
      "Protocol cleanup",
      "Focused unit tests",
      "Documentation and manual integration",
      "Personal repo presentation site"
    ]
  }
};

const architectureText = {
  client:
    "The JavaFX client owns scenes, input handling, sprites, chat UI, key bindings, controller support, and the local presentation of server state.",
  server:
    "The server owns lobby creation, client registration, match start, player routing, sound timing, and authoritative game updates.",
  game:
    "Game state tracks rounds, roles, map collision, player positions, abilities, scoring, and the win conditions for the 3 vs 1 match.",
  protocol:
    "The protocol layer keeps messages explicit so chat, lobby actions, game settings, movement, and state updates can stay synchronized."
};

function setupWorkShowcase() {
  const detail = document.querySelector("#workDetail");
  const tabs = [...document.querySelectorAll("[data-work-tab]")];
  const archButtons = [...document.querySelectorAll("[data-arch]")];
  const archText = document.querySelector("#architectureText");

  function renderWork(key) {
    const data = workDetails[key];
    detail.innerHTML = `
      <h3>${data.title}</h3>
      <ul>${data.items.map((item) => `<li>${item}</li>`).join("")}</ul>
    `;
    tabs.forEach((tab) => tab.classList.toggle("active", tab.dataset.workTab === key));
  }

  function renderArchitecture(key) {
    archText.textContent = architectureText[key];
    archButtons.forEach((button) => button.classList.toggle("active", button.dataset.arch === key));
  }

  tabs.forEach((tab) => tab.addEventListener("click", () => renderWork(tab.dataset.workTab)));
  archButtons.forEach((button) =>
    button.addEventListener("click", () => renderArchitecture(button.dataset.arch))
  );

  renderWork("backend");
  renderArchitecture("client");
}

class PhantomHuntDemo {
  constructor(canvas) {
    this.canvas = canvas;
    this.ctx = canvas.getContext("2d");
    this.tile = 32;
    this.map = [
      "XXXXXXXXXXXXXXXXXXX",
      "XLHHTHHHRXLHHHTHHRX",
      "XVXXVXXXVXVXXXVXXVX",
      "XCHHQHTHBHBHTHQHHEX",
      "XVXXVXVXXXXXVXVXXVX",
      "XDHHEXDHRXLHAXCHHAX",
      "XXXXVXXXVXVXXXVXXXX",
      "XLHHEXLHBTBHRXCHHRX",
      "XVXXCHEXXVXXCHEXXVX",
      "XDRXVXVXXVXXVXVXLAX",
      "XXVXVXCHHQHHEXVXVXX",
      "XLBHEXVXXVXXVXCHBRX",
      "XVXXCHBHTBTHBHEXXVX",
      "XDRXVXXXVXVXXXVXLAX",
      "XXVXVXLHBHBHRXVXVXX",
      "XLBHAXVXXXXXVXDHBRX",
      "XVXXXXDHRXLHAXXXXVX",
      "XVXXXXXXVXVXXXXXXVX",
      "XDHHHHHHBHBHHHHHHAX",
      "XXXXXXXXXXXXXXXXXXX"
    ];
    this.tileImages = {};
    this.humanImages = {};
    this.ghostImages = {};
    this.glitchImages = {};
    this.abilityImage = null;
    this.keys = new Set();
    this.lastDirection = "up";
    this.running = false;
    this.gameOver = false;
    this.lastFrame = 0;
    this.elapsed = 0;
    this.score = 0;
    this.round = 1;
    this.abilityActive = false;
    this.abilityReady = false;
    this.abilityTimer = 0;
    this.status = "Ready";
    this.mode = "steady";
    this.queuedDirection = null;
    this.human = null;
    this.ghosts = [];
    this.pickup = { row: 11, col: 9, collected: false };
    this.ui = {
      time: document.querySelector("#demoTime"),
      score: document.querySelector("#demoScore"),
      round: document.querySelector("#demoRound"),
      status: document.querySelector("#demoStatus"),
      start: document.querySelector("#startDemo"),
      reset: document.querySelector("#resetDemo"),
      ability: document.querySelector("#abilityDemo")
    };
  }

  async init() {
    this.drawMessage("Loading PhantomHunt demo...");
    try {
      await this.loadAssets();
      this.reset();
      this.bindControls();
      this.draw();
    } catch (error) {
      console.error(error);
      this.status = "Asset error";
      this.updateUi();
      this.drawMessage("Demo assets could not load", "Refresh the page or open it from the GitHub Pages URL.");
    }
  }

  async loadAssets() {
    const image = (src) =>
      new Promise((resolve, reject) => {
        const img = new Image();
        img.onload = () => resolve(img);
        img.onerror = () => reject(new Error(`Could not load ${src}`));
        img.src = src;
      });

    const floorNames = {
      Q: "quadra",
      L: "top_left",
      V: "vertical",
      D: "down_left",
      R: "top_right",
      A: "down_right",
      H: "horizontal",
      T: "triple_top",
      B: "triple_down",
      C: "triple_left",
      E: "triple_right"
    };

    await Promise.all(
      Object.entries(floorNames).map(async ([key, name]) => {
        this.tileImages[key] = await image(`assets/game/floors/${name}.png`);
      })
    );

    const humanDirs = ["front", "back", "left", "right"];
    await Promise.all(
      humanDirs.flatMap((dir) =>
        [1, 2].map(async (frame) => {
          this.humanImages[`${dir}${frame}`] = await image(`assets/game/humans/p1_${dir}${frame}.png`);
        })
      )
    );

    const ghosts = [
      ["red", "r"],
      ["blue", "b"],
      ["green", "g"]
    ];
    const dirs = { up: "u", down: "d", left: "l", right: "r" };
    await Promise.all(
      ghosts.flatMap(([, color]) =>
        Object.entries(dirs).flatMap(([dir, suffix]) => [
          image(`assets/game/ghosts/${color}${suffix}_ghost.png`).then((img) => {
            this.ghostImages[`${color}_${dir}`] = img;
          }),
          image(`assets/game/ghosts/${color}${suffix}_glitch.png`).then((img) => {
            this.glitchImages[`${color}_${dir}`] = img;
          })
        ])
      )
    );

    this.abilityImage = await image("assets/game/abilities/ability.png");
  }

  bindControls() {
    window.addEventListener("keydown", (event) => {
      const dir = this.directionFromKey(event.key);
      if (!dir && event.key.toLowerCase() !== "r") return;
      event.preventDefault();
      if (dir) {
        this.keys.add(dir);
        this.queuedDirection = dir;
        this.lastDirection = dir;
        this.canvas.focus({ preventScroll: true });
      } else {
        this.activateAbility();
      }
    });

    window.addEventListener("keyup", (event) => {
      const dir = this.directionFromKey(event.key);
      if (dir) this.keys.delete(dir);
    });

    this.ui.start.addEventListener("click", () => this.toggle());
    this.ui.reset.addEventListener("click", () => this.reset());
    this.ui.ability.addEventListener("click", () => this.activateAbility());

    document.querySelectorAll("[name='demoMode']").forEach((input) => {
      input.addEventListener("change", () => {
        this.mode = input.value;
      });
    });

    document.querySelectorAll("[data-hold]").forEach((button) => {
      const dir = button.dataset.hold;
      const press = (event) => {
        event.preventDefault();
        this.keys.add(dir);
        this.queuedDirection = dir;
        this.lastDirection = dir;
        this.canvas.focus({ preventScroll: true });
      };
      const release = () => this.keys.delete(dir);
      button.addEventListener("pointerdown", press);
      button.addEventListener("pointerup", release);
      button.addEventListener("pointerleave", release);
      button.addEventListener("pointercancel", release);
    });
  }

  directionFromKey(key) {
    const normalized = key.toLowerCase();
    if (normalized === "w" || key === "ArrowUp") return "up";
    if (normalized === "a" || key === "ArrowLeft") return "left";
    if (normalized === "s" || key === "ArrowDown") return "down";
    if (normalized === "d" || key === "ArrowRight") return "right";
    return null;
  }

  reset() {
    this.running = false;
    this.gameOver = false;
    this.elapsed = 0;
    this.score = 0;
    this.round = 1;
    this.abilityActive = false;
    this.abilityReady = false;
    this.abilityTimer = 0;
    this.status = "Ready";
    this.pickup = { row: 11, col: 9, collected: false };
    this.human = this.entity(1, 1, "human", "down", "p1");
    this.ghosts = [
      this.entity(17, 1, "ghost", "down", "r"),
      this.entity(1, 18, "ghost", "up", "b"),
      this.entity(17, 18, "ghost", "up", "g")
    ];
    this.lastDirection = "right";
    this.queuedDirection = null;
    this.keys.clear();
    this.ui.start.textContent = "Start";
    this.updateUi();
    this.draw();
  }

  entity(col, row, type, direction, variant) {
    return {
      x: col * this.tile,
      y: row * this.tile,
      width: this.tile,
      height: this.tile,
      type,
      direction,
      variant,
      vx: 0,
      vy: 0,
      frame: 0,
      frameClock: 0,
      stunned: 0
    };
  }

  toggle() {
    if (this.gameOver) this.reset();
    this.running = !this.running;
    this.status = this.running ? "Survive" : "Paused";
    this.ui.start.textContent = this.running ? "Pause" : "Start";
    this.canvas.focus({ preventScroll: true });
    this.updateUi();
    if (this.running) {
      this.lastFrame = performance.now();
      requestAnimationFrame((time) => this.loop(time));
    } else {
      this.draw();
    }
  }

  loop(time) {
    if (!this.running) return;
    const delta = Math.min((time - this.lastFrame) / 1000, 0.05);
    this.lastFrame = time;
    this.update(delta);
    this.draw();
    requestAnimationFrame((next) => this.loop(next));
  }

  update(delta) {
    if (this.gameOver) return;

    this.elapsed += delta;
    this.score += delta;
    this.updateHuman(delta);
    this.updateGhosts(delta);
    this.checkPickup();
    this.checkCollisions();

    if (this.abilityActive) {
      this.abilityTimer -= delta;
      if (this.abilityTimer <= 0) {
        this.abilityActive = false;
        this.status = "Survive";
      }
    }

    if (this.elapsed >= 50) {
      this.round = Math.min(4, this.round + 1);
      this.score += 50;
      this.elapsed = 0;
      this.status = this.round >= 4 ? "Final round" : "Round survived";
      this.teleportRound();
    }

    this.updateUi();
  }

  updateHuman(delta) {
    const speed = 80;

    if (this.queuedDirection) {
      this.trySetVelocity(this.human, this.queuedDirection, speed);
    }

    this.moveWithVelocity(this.human, delta);
    this.animate(this.human, delta);
  }

  updateGhosts(delta) {
    const speed = this.mode === "aggressive" ? 94 : 78;
    this.ghosts.forEach((ghost) => {
      if (ghost.stunned > 0) {
        ghost.stunned -= delta;
        this.animate(ghost, delta);
        return;
      }

      if (this.isAligned(ghost)) {
        const path = this.findPath(this.tileAtEntity(ghost), this.tileAtEntity(this.human));
        if (path && path.length > 1) {
          const next = path[1];
          const current = this.tileAtEntity(ghost);
          const dir = this.directionFromDelta(next.col - current.col, next.row - current.row);
          this.trySetVelocity(ghost, dir, speed);
        }
      }

      this.moveWithVelocity(ghost, delta);
      this.animate(ghost, delta);
    });
  }

  trySetVelocity(entity, direction, speed) {
    if (!direction) return;
    const vector = this.vector(direction, speed);
    const probeStep = 4;

    if (direction === "up" || direction === "down") {
      const snap = Math.round(entity.x / this.tile) * this.tile;
      if (Math.abs(entity.x - snap) <= probeStep) entity.x = snap;
      else return false;
    } else {
      const snap = Math.round(entity.y / this.tile) * this.tile;
      if (Math.abs(entity.y - snap) <= probeStep) entity.y = snap;
      else return false;
    }

    const next = {
      ...entity,
      x: entity.x + Math.sign(vector.x) * probeStep,
      y: entity.y + Math.sign(vector.y) * probeStep
    };
    if (this.hitsWall(next)) {
      return false;
    }

    entity.vx = vector.x;
    entity.vy = vector.y;
    entity.direction = direction;
    return true;
  }

  moveWithVelocity(entity, delta) {
    if (entity.vx === 0 && entity.vy === 0) return;

    const next = {
      ...entity,
      x: entity.x + entity.vx * delta,
      y: entity.y + entity.vy * delta
    };

    if (this.hitsWall(next)) {
      entity.x = Math.round(entity.x / this.tile) * this.tile;
      entity.y = Math.round(entity.y / this.tile) * this.tile;
      entity.vx = 0;
      entity.vy = 0;
      return;
    }

    entity.x = next.x;
    entity.y = next.y;
  }

  hitsWall(entity) {
    const margin = 6;
    const left = Math.floor((entity.x + margin) / this.tile);
    const right = Math.floor((entity.x + entity.width - margin - 1) / this.tile);
    const top = Math.floor((entity.y + margin) / this.tile);
    const bottom = Math.floor((entity.y + entity.height - margin - 1) / this.tile);

    return (
      !this.isWalkable(top, left) ||
      !this.isWalkable(top, right) ||
      !this.isWalkable(bottom, left) ||
      !this.isWalkable(bottom, right)
    );
  }

  isAligned(entity) {
    return Math.abs(entity.x % this.tile) < 1.5 && Math.abs(entity.y % this.tile) < 1.5;
  }

  vector(direction, speed) {
    if (direction === "up") return { x: 0, y: -speed };
    if (direction === "down") return { x: 0, y: speed };
    if (direction === "left") return { x: -speed, y: 0 };
    if (direction === "right") return { x: speed, y: 0 };
    return { x: 0, y: 0 };
  }

  directionFromDelta(dx, dy) {
    if (dx > 0) return "right";
    if (dx < 0) return "left";
    if (dy > 0) return "down";
    return "up";
  }

  animate(entity, delta) {
    if (entity.vx === 0 && entity.vy === 0 && entity.type === "human") return;
    entity.frameClock += delta;
    if (entity.frameClock > 0.16) {
      entity.frame = entity.frame === 0 ? 1 : 0;
      entity.frameClock = 0;
    }
  }

  checkPickup() {
    if (this.pickup.collected) return;
    const tile = this.tileAtEntity(this.human);
    if (tile.row === this.pickup.row && tile.col === this.pickup.col) {
      this.pickup.collected = true;
      this.abilityReady = true;
      this.status = "Ability ready";
      this.score += 10;
    }
  }

  activateAbility() {
    if (!this.abilityReady || this.abilityActive) return;
    this.abilityReady = false;
    this.abilityActive = true;
    this.abilityTimer = 6;
    this.status = "Phantoms blinded";
    this.ghosts.forEach((ghost) => {
      ghost.stunned = 1.2;
      ghost.vx = 0;
      ghost.vy = 0;
    });
    this.updateUi();
    this.draw();
  }

  checkCollisions() {
    for (const ghost of this.ghosts) {
      if (!this.overlaps(this.human, ghost)) continue;
      if (this.abilityActive) {
        this.score += 10;
        this.teleportGhost(ghost);
        this.status = "Phantom repelled";
      } else {
        this.gameOver = true;
        this.running = false;
        this.status = "Caught";
        this.ui.start.textContent = "Start";
      }
    }
  }

  overlaps(a, b) {
    const inset = 7;
    return (
      a.x + inset < b.x + b.width - inset &&
      a.x + a.width - inset > b.x + inset &&
      a.y + inset < b.y + b.height - inset &&
      a.y + a.height - inset > b.y + inset
    );
  }

  teleportGhost(ghost) {
    const options = [
      { row: 1, col: 17 },
      { row: 18, col: 1 },
      { row: 18, col: 17 },
      { row: 7, col: 9 }
    ];
    const pick = options[Math.floor(Math.random() * options.length)];
    ghost.x = pick.col * this.tile;
    ghost.y = pick.row * this.tile;
    ghost.vx = 0;
    ghost.vy = 0;
    ghost.stunned = 1;
  }

  teleportRound() {
    this.human.x = this.tile;
    this.human.y = this.tile;
    this.ghosts[0].x = 17 * this.tile;
    this.ghosts[0].y = this.tile;
    this.ghosts[1].x = this.tile;
    this.ghosts[1].y = 18 * this.tile;
    this.ghosts[2].x = 17 * this.tile;
    this.ghosts[2].y = 18 * this.tile;
  }

  tileAtEntity(entity) {
    return {
      row: Math.floor((entity.y + entity.height / 2) / this.tile),
      col: Math.floor((entity.x + entity.width / 2) / this.tile)
    };
  }

  isWalkable(row, col) {
    return Boolean(this.map[row] && this.map[row][col] && this.map[row][col] !== "X");
  }

  neighbors(tile) {
    return [
      { row: tile.row - 1, col: tile.col },
      { row: tile.row + 1, col: tile.col },
      { row: tile.row, col: tile.col - 1 },
      { row: tile.row, col: tile.col + 1 }
    ].filter((candidate) => this.isWalkable(candidate.row, candidate.col));
  }

  key(tile) {
    return `${tile.row},${tile.col}`;
  }

  findPath(start, goal) {
    const startKey = this.key(start);
    const goalKey = this.key(goal);
    const queue = [start];
    const seen = new Set([startKey]);
    const cameFrom = new Map();

    for (let index = 0; index < queue.length; index++) {
      const current = queue[index];
      const currentKey = this.key(current);
      if (currentKey === goalKey) break;

      for (const next of this.neighbors(current)) {
        const nextKey = this.key(next);
        if (seen.has(nextKey)) continue;
        seen.add(nextKey);
        cameFrom.set(nextKey, currentKey);
        queue.push(next);
      }
    }

    if (!seen.has(goalKey)) return null;

    const path = [];
    let current = goalKey;
    while (current) {
      const [row, col] = current.split(",").map(Number);
      path.push({ row, col });
      current = cameFrom.get(current);
    }
    return path.reverse();
  }

  draw() {
    const ctx = this.ctx;
    ctx.imageSmoothingEnabled = false;
    ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
    ctx.fillStyle = "#060708";
    ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);

    for (let row = 0; row < this.map.length; row++) {
      for (let col = 0; col < this.map[row].length; col++) {
        const code = this.map[row][col];
        const x = col * this.tile;
        const y = row * this.tile;
        if (code === "X") {
          ctx.fillStyle = "#111318";
          ctx.fillRect(x, y, this.tile, this.tile);
          ctx.strokeStyle = "#20252c";
          ctx.strokeRect(x + 0.5, y + 0.5, this.tile - 1, this.tile - 1);
        } else if (this.tileImages[code]) {
          ctx.drawImage(this.tileImages[code], x, y, this.tile, this.tile);
        }
      }
    }

    if (!this.pickup.collected) {
      ctx.drawImage(
        this.abilityImage,
        this.pickup.col * this.tile,
        this.pickup.row * this.tile,
        this.tile,
        this.tile
      );
    }

    this.ghosts.forEach((ghost) => this.drawGhost(ghost));
    this.drawHuman();

    if (this.abilityActive) {
      ctx.fillStyle = "rgba(140, 199, 189, 0.16)";
      ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
    }

    if (!this.running || this.gameOver) {
      ctx.fillStyle = "rgba(0, 0, 0, 0.56)";
      ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
      ctx.fillStyle = "#f4f2ed";
      ctx.textAlign = "center";
      ctx.font = "700 34px system-ui, sans-serif";
      ctx.fillText(this.gameOver ? "Caught by a phantom" : "PhantomHunt demo", this.canvas.width / 2, 288);
      ctx.font = "18px system-ui, sans-serif";
      ctx.fillText("Press Start, then use WASD or arrow keys", this.canvas.width / 2, 326);
    }
  }

  drawMessage(title, subtitle = "") {
    const ctx = this.ctx;
    ctx.imageSmoothingEnabled = false;
    ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
    ctx.fillStyle = "#090a0c";
    ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);

    ctx.strokeStyle = "rgba(255, 255, 255, 0.06)";
    for (let x = 0; x <= this.canvas.width; x += this.tile) {
      ctx.beginPath();
      ctx.moveTo(x + 0.5, 0);
      ctx.lineTo(x + 0.5, this.canvas.height);
      ctx.stroke();
    }
    for (let y = 0; y <= this.canvas.height; y += this.tile) {
      ctx.beginPath();
      ctx.moveTo(0, y + 0.5);
      ctx.lineTo(this.canvas.width, y + 0.5);
      ctx.stroke();
    }

    ctx.fillStyle = "#f4f2ed";
    ctx.textAlign = "center";
    ctx.font = "700 28px system-ui, sans-serif";
    ctx.fillText(title, this.canvas.width / 2, this.canvas.height / 2 - 10);
    if (subtitle) {
      ctx.fillStyle = "#b9b4aa";
      ctx.font = "16px system-ui, sans-serif";
      ctx.fillText(subtitle, this.canvas.width / 2, this.canvas.height / 2 + 24);
    }
  }

  drawHuman() {
    const dir = this.human.direction === "up" ? "back" : this.human.direction === "down" ? "front" : this.human.direction;
    const img = this.humanImages[`${dir}${this.human.frame + 1}`] || this.humanImages.front1;
    this.ctx.drawImage(img, this.human.x, this.human.y, this.tile, this.tile);
  }

  drawGhost(ghost) {
    const key = `${ghost.variant}_${ghost.direction}`;
    const img = this.abilityActive ? this.glitchImages[key] : this.ghostImages[key];
    this.ctx.drawImage(img || Object.values(this.ghostImages)[0], ghost.x, ghost.y, this.tile, this.tile);
  }

  updateUi() {
    this.ui.time.textContent = `${Math.floor(this.elapsed)}s`;
    this.ui.score.textContent = `${Math.floor(this.score)}`;
    this.ui.round.textContent = `${this.round} / 4`;
    this.ui.status.textContent = this.status;
    this.ui.ability.disabled = !this.abilityReady || this.abilityActive;
    this.ui.ability.textContent = this.abilityActive
      ? `${Math.ceil(this.abilityTimer)}s`
      : this.abilityReady
        ? "Wisdom Blessing"
        : "Find ability";
  }
}

document.addEventListener("DOMContentLoaded", () => {
  setupWorkShowcase();
  const canvas = document.querySelector("#realGame");
  if (!canvas) return;

  const demo = new PhantomHuntDemo(canvas);
  demo.init();
});
