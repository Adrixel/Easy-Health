import { api } from "./api";

export const historyShareService = {
  async shareWithDoctor(medicoId) {
    const { data } = await api.post("/api/historico/compartilhamentos", {
      medicoId: medicoId.trim(),
    });
    return data;
  },

  async listShares() {
    const { data } = await api.get("/api/historico/compartilhamentos");
    return Array.isArray(data) ? data : [];
  },

  async revokeShare(compartilhamentoId) {
    await api.delete(`/api/historico/compartilhamentos/${compartilhamentoId}`);
  },

  async getPatientRecordsAsDoctor(pacienteId) {
    const { data } = await api.get(
      `/api/medicos/pacientes/${pacienteId}/historico/registros`
    );
    return Array.isArray(data) ? data : [];
  },
};