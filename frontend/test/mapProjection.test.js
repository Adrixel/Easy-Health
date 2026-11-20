const test = require("node:test");
const assert = require("node:assert/strict");
const {
  calculateViewport,
  getPointPosition,
  getVisibleTiles,
  normalizeCoordinate,
} = require("../src/utils/mapProjection");

test("normalizes valid coordinates and rejects missing or out-of-range values", () => {
  assert.deepEqual(normalizeCoordinate({ latitude: "0", longitude: "-46.6" }), {
    latitude: 0,
    longitude: -46.6,
  });
  assert.equal(normalizeCoordinate({ latitude: null, longitude: 0 }), null);
  assert.equal(normalizeCoordinate({ latitude: 91, longitude: 0 }), null);
  assert.equal(normalizeCoordinate({ latitude: 0, longitude: Infinity }), null);
});

test("fits establishment and user coordinates inside the mini-map viewport", () => {
  const coordinates = [
    { latitude: -23.55052, longitude: -46.633308 },
    { latitude: -23.561414, longitude: -46.655881 },
    { latitude: -23.515, longitude: -46.62 },
  ];
  const userCoords = { latitude: -23.54, longitude: -46.64 };
  const width = 320;
  const height = 160;
  const viewport = calculateViewport(
    coordinates,
    userCoords,
    width,
    height,
  );

  for (const coordinate of [...coordinates, userCoords]) {
    const position = getPointPosition(
      coordinate,
      viewport.center,
      viewport.zoom,
      width,
      height,
    );
    assert.ok(position.x >= 32 && position.x <= width - 32);
    assert.ok(position.y >= 32 && position.y <= height - 32);
  }
});

test("creates visible OpenStreetMap tiles and wraps tile columns at the date line", () => {
  const center = { latitude: 0, longitude: 179.99 };
  const tiles = getVisibleTiles(center, 3, 320, 160);

  assert.ok(tiles.length > 0);
  assert.ok(
    tiles.some((tile) =>
      /^https:\/\/tile\.openstreetmap\.org\/3\/[0-7]\/\d+\.png$/.test(tile.url),
    ),
  );
  assert.ok(tiles.every((tile) => tile.top < 160 && tile.top + 256 > 0));
});
