import { api } from "./api";

export const mentalHealthService = {
  async list() {
    const { data } = await api.get("/api/saude-mental");
    return Array.isArray(data) ? data : [];
  },
};