const TILE_SIZE = 256;
const MAX_MERCATOR_LATITUDE = 85.05112878;

function normalizeCoordinate(value) {
  if (
    !value ||
    value.latitude === null ||
    value.latitude === undefined ||
    value.longitude === null ||
    value.longitude === undefined
  ) {
    return null;
  }

  const latitude = Number(value.latitude);
  const longitude = Number(value.longitude);

  if (
    !Number.isFinite(latitude) ||
    !Number.isFinite(longitude) ||
    latitude < -90 ||
    latitude > 90 ||
    longitude < -180 ||
    longitude > 180
  ) {
    return null;
  }

  return { latitude, longitude };
}

function projectCoordinate(coordinate, zoom) {
  const scale = TILE_SIZE * 2 ** zoom;
  const latitude = Math.max(
    -MAX_MERCATOR_LATITUDE,
    Math.min(MAX_MERCATOR_LATITUDE, coordinate.latitude),
  );
  const sinLatitude = Math.sin((latitude * Math.PI) / 180);

  return {
    x: ((coordinate.longitude + 180) / 360) * scale,
    y:
      (0.5 -
        Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI)) *
      scale,
  };
}

function unprojectCoordinate(point, zoom) {
  const scale = TILE_SIZE * 2 ** zoom;
  const longitude = (point.x / scale) * 360 - 180;
  const mercatorY = Math.PI * (1 - (2 * point.y) / scale);
  const latitude =
    (Math.atan(Math.sinh(mercatorY)) * 180) / Math.PI;

  return { latitude, longitude };
}

function calculateViewport(points, userCoords, width, height) {
  const coordinates = points
    .map((point) => normalizeCoordinate(point))
    .filter(Boolean);
  const userCoordinate = normalizeCoordinate(userCoords);

  if (userCoordinate) coordinates.push(userCoordinate);
  const hasCoordinates = coordinates.length > 0;
  if (coordinates.length === 0) {
    coordinates.push({ latitude: -23.5505, longitude: -46.6333 });
  }

  const availableWidth = Math.max(40, width - 64);
  const availableHeight = Math.max(40, height - 64);
  let zoom = 12;

  if (hasCoordinates && width > 0 && height > 0) {
    for (zoom = 17; zoom > 1; zoom -= 1) {
      const projected = coordinates.map((coordinate) =>
        projectCoordinate(coordinate, zoom),
      );
      const xValues = projected.map((point) => point.x);
      const yValues = projected.map((point) => point.y);
      const spanX = Math.max(...xValues) - Math.min(...xValues);
      const spanY = Math.max(...yValues) - Math.min(...yValues);

      if (spanX <= availableWidth && spanY <= availableHeight) break;
    }
  }

  const projected = coordinates.map((coordinate) =>
    projectCoordinate(coordinate, zoom),
  );
  const xValues = projected.map((point) => point.x);
  const yValues = projected.map((point) => point.y);
  const center = unprojectCoordinate(
    {
      x: (Math.min(...xValues) + Math.max(...xValues)) / 2,
      y: (Math.min(...yValues) + Math.max(...yValues)) / 2,
    },
    zoom,
  );

  return { center, zoom };
}

function getVisibleTiles(center, zoom, width, height) {
  if (width <= 0 || height <= 0) return [];

  const centerPoint = projectCoordinate(center, zoom);
  const left = centerPoint.x - width / 2;
  const top = centerPoint.y - height / 2;
  const tileCount = 2 ** zoom;
  const firstTileX = Math.floor(left / TILE_SIZE);
  const lastTileX = Math.floor((left + width) / TILE_SIZE);
  const firstTileY = Math.max(0, Math.floor(top / TILE_SIZE));
  const lastTileY = Math.min(
    tileCount - 1,
    Math.floor((top + height) / TILE_SIZE),
  );
  const tiles = [];

  for (let tileX = firstTileX; tileX <= lastTileX; tileX += 1) {
    const wrappedX = ((tileX % tileCount) + tileCount) % tileCount;

    for (let tileY = firstTileY; tileY <= lastTileY; tileY += 1) {
      tiles.push({
        key: `${zoom}/${tileX}/${tileY}`,
        url: `https://tile.openstreetmap.org/${zoom}/${wrappedX}/${tileY}.png`,
        left: tileX * TILE_SIZE - left,
        top: tileY * TILE_SIZE - top,
      });
    }
  }

  return tiles;
}

function getPointPosition(coordinate, center, zoom, width, height) {
  const point = projectCoordinate(coordinate, zoom);
  const centerPoint = projectCoordinate(center, zoom);

  return {
    x: point.x - centerPoint.x + width / 2,
    y: point.y - centerPoint.y + height / 2,
  };
}

module.exports = {
  calculateViewport,
  getPointPosition,
  getVisibleTiles,
  normalizeCoordinate,
};
