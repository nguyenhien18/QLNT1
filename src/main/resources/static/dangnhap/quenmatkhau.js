const form = document.getElementById("forgotForm");
const msg = document.getElementById("msg");

function showMsg(text, ok) {
  msg.style.display = "block";
  msg.textContent = text;
  msg.className = "msg " + (ok ? "ok" : "err");
}

form?.addEventListener("submit", (e) => {
  e.preventDefault();
  const email = form.email.value.trim();
  const isGmail = /^[^\s@]+@gmail\.com$/i.test(email);
  if (!isGmail) {
    showMsg("Vui lòng nhập đúng Gmail (đuôi @gmail.com).", false);
    return;
  }
  showMsg(`Đã ghi nhận yêu cầu khôi phục cho: ${email}.`, true);
  setTimeout(() => { window.location.href = "login.html"; }, 1800);
});

