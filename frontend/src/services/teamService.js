import { api } from "./api";
import { authService } from "./authService";

async function getAuthConfig() {
  const token =
    (await authService.getAccessToken?.()) ||
    (await authService.getToken?.()) ||
    null;
  return token ? { params: { accessToken: token } } : {};
}

export const teamService = {
  async getPainel() {
    const config = await getAuthConfig();
    const { data } = await api.get("/api/equipe/painel", config);
    return data;
  },

  async getAdministracao() {
    const config = await getAuthConfig();
    const { data } = await api.get("/api/equipe/administracao", config);
    return data;
  },

  async listMinhasClinicas() {
    const config = await getAuthConfig();
    const { data } = await api.get("/api/equipe/minhas-clinicas", config);
    return Array.isArray(data) ? data : [];
  },

  async createClinic(payload) {
    const config = await getAuthConfig();
    const { data } = await api.post("/api/estabelecimentos", payload, config);
    return data;
  },

  // --- Preços da clínica (CatalogController + EstablishmentAdminController) ---
  async listClinicPrices(clinicId) {
    const { data } = await api.get(`/api/estabelecimentos/${clinicId}/precos`);
    return Array.isArray(data) ? data : [];
  },

  async updateClinicPrice(clinicId, { servico, valor }) {
    const config = await getAuthConfig();
    const { data } = await api.put(
      `/api/estabelecimentos/${clinicId}/precos`,
      { servico: servico.trim(), valor: valor.trim() },
      config
    );
    return Array.isArray(data) ? data : [];
  },

  // --- Equipe da clínica (EstablishmentAdminController - somente DONO) ---
  async listClinicTeam(clinicId) {
    const config = await getAuthConfig();
    const { data } = await api.get(
      `/api/estabelecimentos/${clinicId}/equipe`,
      config
    );
    return Array.isArray(data) ? data : [];
  },

  async addClinicTeamMember(clinicId, { email, cargo }) {
    const config = await getAuthConfig();
    const { data } = await api.post(
      `/api/estabelecimentos/${clinicId}/equipe`,
      { email: email.trim(), cargo },
      config
    );
    return data;
  },

  async removeClinicTeamMember(clinicId, userId) {
    const config = await getAuthConfig();
    await api.delete(
      `/api/estabelecimentos/${clinicId}/equipe/${userId}`,
      config
    );
  },
};