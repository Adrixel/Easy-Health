import React, { useState } from "react";
import {
  ActivityIndicator,
  Linking,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { colors } from "@/theme/colors";
import { fonts, radius } from "@/theme/typography";
import { Pill } from "@/components/Pill";
import { historyShareService } from "@/services/historyShareService";
import { getApiErrorMessage } from "@/utils/apiError";
import { formatDateShort } from "@/utils/date";

const RECORD_FILTERS = [
  { value: null, label: "Todos" },
  { value: "exame", label: "Exames" },
  { value: "vacina", label: "Vacinas" },
  { value: "medicamento", label: "Medicamentos" },
  { value: "documento", label: "Documentos" },
];

export function DoctorPatientHistorySection() {
  const [pacienteId, setPacienteId] = useState("");
  const [selectedTipo, setSelectedTipo] = useState(null);
  const [records, setRecords] = useState([]);
  const [searched, setSearched] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState(null);

  async function fetchRecords(tipoOverride = selectedTipo) {
    const cleanId = pacienteId.trim();
    if (!cleanId) {
      setErrorMsg("Informe o ID do paciente para consultar.");
      return;
    }

    setLoading(true);
    setErrorMsg(null);
    setSearched(true);

    try {
      const list = await historyShareService.getPatientRecordsAsDoctor(
        cleanId,
        tipoOverride
      );
      setRecords(list);
    } catch (error) {
      setRecords([]);
      const status = error?.response?.status;
      if (status === 403) {
        setErrorMsg(
          error?.response?.data?.message ||
            "Você não possui autorização ativa para acessar este histórico."
        );
      } else if (status === 404) {
        setErrorMsg("Paciente não encontrado. Verifique o ID informado.");
      } else {
        setErrorMsg(
          getApiErrorMessage(
            error,
            "Não foi possível consultar o histórico do paciente."
          )
        );
      }
    } finally {
      setLoading(false);
    }
  }

  function handleSelectFilter(tipo) {
    setSelectedTipo(tipo);
    if (pacienteId.trim() && searched) {
      fetchRecords(tipo);
    }
  }

  const patientName = records[0]?.paciente;

  return (
    <View style={styles.container}>
      <Text style={styles.sectionTitle}>Consultar Histórico de Paciente</Text>
      <Text style={styles.subtitle}>
        Digite o identificador do paciente que autorizou o acesso aos registros
        clínicos.
      </Text>

      <View style={styles.searchRow}>
        <TextInput
          style={styles.input}
          placeholder="Ex.: demo-user"
          placeholderTextColor={colors.inkFaint}
          autoCapitalize="none"
          value={pacienteId}
          onChangeText={setPacienteId}
        />
        <Pressable
          onPress={() => fetchRecords(selectedTipo)}
          disabled={loading}
          style={[styles.searchBtn, loading && { opacity: 0.6 }]}
        >
          <Ionicons name="search-outline" size={18} color={colors.white} />
          <Text style={styles.searchBtnText}>Buscar</Text>
        </Pressable>
      </View>

      {/* Filtros por tipo de registro */}
      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={styles.filterRow}
      >
        {RECORD_FILTERS.map((f) => {
          const active = selectedTipo === f.value;
          return (
            <Pressable
              key={f.label}
              onPress={() => handleSelectFilter(f.value)}
              style={[styles.filterChip, active && styles.filterChipActive]}
            >
              <Text
                style={[
                  styles.filterChipText,
                  active && styles.filterChipTextActive,
                ]}
              >
                {f.label}
              </Text>
            </Pressable>
          );
        })}
      </ScrollView>

      {loading ? (
        <ActivityIndicator
          color={colors.primary}
          style={{ marginVertical: 16 }}
        />
      ) : errorMsg ? (
        <View style={styles.errorBox}>
          <Ionicons
            name="alert-circle-outline"
            size={18}
            color={colors.alertDark}
          />
          <Text style={styles.errorText}>{errorMsg}</Text>
        </View>
      ) : searched && records.length === 0 ? (
        <Text style={styles.emptyText}>
          Nenhum registro encontrado para este filtro.
        </Text>
      ) : (
        <>
          {patientName && (
            <View style={styles.patientBanner}>
              <Ionicons
                name="person-circle-outline"
                size={18}
                color={colors.primaryDark}
              />
              <Text style={styles.patientBannerText}>
                Paciente: {patientName}
              </Text>
            </View>
          )}

          {records.map((item, idx) => {
            const place = item.estabelecimento || item.local;
            const doctor = item.medico || item.profissional;
            return (
              <View key={item.id || idx} style={styles.recordCard}>
                <View style={styles.rowBetween}>
                  <Text style={styles.recordDate}>
                    {item.data ? formatDateShort(item.data) : ""}
                  </Text>
                  {item.tipo && <Pill>{item.tipo.toUpperCase()}</Pill>}
                </View>

                <Text style={styles.recordTitle}>{item.titulo}</Text>

                {(doctor || place) && (
                  <Text style={styles.recordMeta}>
                    {[doctor, place].filter(Boolean).join(" • ")}
                  </Text>
                )}

                {item.descricao ? (
                  <Text style={styles.recordDesc}>{item.descricao}</Text>
                ) : null}

                {item.resultado ? (
                  <View style={styles.resultBox}>
                    <Text style={styles.resultLabel}>Resultado:</Text>
                    <Text style={styles.resultText}>{item.resultado}</Text>
                  </View>
                ) : null}

                {item.laudoUrl ? (
                  <Pressable
                    onPress={() => Linking.openURL(item.laudoUrl)}
                    style={styles.laudoBtn}
                  >
                    <Ionicons
                      name="document-text-outline"
                      size={15}
                      color={colors.primaryDark}
                    />
                    <Text style={styles.laudoBtnText}>Abrir Laudo / Anexo</Text>
                  </Pressable>
                ) : null}
              </View>
            );
          })}
        </>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: colors.panel,
    borderRadius: radius.lg,
    padding: 16,
    marginBottom: 20,
  },
  sectionTitle: {
    fontFamily: fonts.bold,
    fontSize: 15.5,
    color: colors.ink,
    marginBottom: 4,
  },
  subtitle: {
    fontFamily: fonts.regular,
    fontSize: 12.5,
    color: colors.inkSoft,
    lineHeight: 18,
    marginBottom: 12,
  },
  searchRow: {
    flexDirection: "row",
    gap: 8,
    marginBottom: 10,
  },
  input: {
    flex: 1,
    backgroundColor: colors.bg,
    borderWidth: 1,
    borderColor: colors.line,
    borderRadius: radius.md,
    paddingHorizontal: 12,
    paddingVertical: 10,
    fontFamily: fonts.regular,
    fontSize: 13.5,
    color: colors.ink,
  },
  searchBtn: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: colors.primary,
    borderRadius: radius.md,
    paddingHorizontal: 14,
    gap: 6,
  },
  searchBtnText: {
    fontFamily: fonts.semiBold,
    fontSize: 13.5,
    color: colors.white,
  },
  filterRow: {
    gap: 6,
    paddingBottom: 10,
  },
  filterChip: {
    paddingHorizontal: 11,
    paddingVertical: 6,
    borderRadius: radius.pill,
    backgroundColor: colors.bg,
    borderWidth: 1,
    borderColor: colors.line,
  },
  filterChipActive: {
    backgroundColor: colors.primarySoft,
    borderColor: colors.primary,
  },
  filterChipText: {
    fontFamily: fonts.semiBold,
    fontSize: 12,
    color: colors.inkSoft,
  },
  filterChipTextActive: {
    color: colors.primaryDark,
  },
  patientBanner: {
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
    backgroundColor: colors.primarySoft,
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: radius.md,
    marginTop: 4,
    marginBottom: 4,
  },
  patientBannerText: {
    fontFamily: fonts.semiBold,
    fontSize: 13,
    color: colors.primaryDark,
  },
  errorBox: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
    backgroundColor: colors.alertSoft,
    padding: 12,
    borderRadius: radius.md,
  },
  errorText: {
    flex: 1,
    fontFamily: fonts.regular,
    fontSize: 12.5,
    color: colors.alertDark,
    lineHeight: 18,
  },
  emptyText: {
    fontFamily: fonts.regular,
    fontSize: 13,
    color: colors.inkSoft,
    textAlign: "center",
    paddingVertical: 10,
  },
  recordCard: {
    backgroundColor: colors.bg,
    borderRadius: radius.md,
    padding: 12,
    marginTop: 8,
    borderWidth: 1,
    borderColor: colors.line,
  },
  rowBetween: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  recordDate: {
    fontFamily: fonts.regular,
    fontSize: 11.5,
    color: colors.inkSoft,
  },
  recordTitle: {
    fontFamily: fonts.bold,
    fontSize: 14,
    color: colors.ink,
    marginTop: 4,
  },
  recordMeta: {
    fontFamily: fonts.regular,
    fontSize: 12,
    color: colors.inkSoft,
    marginTop: 2,
  },
  recordDesc: {
    fontFamily: fonts.regular,
    fontSize: 12.5,
    color: colors.ink,
    lineHeight: 18,
    marginTop: 6,
  },
  resultBox: {
    marginTop: 8,
    backgroundColor: colors.panel,
    padding: 8,
    borderRadius: 8,
  },
  resultLabel: {
    fontFamily: fonts.semiBold,
    fontSize: 11.5,
    color: colors.inkSoft,
  },
  resultText: {
    fontFamily: fonts.regular,
    fontSize: 12.5,
    color: colors.ink,
    marginTop: 2,
  },
  laudoBtn: {
    marginTop: 8,
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
    alignSelf: "flex-start",
    backgroundColor: colors.primarySoft,
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 8,
  },
  laudoBtnText: {
    fontFamily: fonts.semiBold,
    fontSize: 12,
    color: colors.primaryDark,
  },
});