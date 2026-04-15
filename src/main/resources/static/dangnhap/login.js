const API_BASE = window.API_BASE || "http://localhost:8086";

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

async function tryLogin(url, username, password) {
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password })
  });
  let data = null;
  try { data = await res.json(); } catch { data = null; }
  const payload = data && typeof data === "object" && Object.prototype.hasOwnProperty.call(data, "result")
    ? data.result
    : data;
  return { ok: res.ok, data, payload, status: res.status };
}

form?.addEventListener("submit", async (e) => {
  e.preventDefault();
  clearError();

  const username = document.getElementById("username")?.value.trim() || "";
  const password = document.getElementById("password")?.value || "";

  if (!username || !password) {
    showError("Vui lòng nhập đầy đủ tài khoản và mật khẩu.");
    return;
  }

  try {
    let result = await tryLogin(`${API_BASE}/api/auth/login/admin`, username, password);
    if (result.ok) {
      const user = result.payload || {};
      localStorage.setItem("authToken", user.token || "");
      localStorage.setItem("currentUser", JSON.stringify(user));
      localStorage.setItem("role", "ADMIN");
      localStorage.setItem("logged_in_user", user.hoTen || user.username || username);
      window.location.href = "../admin/trangchu.html";
      return;
    }

    result = await tryLogin(`${API_BASE}/api/auth/login/user`, username, password);
    if (result.ok) {
      const user = result.payload || {};
      localStorage.setItem("authToken", user.token || "");
      localStorage.setItem("currentUser", JSON.stringify(user));
      localStorage.setItem("role", "USER");
      localStorage.setItem("logged_in_user", user.hoTen || user.username || username);
      window.location.href = "../nguoithue/trangcanhan.html";
      return;
    }

    showError((result.data && result.data.message) || "Sai tài khoản hoặc mật khẩu!");
  } catch (error) {
    console.error("Lỗi đăng nhập:", error);
    showError("Không kết nối được backend!");
  }
});


