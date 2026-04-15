const currentUser = JSON.parse(localStorage.getItem("currentUser") || "null");
const role = localStorage.getItem("role");
const token = localStorage.getItem("authToken");
const fallbackName = localStorage.getItem("logged_in_user");

if (!currentUser || role !== "USER" || !token) {
  localStorage.clear();
  window.location.href = "../dangnhap/login.html";
}

window.buildAuthHeaders = function (headers = {}) {
  const authToken = localStorage.getItem("authToken") || "";
  return {
    ...headers,
    Authorization: `Bearer ${authToken}`
  };
};

const helloUser =
  (currentUser && (currentUser.hoTen || currentUser.username)) ||
  fallbackName ||
  "Người thuê";

const hello = document.getElementById("helloUser");
if (hello) {
  hello.textContent = helloUser;
} else {
  const userBlock = document.querySelector(".user");
  if (userBlock) userBlock.textContent = `Xin chào, ${helloUser}`;
}

const logoutBtn = document.getElementById("logoutBtn");
logoutBtn?.addEventListener("click", () => {
  localStorage.removeItem("currentUser");
  localStorage.removeItem("role");
  localStorage.removeItem("authToken");
  localStorage.removeItem("logged_in_user");
  localStorage.removeItem("tenant_selected_invoice_period");
  window.location.href = "../dangnhap/login.html";
});
