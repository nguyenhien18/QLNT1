const currentUser = JSON.parse(localStorage.getItem("currentUser") || "null");
const role = localStorage.getItem("role");
const token = localStorage.getItem("authToken");
const fallbackName = localStorage.getItem("logged_in_user");

if (!currentUser || role !== "ADMIN" || !token) {
  localStorage.clear();
  window.location.href = "../dangnhap/login.html";
}

window.buildAuthHeaders = function (headers = {}) {
  return {
    ...headers,
    Authorization: `Bearer ${localStorage.getItem("authToken") || ""}`
  };
};

const hello = document.getElementById("helloUser");
if (hello) {
  hello.textContent = (currentUser && (currentUser.hoTen || currentUser.username)) || fallbackName || "ADMIN";
}

const logoutBtn = document.getElementById("logoutBtn");
logoutBtn?.addEventListener("click", () => {
  localStorage.removeItem("currentUser");
  localStorage.removeItem("role");
  localStorage.removeItem("logged_in_user");
  localStorage.removeItem("authToken");
  localStorage.removeItem("admin_invoice_focus");
  window.location.href = "../dangnhap/login.html";
});
