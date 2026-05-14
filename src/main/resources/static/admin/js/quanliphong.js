(function () {
const { api: apiClient, money: fmtMoney } = window.AppUtils || {};
const { escapeHtml, textOrDash } = window.UiHelpers || {};
const { bindChanges } = window.PageFilters || {};

const $ = (id) => document.getElementById(id);
const PAGE_SIZE = 5;
const SERVICE_PAGE_SIZE = 1000;

let currentPage = 1;
let totalPages = 1;
let totalItems = 0;
let rooms = [];
let services = [];
let summaryByRoomId = new Map();
let roomNameOptions = [];

function openModal() { window.Modal?.open("roomModal"); }
function closeModal() { window.Modal?.close("roomModal"); }
function normalizeRoomType(v) {
  const key = String(v || "").trim().toUpperCase();
  if (key === "VIP") return "VIP";
  return "THUONG";
}
function typeText(v) {
  const normalized = normalizeRoomType(v);
  const mapped = window.AppUtils?.labels?.roomType?.(normalized);
  if (mapped) return mapped;
  return normalized === "VIP" ? "VIP" : "Thuong";
}
function roomTypeClass(v) {
  return normalizeRoomType(v) === "VIP" ? "room-type-vip" : "room-type-thuong";
}
function roomTypeBadge(v) {
  const text = typeText(v);
  const safe = escapeHtml ? escapeHtml(text) : text;
  return `<span class="room-type-badge ${roomTypeClass(v)}">${safe}</span>`;
}
function statusText(v) { return window.AppUtils?.labels?.roomStatus(v) || (v === "DA_CHO_THUE" ? "Da cho thue" : "Phong trong"); }
function statusClass(v) { return v === "DA_CHO_THUE" ? "status-rented" : "status-empty"; }

function roomSummary(roomId) {
  return summaryByRoomId.get(Number(roomId)) || null;
}

function isRoomOccupied(roomId) {
  const summary = roomSummary(roomId);
  if (!summary) return false;
  if (summary.trangThai === "DA_CHO_THUE") return true;
  return Number(summary.soNguoiDangO || 0) > 0;
}

function currentCount(roomId) {
  return Number(roomSummary(roomId)?.soNguoiDangO || 0);
}

function representativeName(roomId) {
  return roomSummary(roomId)?.daiDien || "Trong";
}

function buildRoomNameOptions(summaryItems) {
  return [...new Set((summaryItems || [])
    .map((item) => String(item?.tenPhong || "").trim())
    .filter(Boolean))]
    .sort((left, right) => left.localeCompare(right, "vi", { numeric: true, sensitivity: "base" }));
}

function renderRoomNameFilter() {
  const roomNameFilter = $("fRoomName");
  if (!roomNameFilter) return;

  const currentValue = roomNameFilter.value || "";
  roomNameFilter.innerHTML = '<option value="">Tat ca</option>' + roomNameOptions
    .map((name) => {
      const safeName = escapeHtml ? escapeHtml(name) : name;
      return `<option value="${safeName}">${safeName}</option>`;
    })
    .join("");

  if (currentValue && roomNameOptions.includes(currentValue)) {
    roomNameFilter.value = currentValue;
  }
}

async function loadMeta() {
  const [servicePage, roomSummaryList] = await Promise.all([
    window.fetchPage(apiClient, "/api/dich-vu", {
      page: 0,
      size: SERVICE_PAGE_SIZE,
      sortBy: "dichVuId",
      direction: "desc",
    }).catch(() => ({ content: [] })),
    apiClient("/api/phong-tro/summary").catch(() => []),
  ]);

  services = servicePage.content || [];
  const summaryItems = roomSummaryList || [];
  summaryByRoomId = new Map(summaryItems.map((item) => [Number(item.phongTroId), item]));
  roomNameOptions = buildRoomNameOptions(summaryItems);
  renderRoomNameFilter();
  renderServiceChecklist();
}

async function refreshRoomSummary() {
  const summaryItems = await apiClient("/api/phong-tro/summary").catch(() => []);
  summaryByRoomId = new Map((summaryItems || []).map((item) => [Number(item.phongTroId), item]));
  roomNameOptions = buildRoomNameOptions(summaryItems);
  renderRoomNameFilter();
}

async function loadList() {
  const name = $("fRoomName")?.value.trim() || "";
  const type = $("fRoomType")?.value || "";
  const status = $("fRoomStatus")?.value || "";

  try {
    const pageData = await window.fetchPage(apiClient, "/api/phong-tro/search", {
      page: currentPage - 1,
      size: PAGE_SIZE,
      params: { name, type, status },
    });

    rooms = pageData.content || [];
    totalItems = Number(pageData.totalElements || 0);
    totalPages = Math.max(1, Number(pageData.totalPages || 1));

    if (currentPage > totalPages) {
      currentPage = totalPages;
      return loadList();
    }
  } catch (error) {
    console.error("Load room search failed", error);
    const paging = $("roomPagingInfo");
    if (paging) {
      const status = Number(error?.status || 0);
      paging.textContent = status
        ? `Khong tai duoc du lieu phong (HTTP ${status}). Vui long dang nhap lai.`
        : "Khong tai duoc du lieu phong. Vui long thu lai.";
    }
    rooms = [];
    totalItems = 0;
    totalPages = 1;
  }

  render();
}

function renderServiceChecklist(selected = []) {
  const box = $("roomServiceList");
  if (!box) return;

  box.innerHTML = services.map((service) => `
    <label class="service-item">
      <input type="checkbox" value="${service.dichVuId}" ${selected.includes(service.dichVuId) ? "checked" : ""}>
      <span>${escapeHtml ? escapeHtml(service.tenDichVu || "") : (service.tenDichVu || "")} (${fmtMoney(service.giaDichVu)})</span>
    </label>
  `).join("");
}

function getSelectedServiceIds() {
  return [...document.querySelectorAll('#roomServiceList input[type="checkbox"]:checked')]
    .map((input) => Number(input.value))
    .filter((id) => Number.isFinite(id));
}

function render() {
  const start = totalItems ? (currentPage - 1) * PAGE_SIZE + 1 : 0;
  const end = Math.min(currentPage * PAGE_SIZE, totalItems);

  $("roomCountText").textContent = `${totalItems} phong duoc tim thay`;
  $("roomPagingInfo").textContent = totalItems
    ? `Hien thi ${start} - ${end} / ${totalItems} phong`
    : "Hien thi 0 phong";
  $("pageInfo").textContent = `Trang ${currentPage} / ${totalPages}`;
  $("prevPageBtn").disabled = currentPage <= 1;
  $("nextPageBtn").disabled = currentPage >= totalPages;

  const tbody = $("roomTableBody");
  if (!tbody) return;

  if (!totalItems) {
    tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;color:#6b7280;">Khong co phong nao phu hop.</td></tr>';
    return;
  }

  tbody.innerHTML = rooms.map((room) => `
    <tr>
      <td class="room-name-cell">${textOrDash ? textOrDash(room.tenPhong) : (room.tenPhong || "")}</td>
      <td>${roomTypeBadge(room.loaiPhong)}</td>
      <td class="room-price">${fmtMoney(room.giaThue)}</td>
      <td><span class="status-badge ${statusClass(room.trangThai)}">${escapeHtml ? escapeHtml(statusText(room.trangThai)) : statusText(room.trangThai)}</span></td>
      <td>${textOrDash ? textOrDash(representativeName(room.phongTroId)) : representativeName(room.phongTroId)}</td>
      <td>${room.sucChua || 0}</td>
      <td>${currentCount(room.phongTroId)}</td>
      <td>
        <div class="action-group">
          <button class="btn-small btn-edit" type="button" data-action="edit" data-id="${room.phongTroId}">Sua</button>
          <button class="btn-small btn-delete" type="button" data-action="delete" data-id="${room.phongTroId}">Xoa</button>
        </div>
      </td>
    </tr>
  `).join("");
}

function clearForm() {
  $("roomForm").reset();
  $("roomId").value = "";
  $("roomCurrent").value = "0";
  $("roomRepresentative").value = "Trong";
  $("roomStatus").value = "TRONG";
  renderServiceChecklist([]);
}

async function loadRoomServiceIds(roomId) {
  try {
    const pageData = await window.fetchPage(apiClient, `/api/phong-dich-vu/phong/${roomId}`, {
      page: 0,
      size: SERVICE_PAGE_SIZE,
    });
    return (pageData.content || [])
      .map((item) => Number(item.dichVu?.dichVuId || item.dichVuId))
      .filter((id) => Number.isFinite(id));
  } catch (_) {
    return [];
  }
}

async function editRoom(id) {
  if (isRoomOccupied(id)) {
    alert("Phong dang co hop dong hieu luc, khong duoc sua.");
    return;
  }

  let room = rooms.find((item) => item.phongTroId === id);
  if (!room) {
    try {
      room = await apiClient(`/api/phong-tro/${id}`);
    } catch (_) {
      return;
    }
  }
  if (!room) return;

  $("roomId").value = room.phongTroId;
  $("roomName").value = room.tenPhong || "";
  $("roomType").value = normalizeRoomType(room.loaiPhong);
  $("roomPrice").value = room.giaThue || 0;
  $("roomStatus").value = room.trangThai || "TRONG";
  $("roomRepresentative").value = representativeName(room.phongTroId);
  $("roomCapacity").value = room.sucChua || 1;
  $("roomCurrent").value = currentCount(room.phongTroId);
  $("roomDesc").value = room.moTa || "";

  const selectedServiceIds = await loadRoomServiceIds(room.phongTroId);
  renderServiceChecklist(selectedServiceIds);
  $("roomTitle").textContent = "Cap nhat phong";
  openModal();
}

async function deleteRoom(id) {
  if (!confirm("Xoa phong nay?")) return;
  try {
    await apiClient(`/api/phong-tro/${id}`, { method: "DELETE" });
    await refreshRoomSummary();
    await loadList();
  } catch (error) {
    alert("Xoa phong that bai: " + error.message);
  }
}

async function syncRoomServices(roomId) {
  const selected = getSelectedServiceIds();
  const current = await loadRoomServiceIds(roomId);

  const toAdd = selected.filter((id) => !current.includes(id));
  const toDelete = current.filter((id) => !selected.includes(id));

  for (const serviceId of toAdd) {
    try {
      await apiClient("/api/phong-dich-vu", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ phongTroId: roomId, dichVuId: serviceId }),
      });
    } catch (_) {
      // Ignore duplicate/partial failures, backend validation is source of truth.
    }
  }

  for (const serviceId of toDelete) {
    try {
      await apiClient(`/api/phong-dich-vu?phongTroId=${roomId}&dichVuId=${serviceId}`, { method: "DELETE" });
    } catch (_) {
      // Ignore partial failures, list refresh will reflect actual state from backend.
    }
  }
}

async function saveRoom() {
  const id = $("roomId").value.trim();

  if (id && isRoomOccupied(Number(id))) {
    alert("Phong dang co hop dong hieu luc, khong duoc sua.");
    return;
  }

  const ensuredUser = window.AuthSession?.ensure
    ? await window.AuthSession.ensure().catch(() => null)
    : null;
  const currentUser = window.AuthSession?.getCurrentUser?.() || ensuredUser;
  const body = {
    chuTroId: currentUser?.id,
    tenPhong: $("roomName").value.trim(),
    loaiPhong: normalizeRoomType($("roomType").value),
    giaThue: Number($("roomPrice").value || 0),
    trangThai: $("roomStatus").value,
    sucChua: Number($("roomCapacity").value || 0),
    moTa: $("roomDesc").value.trim(),
  };

  if (!body.tenPhong || !body.chuTroId) {
    alert("Thieu ten phong hoac thong tin chu tro dang nhap.");
    return;
  }

  try {
    let saved;
    if (id) {
      saved = await apiClient(`/api/phong-tro/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });
    } else {
      saved = await apiClient("/api/phong-tro", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });
    }

    await syncRoomServices(saved.phongTroId || Number(id));
    closeModal();
    clearForm();
    await refreshRoomSummary();
    await loadList();
  } catch (error) {
    alert("Luu phong that bai: " + error.message);
  }
}

function bindTableActions() {
  $("roomTableBody")?.addEventListener("click", async (event) => {
    const button = event.target.closest("button[data-action][data-id]");
    if (!button) return;

    const roomId = Number(button.dataset.id);
    if (!Number.isFinite(roomId) || roomId <= 0) return;

    if (button.dataset.action === "edit") {
      await editRoom(roomId);
      return;
    }
    if (button.dataset.action === "delete") {
      await deleteRoom(roomId);
    }
  });
}

document.addEventListener("DOMContentLoaded", async () => {
  $("addRoomBtn").addEventListener("click", () => {
    clearForm();
    $("roomTitle").textContent = "Them phong";
    openModal();
  });

  $("roomClose").addEventListener("click", closeModal);
  $("roomCancel").addEventListener("click", closeModal);
  $("roomBackdrop").addEventListener("click", closeModal);
  $("roomSave").addEventListener("click", saveRoom);
  bindTableActions();

  bindChanges?.(["fRoomName", "fRoomType", "fRoomStatus"], () => {
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
    loadList,
  );

  await loadMeta();
  await loadList();
});

})();
