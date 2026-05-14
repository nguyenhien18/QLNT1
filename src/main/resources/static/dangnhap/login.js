const API_BASE = window.API_BASE || "http://localhost:8087";
const AUTH_TOKEN_KEY = "QLPT_AUTH_TOKEN";
const AUTH_ROLE_KEY = "QLPT_AUTH_ROLE";
const API_BASE_KEY = "QLPT_API_BASE";
const FRONTEND_BUST_VERSION = "20260429authfix6";
const LEGACY_AUTH_KEYS = ["authToken", "currentUser", "role", "logged_in_user"];

const form = document.getElementById("loginForm");
const err = document.getElementById("loginError");

function showError(message) {
  if (!err) return;
  err.style.display = "block";
  err.textContent = message;
}

function clearError() {
  if (!err) return;
  err.style.display = "none";
  err.textContent = "";
}

function saveAuthFromResult(result) {
  if (!result || typeof result !== "object") {
    localStorage.removeItem(AUTH_TOKEN_KEY);
    localStorage.removeItem(AUTH_ROLE_KEY);
    LEGACY_AUTH_KEYS.forEach((key) => localStorage.removeItem(key));
    return;
  }

  if (result.token) {
    localStorage.setItem(AUTH_TOKEN_KEY, result.token);
  } else {
    localStorage.removeItem(AUTH_TOKEN_KEY);
  }

  if (result.role) {
    localStorage.setItem(AUTH_ROLE_KEY, result.role);
  } else {
    localStorage.removeItem(AUTH_ROLE_KEY);
  }
  LEGACY_AUTH_KEYS.forEach((key) => localStorage.removeItem(key));
}

function normalizeBase(base) {
  if (!base || typeof base !== "string") return "";
  return base.trim().replace(/\/+$/, "");
}

function buildCandidateApiBases() {
  const candidates = [];
  const add = (base) => {
    const normalized = normalizeBase(base);
    if (!normalized || candidates.includes(normalized)) return;
    candidates.push(normalized);
  };

  if (window.location.protocol.startsWith("http")) {
    add(window.location.origin);
  }

  const isLocal = ["localhost", "127.0.0.1"].includes(window.location.hostname);
  if (isLocal || window.location.protocol === "file:") {
    add("http://localhost:8087");
    add("http://127.0.0.1:8087");
  }

  return candidates;
}

async function tryLoginAtBase(base, path, username, password) {
  try {
    const response = await fetch(`${base}${path}`, {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password }),
    });

    let payload = null;
    try {
      payload = await response.json();
    } catch (_) {
      payload = null;
    }

    const result = payload && typeof payload === "object" && Object.prototype.hasOwnProperty.call(payload, "result")
      ? payload.result
      : payload;

    return {
      ok: response.ok,
      status: response.status,
      base,
      path,
      data: payload,
      result,
    };
  } catch (error) {
    return {
      ok: false,
      status: 0,
      base,
      path,
      data: null,
      result: null,
      error,
    };
  }
}

form?.addEventListener("submit", async (event) => {
  event.preventDefault();
  clearError();
  localStorage.removeItem(AUTH_TOKEN_KEY);
  localStorage.removeItem(AUTH_ROLE_KEY);
  LEGACY_AUTH_KEYS.forEach((key) => localStorage.removeItem(key));

  const username = document.getElementById("username")?.value.trim() || "";
  const password = document.getElementById("password")?.value || "";

  if (!username || !password) {
    showError("Vui long nhap day du tai khoan va mat khau.");
    return;
  }

  try {
    const attempts = [];
    const candidateBases = buildCandidateApiBases();

    for (const base of candidateBases) {
      const adminAttempt = await tryLoginAtBase(base, "/api/auth/login/admin", username, password);
      attempts.push(adminAttempt);
      if (adminAttempt.ok) {
        saveAuthFromResult(adminAttempt.result);
        localStorage.setItem(API_BASE_KEY, base);
        window.API_BASE = base;
        window.location.href = `${base}/admin/trangchu.html?v=${FRONTEND_BUST_VERSION}`;
        return;
      }

      const userAttempt = await tryLoginAtBase(base, "/api/auth/login/user", username, password);
      attempts.push(userAttempt);
      if (userAttempt.ok) {
        saveAuthFromResult(userAttempt.result);
        localStorage.setItem(API_BASE_KEY, base);
        window.API_BASE = base;
        window.location.href = `${base}/nguoithue/trangcanhan.html?v=${FRONTEND_BUST_VERSION}`;
        return;
      }
    }

    saveAuthFromResult(null);
    const anyNetworkError = attempts.some((attempt) => attempt.status === 0);
    const anyBackendResponse = attempts.some((attempt) => attempt.status > 0);

    if (!anyBackendResponse && anyNetworkError) {
      showError("Khong ket noi duoc backend. Hay kiem tra backend da chay chua (cong 8087).");
      return;
    }

    if (attempts.some((attempt) => attempt.status === 404)) {
      showError("Khong tim thay API dang nhap. Hay kiem tra dung cong backend (8087).");
      return;
    }

    const backendMessage = attempts
      .map((attempt) => attempt.data && attempt.data.message)
      .find((message) => typeof message === "string" && message.trim().length > 0);
    showError(backendMessage || "Sai tai khoan hoac mat khau!");
  } catch (error) {
    console.error("Loi dang nhap:", error);
    saveAuthFromResult(null);
    showError("Khong ket noi duoc backend!");
  }
});
