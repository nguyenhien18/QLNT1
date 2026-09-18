const API_BASE = (import.meta.env.VITE_API_BASE || "http://localhost:8087").replace(/\/$/, "");
const TOKEN_KEY = "QLPT_AUTH_TOKEN";
const ROLE_KEY = "QLPT_AUTH_ROLE";

export const authStore = {
  get token() { return localStorage.getItem(TOKEN_KEY); },
  get role() { return localStorage.getItem(ROLE_KEY); },
  save({ token, role }) {
    if (token) localStorage.setItem(TOKEN_KEY, token);
    if (role) localStorage.setItem(ROLE_KEY, role);
  },
  clear() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(ROLE_KEY);
  },
};

export async function api(path, options = {}) {
  const { body, headers, auth = true, ...rest } = options;
  const token = authStore.token;
  const response = await fetch(`${API_BASE}${path}`, {
    credentials: "include",
    ...rest,
    headers: {
      ...(body ? { "Content-Type": "application/json" } : {}),
      ...(auth && token ? { Authorization: `Bearer ${token}` } : {}),
      ...headers,
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  const contentType = response.headers.get("content-type") || "";
  const payload = contentType.includes("application/json") ? await response.json() : null;
  if (!response.ok) {
    const error = new Error(payload?.message || `Yêu cầu thất bại (HTTP ${response.status})`);
    error.status = response.status;
    throw error;
  }
  return payload && Object.hasOwn(payload, "result") ? payload.result : payload;
}

export async function login(username, password) {
  let lastError;
  for (const path of ["/api/auth/login/admin", "/api/auth/login/user"]) {
    try {
      const result = await api(path, { method: "POST", body: { username, password }, auth: false });
      authStore.save(result);
      return result;
    } catch (error) {
      lastError = error;
      if (error.status !== 401 && error.status !== 403) throw error;
    }
  }
  throw lastError || new Error("Không thể đăng nhập");
}

export async function logout() {
  try {
    await api("/api/auth/logout", { method: "POST", auth: false });
  } finally {
    authStore.clear();
  }
}

export function pageContent(value) {
  return Array.isArray(value) ? value : value?.content || [];
}
