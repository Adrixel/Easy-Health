import React, { useEffect, useState } from "react";
import {
  ActivityIndicator,
  ScrollView,
  Text,
  TextInput,
  View,
} from "react-native";
import { AppAlert as Alert } from "@/services/appAlert";
import { Ionicons } from "@expo/vector-icons";
import { useRoute } from "@react-navigation/native";
import { colors } from "@/theme/colors";
import { fonts, radius, spacing } from "@/theme/typography";
import { PillTabs } from "@/components/PillTabs";
import { ClinicCard } from "@/components/ClinicCard";
import { EstablishmentMiniMap } from "@/components/EstablishmentMiniMap";
import { useLocation } from "@/hooks/useLocation";
import { useAppNavigation } from "@/navigation/useAppNavigation";
import { establishmentsService } from "@/services/establishmentsService";
import { emergencyService } from "@/services/emergencyService";
import { AdBanner } from "@/components/AdBanner";
const FILTERS = [
  {
    value: "todos",
    label: "Todos",
  },
  {
    value: "hospital",
    label: "Hospitais",
  },
  {
    value: "clinica",
    label: "Clínicas",
  },
  {
    value: "ubs",
    label: "UBS",
  },
  {
    value: "upa",
    label: "UPA",
  },
];
export function SearchScreen() {
  const navigation = useAppNavigation();
  const route = useRoute();
  const { coords } = useLocation();
  const [filtro, setFiltro] = useState("todos");
  const [query, setQuery] = useState(route.params?.query ?? "");
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(false);

  useEffect(() => {
    let active = true;

    setLoading(true);
    setLoadError(false);

    establishmentsService
      .search({
        latitude: coords?.latitude,
        longitude: coords?.longitude,
        tipo: filtro,
        query: query || undefined,
      })
      .then((data) => {
        if (!active) return;

        const orderedResults = [...data];

        // Ordena pela distância quando a localização está disponível.
        if (coords) {
          orderedResults.sort((a, b) => {
            const distanceA = Number(a.distanciaKm ?? Infinity);
            const distanceB = Number(b.distanciaKm ?? Infinity);

            return distanceA - distanceB;
          });
        }

        // Exibe somente os 10 primeiros resultados.
        setResults(orderedResults.slice(0, 10));
      })
      .catch((error) => {
        if (!active) return;

        console.error("Não foi possível carregar os estabelecimentos.", error);
        setResults([]);
        setLoadError(true);
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, [coords, filtro, query]);

  async function handleRoute(item) {
    try {
      await emergencyService.openRouteInMaps(
        {
          latitude: item.latitude,
          longitude: item.longitude,
        },
        item.nome,
      );
    } catch {
      Alert.alert(
        "Não foi possível abrir o mapa",
        "Tente novamente em instantes.",
      );
    }
  }
  return (
    <View style={{ flex: 1, backgroundColor: colors.bg }}>
      <ScrollView
        style={{ flex: 1 }}
        contentContainerStyle={{
          padding: spacing.lg,
          paddingTop: spacing.xl,
        }}
      >
      <Text
        style={{
          fontFamily: fonts.bold,
          fontSize: 19,
          color: colors.ink,
          marginBottom: 16,
        }}
      >
        Buscar Atendimento
      </Text>

      <View
        style={{
          flexDirection: "row",
          alignItems: "center",
          gap: 8,
          backgroundColor: colors.panel,
          borderRadius: radius.md,
          paddingHorizontal: 14,
          marginBottom: 12,
        }}
      >
        <Ionicons name="search" size={16} color={colors.inkFaint} />
        <TextInput
          value={query}
          onChangeText={setQuery}
          placeholder="Hospitais próximos"
          placeholderTextColor={colors.inkFaint}
          style={{
            flex: 1,
            paddingVertical: 13,
            fontFamily: fonts.regular,
            fontSize: 13,
            color: colors.ink,
          }}
        />
      </View>

      <View
        style={{
          marginBottom: 16,
        }}
      >
        <PillTabs options={FILTERS} value={filtro} onChange={setFiltro} />
      </View>

      <EstablishmentMiniMap
        establishments={results}
        userCoords={coords}
        onSelect={(item) =>
          navigation.navigate("EstablishmentDetail", { id: item.id })
        }
      />

      <Text
        style={{
          fontFamily: fonts.bold,
          fontSize: 14.5,
          color: colors.ink,
          marginBottom: 12,
        }}
      >
        Resultados Próximos
      </Text>

      {loading ? (
        <ActivityIndicator color={colors.primary} />
      ) : loadError ? (
        <Text
          style={{
            fontFamily: fonts.regular,
            fontSize: 12.5,
            color: colors.inkSoft,
          }}
        >
          Não foi possível carregar os estabelecimentos. Verifique a conexão e
          tente novamente.
        </Text>
      ) : results.length === 0 ? (
        <Text
          style={{
            fontFamily: fonts.regular,
            fontSize: 12.5,
            color: colors.inkSoft,
          }}
        >
          Nenhum estabelecimento encontrado por perto.
        </Text>
      ) : (
        <View
          style={{
            gap: 10,
          }}
        >
          {results.map((item) => (
            <ClinicCard
              key={item.id}
              item={item}
              onPress={() =>
                navigation.navigate("EstablishmentDetail", {
                  id: item.id,
                })
              }
              onPressRoute={() => handleRoute(item)}
            />
          ))}
        </View>
      )}
      </ScrollView>
      <AdBanner />
    </View>
  );
}
