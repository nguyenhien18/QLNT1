(function () {
  const AUTH_TOKEN_KEY = "QLPT_AUTH_TOKEN";
  const AUTH_ROLE_KEY = "QLPT_AUTH_ROLE";

  function applyHello(profile, options) {
    const name = profile?.hoTen || profile?.username || options.defaultName || "";
    const hello = document.getElementById("helloUser");
    if (hello) {
      hello.textContent = name;
      return;
    }

    if (!options.fallbackHelloSelector) return;
    const fallback = document.querySelector(options.fallbackHelloSelector);
    if (!fallback) return;
    fallback.textContent = `${options.fallbackHelloPrefix || ""}${name}`;
  }

  function redirectToLogin(loginUrl) {
    window.location.href = loginUrl || "../dangnhap/login.html";
  }

  function normalizeRole(role) {
    if (!role) return "";
    const normalized = String(role).trim().toUpperCase();
    if (normalized === "ADMIN" || normalized === "ROLE_ADMIN") return "ADMIN";
    if (normalized === "USER" || normalized === "ROLE_USER") return "USER";
    return normalized;
  }

  function redirectToRoleHome(role, options = {}) {
    const normalizedRole = normalizeRole(role);
    const apiBase = (window.API_BASE || window.location.origin || "").replace(/\/+$/, "");
    const adminHomeUrl = options.adminHomeUrl || `${apiBase}/admin/trangchu.html`;
    const userHomeUrl = options.userHomeUrl || `${apiBase}/nguoithue/trangcanhan.html`;

    if (normalizedRole === "ADMIN") {
      window.location.href = adminHomeUrl;
      return true;
    }
    if (normalizedRole === "USER") {
      window.location.href = userHomeUrl;
      return true;
    }
    return false;
  }

  window.AuthSessionCore = window.AuthSessionCore || {
    init(options = {}) {
      const { api } = window.AppUtils || {};
      if (typeof api !== "function") {
        throw new Error("AppUtils.api is required before AuthSessionCore.init");
      }

      const requiredRole = options.requiredRole || "";
      const loginUrl = options.loginUrl || "../dangnhap/login.html";

      const authState = {
        profile: null,
        loading: null,
      };

      async function loadProfile(force = false) {
        if (!force && authState.profile) return authState.profile;
        if (!force && authState.loading) return authState.loading;

        authState.loading = (async () => {
          const profile = await api("/api/auth/me");
          authState.profile = profile || null;
          if (authState.profile?.role) {
            localStorage.setItem(AUTH_ROLE_KEY, authState.profile.role);
          }
          return authState.profile;
        })();

        try {
          return await authState.loading;
        } finally {
          authState.loading = null;
        }
      }

      const session = {
        async ensure(force = false) {
          return loadProfile(force);
        },
        getCurrentUser() {
          return authState.profile;
        },
        getRole() {
          return authState.profile?.role || "";
        },
        async logout() {
          try {
            await api("/api/auth/logout", { method: "POST", auth: false });
          } catch (_) {
            // Ignore logout failures and still clear local auth cache.
          } finally {
            localStorage.removeItem(AUTH_TOKEN_KEY);
            localStorage.removeItem(AUTH_ROLE_KEY);
          }
        },
      };

      window.AuthSession = session;
      window.buildAuthHeaders = function buildAuthHeaders(headers = {}) {
        const finalHeaders = { ...headers };
        const hasAuthorizationHeader = Object.keys(finalHeaders)
          .some((key) => key.toLowerCase() === "authorization");

        if (!hasAuthorizationHeader) {
          const token = localStorage.getItem(AUTH_TOKEN_KEY);
          if (token) {
            finalHeaders.Authorization = `Bearer ${token}`;
          }
        }

        return finalHeaders;
      };

      const logoutBtn = document.getElementById("logoutBtn");
      if (logoutBtn) {
        logoutBtn.onclick = async () => {
          await session.logout();
          redirectToLogin(loginUrl);
        };
      }

      (async () => {
        try {
          const profile = await loadProfile();
          if (!profile) {
            const cachedRole = normalizeRole(localStorage.getItem(AUTH_ROLE_KEY));
            if (requiredRole && cachedRole === normalizeRole(requiredRole)) {
              return;
            }
            redirectToLogin(loginUrl);
            return;
          }

          const currentRole = normalizeRole(profile.role);
          if (requiredRole && currentRole !== normalizeRole(requiredRole)) {
            if (!redirectToRoleHome(currentRole, options)) {
              redirectToLogin(loginUrl);
            }
            return;
          }

          applyHello(profile, options);
        } catch (error) {
          if (error?.status === 401) {
            const cachedRole = normalizeRole(localStorage.getItem(AUTH_ROLE_KEY));
            if (requiredRole && cachedRole === normalizeRole(requiredRole)) {
              return;
            }
            redirectToLogin(loginUrl);
            return;
          }
          console.error("Auth session check failed:", error);
        }
      })();

      return session;
    },
  };
})();
