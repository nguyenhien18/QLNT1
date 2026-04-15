const { api, money: fmtMoney } = window.AppUtils || {};
const { escapeHtml, textOrDash } = window.UiHelpers || {};
const { bindChanges, bindInputs } = window.PageFilters || {};

const $ = (id) => document.getElementById(id);
const PAGE_SIZE = 5;
let currentPage = 1;
let totalPages = 1;
let totalItems = 0;
let rooms = [];
let services = [];
let roomServices = [];
let contracts = [];
let members = [];

function openModal() { window.Modal?.open("roomModal"); }
function closeModal() { window.Modal?.close("roomModal"); }
function typeText(v) { return window.AppUtils?.labels?.roomType(v) || (v === "CO_GAC" ? "Có gác" : "Không gác"); }
function statusText(v) { return window.AppUtils?.labels?.roomStatus(v) || (v === "DA_CHO_THUE" ? "Đã cho thuê" : "Phòng trống"); }
function statusClass(v) { return v === "DA_CHO_THUE" ? "status-rented" : "status-empty"; }

async function loadMeta() {
  [services, roomServices, contracts, members] = await Promise.all([
    window.fetchAllPages(api, "/api/dich-vu", { sortBy: "dichVuId", direction: "desc" }).catch(() => []),
    api("/api/phong-dich-vu").catch(() => []),
    window.fetchAllPages(api, "/api/hop-dong", { sortBy: "ngayBatDau", direction: "desc" }).catch(() => []),
    window.fetchAllPages(api, "/api/thanh-vien-phong", { sortBy: "thanhVienId", direction: "desc" }).catch(() => []),
  ]);
  renderServiceChecklist();
}

async function loadList() {
  const name = $("fRoomName")?.value.trim() || "";
  const type = $("fRoomType")?.value || "";
  const status = $("fRoomStatus")?.value || "";
  const keyword = "";

  try {
    const pageData = await window.fetchPage(api, "/api/phong-tro/search", {
      page: currentPage - 1,
      size: PAGE_SIZE,
      params: { name, type, status, keyword },
    });

    rooms = pageData.content || [];
    totalItems = Number(pageData.totalElements || 0);
    totalPages = Math.max(1, Number(pageData.totalPages || 1));

    if (currentPage > totalPages) {
      currentPage = totalPages;
      return loadList();
    }
  } catch (e) {
    console.error("Load room search failed", e);
    rooms = [];
    totalItems = 0;
    totalPages = 1;
  }

  render();
}

function renderServiceChecklist(selected = []) {
  const box = $("roomServiceList");
  if (!box) return;

  box.innerHTML = services.map((s) => `
    <label class="service-item">
      <input type="checkbox" value="${s.dichVuId}" ${selected.includes(s.dichVuId) ? "checked" : ""}>
      <span>${escapeHtml ? escapeHtml(s.tenDichVu || "") : (s.tenDichVu || "")} (${fmtMoney(s.giaDichVu)})</span>
    </label>
  `).join("");
}

function getSelectedServiceIds() {
  return [...document.querySelectorAll('#roomServiceList input[type="checkbox"]:checked')]
      .map((x) => Number(x.value));
}

function activeContractForRoom(roomId) {
  return contracts.find((c) => c.phongTro?.phongTroId === roomId && c.trangThai === "CON_HIEU_LUC");
}

function hasAnyContractForRoom(roomId) {
  return contracts.some((c) => c.phongTro?.phongTroId === roomId);
}

function currentCount(roomId) {
  const c = activeContractForRoom(roomId);
  if (!c) return 0;
  return members.filter((m) => m.hopDong?.hopDongId === c.hopDongId).length;
}

function representativeName(roomId) {
  const c = activeContractForRoom(roomId);
  if (!c) return "Trống";
  const rep = members.find((m) => m.hopDong?.hopDongId === c.hopDongId && m.vaiTro === "DAI_DIEN");
  return rep?.hoTen || c.khachThue?.hoTen || "Chưa có";
}

function render() {
  const start = totalItems ? (currentPage - 1) * PAGE_SIZE + 1 : 0;
  const end = Math.min(currentPage * PAGE_SIZE, totalItems);

  $("roomCountText").textContent = `${totalItems} phòng được tìm thấy`;
  $("roomPagingInfo").textContent = totalItems
      ? `Hiển thị ${start} - ${end} / ${totalItems} phòng`
      : "Hiển thị 0 phòng";
  $("pageInfo").textContent = `Trang ${currentPage} / ${totalPages}`;
  $("prevPageBtn").disabled = currentPage <= 1;
  $("nextPageBtn").disabled = currentPage >= totalPages;

  const tbody = $("roomTableBody");
  if (!tbody) return;

  if (!totalItems) {
    tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;color:#6b7280;">Không có phòng nào phù hợp.</td></tr>';
    return;
  }

  tbody.innerHTML = rooms.map((r) => `
    <tr>
      <td class="room-name-cell">${textOrDash ? textOrDash(r.tenPhong) : (r.tenPhong || "")}</td>
      <td>${escapeHtml ? escapeHtml(typeText(r.loaiPhong)) : typeText(r.loaiPhong)}</td>
      <td class="room-price">${fmtMoney(r.giaThue)}</td>
      <td><span class="status-badge ${statusClass(r.trangThai)}">${escapeHtml ? escapeHtml(statusText(r.trangThai)) : statusText(r.trangThai)}</span></td>
      <td>${textOrDash ? textOrDash(representativeName(r.phongTroId)) : representativeName(r.phongTroId)}</td>
      <td>${r.sucChua || 0}</td>
      <td>${currentCount(r.phongTroId)}</td>
      <td>
        <div class="action-group">
          <button class="btn-small btn-edit" onclick="window.editRoom(${r.phongTroId})">Sửa</button>
          <button class="btn-small btn-delete" onclick="window.deleteRoom(${r.phongTroId})">Xóa</button>
        </div>
      </td>
    </tr>
  `).join("");
}

function clearForm() {
  $("roomForm").reset();
  $("roomId").value = "";
  $("roomCurrent").value = "0";
  $("roomRepresentative").value = "Trống";
  $("roomStatus").value = "TRONG";
  renderServiceChecklist([]);
}

window.editRoom = async function editRoom(id) {
  if (activeContractForRoom(id)) {
    alert("Phòng đang có hợp đồng hiệu lực, không được sửa.");
    return;
  }

  let r = rooms.find((x) => x.phongTroId === id);
  if (!r) {
    try {
      r = await api(`/api/phong-tro/${id}`);
    } catch (_) {
      return;
    }
  }

  if (!r) return;

  $("roomId").value = r.phongTroId;
  $("roomName").value = r.tenPhong || "";
  $("roomType").value = r.loaiPhong || "KHONG_GAC";
  $("roomPrice").value = r.giaThue || 0;
  $("roomStatus").value = r.trangThai || "TRONG";
  $("roomRepresentative").value = representativeName(r.phongTroId);
  $("roomCapacity").value = r.sucChua || 1;
  $("roomCurrent").value = currentCount(r.phongTroId);
  $("roomDesc").value = r.moTa || "";

  const selected = roomServices
      .filter((rs) => (rs.phongTro?.phongTroId || rs.phongTroId) === r.phongTroId)
      .map((rs) => Number(rs.dichVu?.dichVuId || rs.dichVuId));

  renderServiceChecklist(selected);
  $("roomTitle").textContent = "Cập nhật phòng";
  openModal();
};

window.deleteRoom = async function deleteRoom(id) {
  if (hasAnyContractForRoom(id)) {
    alert("Không thể xóa phòng đã từng có hợp đồng.");
    return;
  }

  if (!confirm("Xóa phòng này?")) return;

  try {
    await api(`/api/phong-tro/${id}`, { method: "DELETE" });
    await loadList();
  } catch (e) {
    alert("Xóa phòng thất bại: " + e.message);
  }
};

async function syncRoomServices(roomId) {
  const selected = getSelectedServiceIds();
  const current = roomServices
      .filter((rs) => (rs.phongTro?.phongTroId || rs.phongTroId) === roomId)
      .map((rs) => Number(rs.dichVu?.dichVuId || rs.dichVuId));

  const toAdd = selected.filter((id) => !current.includes(id));
  const toDel = current.filter((id) => !selected.includes(id));

  for (const id of toAdd) {
    try {
      await api("/api/phong-dich-vu", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ phongTroId: roomId, dichVuId: id }),
      });
    } catch (_) {}
  }

  for (const id of toDel) {
    try {
      await api(`/api/phong-dich-vu?phongTroId=${roomId}&dichVuId=${id}`, { method: "DELETE" });
    } catch (_) {}
  }
}

async function saveRoom() {
  const id = $("roomId").value.trim();

  if (id && activeContractForRoom(Number(id))) {
    alert("Phòng đang có hợp đồng hiệu lực, không được sửa.");
    return;
  }

  const currentUser = JSON.parse(localStorage.getItem("currentUser") || "null");
  const body = {
    chuTro: { chuTroId: currentUser?.id },
    tenPhong: $("roomName").value.trim(),
    loaiPhong: $("roomType").value,
    giaThue: Number($("roomPrice").value || 0),
    trangThai: $("roomStatus").value,
    sucChua: Number($("roomCapacity").value || 0),
    moTa: $("roomDesc").value.trim(),
  };

  if (!body.tenPhong || !body.chuTro?.chuTroId) {
    alert("Thiếu tên phòng hoặc thông tin chủ trọ đăng nhập.");
    return;
  }

  try {
    let saved;
    if (id) {
      saved = await api(`/api/phong-tro/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });
    } else {
      saved = await api("/api/phong-tro", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });
    }

    await syncRoomServices(saved.phongTroId || Number(id));
    closeModal();
    clearForm();
    await loadMeta();
    await loadList();
  } catch (e) {
    alert("Lưu phòng thất bại: " + e.message);
  }
}

document.addEventListener("DOMContentLoaded", async () => {
  $("addRoomBtn").addEventListener("click", () => {
    clearForm();
    $("roomTitle").textContent = "Thêm phòng";
    openModal();
  });

  $("roomClose").addEventListener("click", closeModal);
  $("roomCancel").addEventListener("click", closeModal);
  $("roomBackdrop").addEventListener("click", closeModal);
  $("roomSave").addEventListener("click", saveRoom);

  bindChanges?.(["fRoomType", "fRoomStatus"], () => {
    currentPage = 1;
    loadList();
  });

  bindInputs?.(["fRoomName"], () => {
    currentPage = 1;
    loadList();
  });

  $("resetRoomFilter").addEventListener("click", () => {
    $("fRoomName").value = "";
    $("fRoomType").value = "";
    $("fRoomStatus").value = "";
    currentPage = 1;
    loadList();
  });

  window.PageFilters?.bindPagination(
      "prevPageBtn",
      "nextPageBtn",
      {
        get page() { return currentPage; },
        set page(v) { currentPage = v; },
        get totalPages() { return totalPages; },
      },
      loadList
  );

  await loadMeta();
  await loadList();
});