/**
 * @jest-environment jsdom
 */

const game = require('./pacman');

describe('Essential Game Logic', () => {

  test('Manhattan Distance (Math Check)', () => {
    // If the function exists, test it. If not, fail with a clear message.
    if (!game.manhattan) throw new Error("Could not find 'manhattan' function in pacman.js");

    const distance = game.manhattan({ r: 0, c: 0 }, { r: 3, c: 4 });
    // |0-3| + |0-4| = 7
    expect(distance).toBe(7);
  });

  test('Collision Detection (Logic Check)', () => {
    if (!game.checkCollision) throw new Error("Could not find 'checkCollision' function in pacman.js");

    const player = { x: 100, y: 100, width: 32, height: 32 };
    const enemy = { x: 105, y: 105, width: 32, height: 32 }; // Slight overlap
    
    expect(game.checkCollision(player, enemy)).toBe(true);
  });

  test('Safe Movement Check', () => {
    // This checks if the game knows what a "valid move" looks like
    if (!game.getValidMoves) throw new Error("Could not find 'getValidMoves' function");
    
    // Create a fake entity to test
    const ghost = { x: 50, y: 50, width: 32, height: 32, velocityX: 0, velocityY: 0 };
    
    // We expect this to run without crashing
    const moves = game.getValidMoves(ghost);
    expect(Array.isArray(moves)).toBe(true);
  });

  
});
