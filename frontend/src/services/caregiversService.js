import { api } from "./api";

export const caregiversService = {
  async getSearchLink(cidade = "") {
    const params = cidade.trim() ? { cidade: cidade.trim() } : {};
    const { data } = await api.get("/api/cuidadoras/link-busca", { params });
    return data?.url || "https://www.linkedin.com/search/results/people/?keywords=cuidadora%20de%20idosos";
  },
};