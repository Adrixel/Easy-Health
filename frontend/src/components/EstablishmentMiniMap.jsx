import React, { useMemo, useState } from "react";
import { Image, Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { colors } from "@/theme/colors";
import { fonts, radius } from "@/theme/typography";
import {
  calculateViewport,
  getPointPosition,
  getVisibleTiles,
  normalizeCoordinate,
} from "@/utils/mapProjection";

const MAP_HEIGHT = 160;
const TILE_SIZE = 256;
const MIN_ZOOM = 2;
const MAX_ZOOM = 19;
const EMPTY_SET = new Set();

export function EstablishmentMiniMap({
  establishments,
  userCoords,
  onSelect,
}) {
  const [size, setSize] = useState({ width: 0, height: MAP_HEIGHT });
  const [zoomState, setZoomState] = useState({ key: null, offset: 0 });
  const [tileFailures, setTileFailures] = useState({
    signature: "",
    keys: EMPTY_SET,
  });

  const mapPoints = useMemo(
    () =>
      establishments.flatMap((item, index) => {
        const coordinate = normalizeCoordinate(item);
        return coordinate ? [{ item, index, coordinate }] : [];
      }),
    [establishments],
  );
  const validUserCoords = normalizeCoordinate(userCoords);
  const pointsKey = mapPoints
    .map(({ item, coordinate }) => `${item.id}:${coordinate.latitude},${coordinate.longitude}`)
    .join("|");
  const mapViewKey = JSON.stringify([
    pointsKey,
    size.width,
    size.height,
    validUserCoords?.latitude,
    validUserCoords?.longitude,
  ]);
  const zoomOffset =
    zoomState.key === mapViewKey ? zoomState.offset : 0;
  const viewport = calculateViewport(
    mapPoints.map(({ coordinate }) => coordinate),
    validUserCoords,
    size.width,
    size.height,
  );
  const zoom = Math.max(
    MIN_ZOOM,
    Math.min(MAX_ZOOM, viewport.zoom + zoomOffset),
  );
  const tiles = getVisibleTiles(
    viewport.center,
    zoom,
    size.width,
    size.height,
  );
  const tileSignature = tiles.map((tile) => tile.key).join("|");
  const failedTiles =
    tileFailures.signature === tileSignature
      ? tileFailures.keys
      : EMPTY_SET;
  const allTilesFailed = tiles.length > 0 && failedTiles.size >= tiles.length;
  const hasResultsWithoutCoordinates =
    establishments.length > 0 && mapPoints.length === 0;

  function updateSize(event) {
    const { width, height } = event.nativeEvent.layout;
    setSize((current) =>
      current.width === width && current.height === height
        ? current
        : { width, height },
    );
  }

  function zoomBy(amount) {
    setZoomState((current) => {
      const currentOffset =
        current.key === mapViewKey ? current.offset : 0;
      return {
        key: mapViewKey,
        offset: Math.max(
          MIN_ZOOM - viewport.zoom,
          Math.min(MAX_ZOOM - viewport.zoom, currentOffset + amount),
        ),
      };
    });
  }

  function markTileFailed(tileKey) {
    setTileFailures((current) => {
      const keys =
        current.signature === tileSignature ? current.keys : EMPTY_SET;
      return {
        signature: tileSignature,
        keys: new Set(keys).add(tileKey),
      };
    });
  }

  return (
    <View
      onLayout={updateSize}
      style={{
        height: MAP_HEIGHT,
        borderRadius: radius.lg,
        backgroundColor: "#E6EEE9",
        marginBottom: 20,
        overflow: "hidden",
      }}
    >
      {tiles.map((tile) => (
        <Image
          key={tile.key}
          source={{ uri: tile.url }}
          resizeMode="stretch"
          onError={() => markTileFailed(tile.key)}
          style={{
            position: "absolute",
            left: tile.left,
            top: tile.top,
            width: TILE_SIZE,
            height: TILE_SIZE,
          }}
        />
      ))}

      {mapPoints.map(({ item, index, coordinate }) => {
        const position = getPointPosition(
          coordinate,
          viewport.center,
          zoom,
          size.width,
          size.height,
        );

        if (
          position.x < -24 ||
          position.x > size.width + 24 ||
          position.y < -24 ||
          position.y > size.height + 24
        ) {
          return null;
        }

        return (
          <Pressable
            key={item.id}
            accessibilityRole="button"
            accessibilityLabel={`${index + 1}. ${item.nome}`}
            onPress={() => onSelect(item)}
            style={{
              position: "absolute",
              left: position.x - 15,
              top: position.y - 15,
              width: 30,
              height: 30,
              borderRadius: 15,
              borderWidth: 2,
              borderColor: colors.white,
              backgroundColor: colors.primary,
              alignItems: "center",
              justifyContent: "center",
              elevation: 3,
              shadowColor: colors.black,
              shadowOpacity: 0.25,
              shadowRadius: 3,
            }}
          >
            <Text
              style={{
                color: colors.white,
                fontFamily: fonts.bold,
                fontSize: 11,
              }}
            >
              {index + 1}
            </Text>
          </Pressable>
        );
      })}

      {validUserCoords && (
        <View
          accessibilityLabel="Sua localização"
          style={{
            position: "absolute",
            left:
              getPointPosition(
                validUserCoords,
                viewport.center,
                zoom,
                size.width,
                size.height,
              ).x - 7,
            top:
              getPointPosition(
                validUserCoords,
                viewport.center,
                zoom,
                size.width,
                size.height,
              ).y - 7,
            width: 14,
            height: 14,
            borderRadius: 7,
            borderWidth: 3,
            borderColor: colors.white,
            backgroundColor: "#2563EB",
            elevation: 4,
          }}
        />
      )}

      <View
        style={{
          position: "absolute",
          right: 8,
          top: 8,
          gap: 6,
        }}
      >
        {[
          { icon: "add", label: "Aumentar zoom", amount: 1 },
          { icon: "remove", label: "Diminuir zoom", amount: -1 },
        ].map((control) => (
          <Pressable
            key={control.icon}
            accessibilityRole="button"
            accessibilityLabel={control.label}
            onPress={() => zoomBy(control.amount)}
            style={{
              width: 32,
              height: 32,
              borderRadius: 8,
              backgroundColor: "rgba(255,255,255,0.94)",
              alignItems: "center",
              justifyContent: "center",
            }}
          >
            <Ionicons name={control.icon} size={19} color={colors.ink} />
          </Pressable>
        ))}
      </View>

      {hasResultsWithoutCoordinates && (
        <View
          style={{
            position: "absolute",
            alignSelf: "center",
            top: "38%",
            paddingHorizontal: 10,
            paddingVertical: 6,
            borderRadius: radius.sm,
            backgroundColor: "rgba(255,255,255,0.92)",
          }}
        >
          <Text
            style={{
              color: colors.inkSoft,
              fontFamily: fonts.medium,
              fontSize: 11,
            }}
          >
            Sem coordenadas para exibir os pontos
          </Text>
        </View>
      )}

      {allTilesFailed && (
        <View
          style={{
            position: "absolute",
            alignSelf: "center",
            top: "38%",
            paddingHorizontal: 10,
            paddingVertical: 6,
            borderRadius: radius.sm,
            backgroundColor: "rgba(255,255,255,0.94)",
          }}
        >
          <Text
            style={{
              color: colors.inkSoft,
              fontFamily: fonts.medium,
              fontSize: 11,
            }}
          >
            Não foi possível carregar o mapa
          </Text>
        </View>
      )}

      <Text
        accessibilityLabel="Dados do mapa: OpenStreetMap"
        style={{
          position: "absolute",
          left: 5,
          bottom: 3,
          paddingHorizontal: 4,
          paddingVertical: 2,
          borderRadius: 3,
          backgroundColor: "rgba(255,255,255,0.85)",
          color: "#374151",
          fontSize: 9,
        }}
      >
        © OpenStreetMap contributors
      </Text>
    </View>
  );
}
