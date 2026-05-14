(function () {
  const LOGIN_URL = "../dangnhap/login.html";
  const AUTH_SESSION_SRC = "../js/core/auth-session.js?v=20260508sessionfix1";

  function redirectLogin() {
    window.location.href = LOGIN_URL;
  }

  function initAuth() {
    window.AuthSessionCore.init({
      requiredRole: "ADMIN",
      loginUrl: LOGIN_URL,
      defaultName: "ADMIN",
    });
  }

  function loadAuthSessionCore() {
    return new Promise((resolve, reject) => {
      const existing = document.querySelector('script[data-auth-session-core="1"]');
      if (existing) {
        existing.addEventListener("load", resolve, { once: true });
        existing.addEventListener("error", reject, { once: true });
        return;
      }

      const script = document.createElement("script");
      script.src = AUTH_SESSION_SRC;
      script.dataset.authSessionCore = "1";
      script.onload = resolve;
      script.onerror = reject;
      document.head.appendChild(script);
    });
  }

  (async () => {
    try {
      if (!window.AuthSessionCore?.init) {
        await loadAuthSessionCore();
      }
      if (!window.AuthSessionCore?.init) {
        redirectLogin();
        return;
      }
      initAuth();
    } catch (_) {
      redirectLogin();
    }
  })();
})();
