import { api } from "./api";

export const medicationsService = {
  async listToday() {
    const { data } = await api.get("/api/medicamentos/hoje");
    return Array.isArray(data) ? data : [];
  },

  async create({ nomeRemedio, dosagem, horario }) {
    const { data } = await api.post("/api/medicamentos", {
      nomeRemedio,
      dosagem,
      horario,
    });
    return data;
  },

  async confirmIntake(medicamentoId) {
    const { data } = await api.post(`/api/medicamentos/${medicamentoId}/confirmar`);
    return data;
  },

  async remove(medicamentoId) {
    await api.delete(`/api/medicamentos/${medicamentoId}`);
  },
};