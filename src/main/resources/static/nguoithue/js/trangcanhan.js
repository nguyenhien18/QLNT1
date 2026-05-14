(function () {
  const { api: apiClient, labels } = window.AppUtils || {};

  function fmtDate(value) {
    if (!value) return "";
    if (typeof value === "string" && /^\d{4}-\d{2}-\d{2}/.test(value)) {
      const [year, month, day] = value.slice(0, 10).split("-");
      return `${day}-${month}-${year}`;
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleDateString("vi-VN");
  }

  function genderText(value) {
    if (value === "NAM") return "Nam";
    if (value === "NU") return "Nu";
    return "Khac";
  }

  function accountStatusText(value) {
    if (value === "HOAT_DONG") return "Dang hoat dong";
    if (value === "KHOA") return "Tam khoa";
    return "Chua cap nhat";
  }

  function buildInitials(fullName) {
    const words = String(fullName || "")
      .trim()
      .split(/\s+/)
      .filter(Boolean);
    if (!words.length) return "--";
    const first = words[0].charAt(0);
    const last = words.length > 1 ? words[words.length - 1].charAt(0) : "";
    return `${first}${last}`.toUpperCase();
  }

  function setText(id, value) {
    const el = document.getElementById(id);
    if (el) el.textContent = value ?? "";
  }

  function updateStatusPill(status) {
    const el = document.getElementById("profileStatus");
    if (!el) return;
    el.textContent = accountStatusText(status);
    el.classList.remove("is-active", "is-locked");
    if (status === "HOAT_DONG") el.classList.add("is-active");
    if (status === "KHOA") el.classList.add("is-locked");
  }

  function updateRoomPill(roomName, roleName) {
    const el = document.getElementById("profileRoomTag");
    if (!el) return;
    if (!roomName || roomName === "Chua co") {
      el.textContent = "Chua co hop dong";
      return;
    }
    el.textContent = roleName ? `${roomName} - ${roleName}` : roomName;
  }

  async function loadProfile() {
    try {
      const [tenant, currentContract] = await Promise.all([
        apiClient("/api/tenant/profile"),
        apiClient("/api/tenant/hop-dong/current").catch(() => null),
      ]);

      const activeContract = currentContract || null;
      const isRepresentative = Boolean(
        activeContract &&
        tenant?.khachThueId &&
        activeContract?.khachThue?.khachThueId &&
        Number(activeContract.khachThue.khachThueId) === Number(tenant.khachThueId),
      );

      const roomName = activeContract?.phongTro?.tenPhong || "Chua co";
      const roleCode = activeContract ? (isRepresentative ? "DAI_DIEN" : "O_CUNG") : "";
      const roleName = roleCode ? (labels?.memberRole(roleCode) || (roleCode === "DAI_DIEN" ? "Dai dien" : "O cung")) : "Chua co";

      setText("fullName", tenant?.hoTen || "");
      setText("email", tenant?.email || "");
      setText("phone", tenant?.sdt || "");
      setText("cccd", tenant?.cccd || "");
      setText("address", tenant?.diaChi || "");
      setText("dob", fmtDate(tenant?.ngaySinh) || "");
      setText("gender", genderText(tenant?.gioiTinh));
      setText("username", tenant?.tenDangNhap || "");
      setText("tenantStatus", accountStatusText(tenant?.trangThai));
      setText("createdAt", fmtDate(tenant?.createdAt) || "");
      setText("room", roomName);
      setText("roleInRoom", roleName);

      setText("profileName", tenant?.hoTen || "Nguoi thue");
      setText("profileUsername", tenant?.tenDangNhap ? `@${tenant.tenDangNhap}` : "@");
      setText("profileInitials", buildInitials(tenant?.hoTen || ""));
      updateStatusPill(tenant?.trangThai);
      updateRoomPill(roomName, roleName === "Chua co" ? "" : roleName);
    } catch (error) {
      console.error("Loi load trang ca nhan:", error);
      alert("Khong tai duoc thong tin ca nhan.");
    }
  }

  document.addEventListener("DOMContentLoaded", loadProfile);
})();
