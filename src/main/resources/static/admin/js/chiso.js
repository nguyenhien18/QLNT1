(function () {
const PAGE_SIZE = 4;
const { buildOptions, textOrDash } = window.UiHelpers || {};
const { bindChanges, bindInputs } = window.PageFilters || {};
const { api: apiClient, money: fmtMoney, date: fmtDate, todayISO, labels } = window.AppUtils || {};
const $ = (id) => document.getElementById(id);

let currentPage = 1;
let totalPages = 1;
let totalItems = 0;
let meters = [];
let rooms = [];
let contracts = [];
let invoiceLockedKeys = new Set();

function openModal() { window.Modal?.open("meterModal"); }
function closeModal() { window.Modal?.close("meterModal"); }
function getUnit(type) { return type === "DIEN" ? "kWh" : "m3"; }
function getTypeLabel(type) { return labels?.meterType(type) || (type === "DIEN" ? "Dien" : "Nuoc"); }
function getTypeBadgeClass(type) { return type === "DIEN" ? "badge badge-wait" : "badge badge-green"; }

function calcConsumption(meter) {
  return Number(meter.luongTieuThu ?? (Number(meter.chiSoMoi || 0) - Number(meter.chiSoCu || 0)));
}
function calcAmount(meter) {
  return Number(meter.thanhTien ?? calcConsumption(meter) * Number(meter.donGia || 0));
}
function meterLockKey(contractId, roomId, period) {
  return `${contractId || ""}::${roomId || ""}::${period || ""}`;
}
function isMeterLocked(meter) {
  const contractId = meter?.hopDong?.hopDongId;
  const roomId = meter?.phongTro?.phongTroId;
  return invoiceLockedKeys.has(meterLockKey(contractId, roomId, meter?.ky));
}
function isExpiredContract(c) {
  return !!(c?.ngayKetThuc && c.ngayKetThuc < todayISO());
}

function isSelectableContract(contract) {
  return contract?.trangThai === "CON_HIEU_LUC" && !isExpiredContract(contract);
}

function renderOptions() {
  const selectableContracts = (contracts || []).filter(isSelectableContract);
  const roomNames = [...new Set((rooms || []).map((room) => room?.tenPhong).filter(Boolean))];
  $("meterContract").innerHTML = '<option value="">-- Chon hop dong --</option>' + selectableContracts
    .map((c) => {
      const room = c?.phongTro?.tenPhong || "-";
      const tenant = c?.khachThue?.tenDangNhap || c?.khachThue?.hoTen || "-";
      return `<option value="${c.hopDongId}">#${c.hopDongId} - ${room} - ${tenant}</option>`;
    })
    .join("");
  $("filterRoom").innerHTML = buildOptions(roomNames, (room) => room, (room) => room, "Tat ca");
}

function renderMeterRow(meter) {
  return `
    <tr data-id="${meter.chiSoId}">
      <td class="name-cell">${textOrDash(meter.phongTro?.tenPhong)}</td>
      <td>${meter.hopDong?.hopDongId ? `#${meter.hopDong.hopDongId}` : '<span class="text-muted">-</span>'}</td>
      <td><span class="${getTypeBadgeClass(meter.loai)}">${getTypeLabel(meter.loai)}</span></td>
      <td>${textOrDash(meter.ky)}</td>
      <td>${textOrDash(fmtDate(meter.thoiDiem))}</td>
      <td>${textOrDash(meter.chiSoCu)}</td>
      <td>${textOrDash(meter.chiSoMoi)}</td>
      <td>${calcConsumption(meter)} ${getUnit(meter.loai)}</td>
      <td>${fmtMoney(calcAmount(meter))}</td>
      <td>
        <div class="action-group">
          <button class="btn-small btn-edit" type="button" data-action="edit" data-id="${meter.chiSoId}">Sua</button>
          <button class="btn-small btn-delete" type="button" data-action="delete" data-id="${meter.chiSoId}">Xoa</button>
        </div>
      </td>
    </tr>`;
}

function render() {
  const startIndex = totalItems ? (currentPage - 1) * PAGE_SIZE + 1 : 0;
  const endIndex = Math.min(currentPage * PAGE_SIZE, totalItems);

  $("meterCountText").textContent = `${totalItems} chi so duoc tim thay`;
  $("meterPagingInfo").textContent = totalItems ? `Hien thi ${startIndex} - ${endIndex} / ${totalItems} chi so` : "Hien thi 0 chi so";
  $("pageInfo").textContent = `Trang ${currentPage} / ${totalPages}`;
  $("prevPageBtn").disabled = currentPage <= 1;
  $("nextPageBtn").disabled = currentPage >= totalPages;
  $("emptyState").style.display = totalItems ? "none" : "";
  $("meterList").innerHTML = totalItems ? meters.map(renderMeterRow).join("") : "";
}

function resetForm() {
  $("meterForm").reset();
  $("meterId").value = "";
  $("meterDate").value = todayISO();
}

function fillForm(meter) {
  $("meterTitle").textContent = "C?p nh?t chi so";
  $("meterId").value = meter.chiSoId;
  $("meterType").value = meter.loai || "DIEN";
  $("meterContract").value = meter.hopDong?.hopDongId || inferContractIdForMeter(meter) || "";
  $("meterPeriod").value = meter.ky || "";
  $("meterDate").value = (meter.thoiDiem || "").slice(0, 10);
  $("meterOld").value = meter.chiSoCu || 0;
  $("meterNew").value = meter.chiSoMoi || 0;
  $("meterUnitPrice").value = meter.donGia || 0;
  openModal();
}

function buildPayload() {
  const contractId = Number($("meterContract").value || 0);
  const contract = contracts.find((c) => c.hopDongId === contractId);
  const roomId = contract?.phongTro?.phongTroId ? Number(contract.phongTro.phongTroId) : null;
  return {
    hopDongId: contractId || null,
    phongTroId: roomId,
    loai: $("meterType").value,
    ky: $("meterPeriod").value.trim(),
    thoiDiem: $("meterDate").value,
    chiSoCu: Number($("meterOld").value || 0),
    chiSoMoi: Number($("meterNew").value || 0),
    donGia: Number($("meterUnitPrice").value || 0),
  };
}

async function loadMeta() {
  const [roomPage, contractPage] = await Promise.all([
    window.fetchPage(apiClient, "/api/phong-tro", {
      page: 0,
      size: 1000,
      sortBy: "phongTroId",
      direction: "desc",
    }).catch(() => ({ content: [] })),
    window.fetchPage(apiClient, "/api/hop-dong", {
      page: 0,
      size: 1000,
      sortBy: "ngayBatDau",
      direction: "desc",
    }).catch(() => ({ content: [] })),
  ]);

  rooms = roomPage.content || [];
  contracts = contractPage.content || [];
  renderOptions();
}

function inferContractIdForMeter(meter) {
  const roomId = meter?.phongTro?.phongTroId;
  if (!roomId) return "";
  const meterDate = meter?.thoiDiem ? new Date(meter.thoiDiem) : null;
  const matched = (contracts || []).filter((contract) => {
    if (contract?.trangThai === "HUY") return false;
    if (contract?.phongTro?.phongTroId !== roomId) return false;
    if (!meterDate) return true;

    const start = contract?.ngayBatDau ? new Date(contract.ngayBatDau) : null;
    const end = contract?.ngayKetThuc ? new Date(contract.ngayKetThuc) : null;
    return (!start || meterDate >= start) && (!end || meterDate <= end);
  });

  matched.sort((a, b) => String(b?.ngayBatDau || "").localeCompare(String(a?.ngayBatDau || "")));
  return matched[0]?.hopDongId || "";
}

async function loadList() {
  const type = $("filterType").value;
  const room = $("filterRoom").value;
  const period = $("filterPeriod").value.trim();

  try {
    const pageData = await window.fetchPage(apiClient, "/api/chi-so/search", {
      page: currentPage - 1,
      size: PAGE_SIZE,
      params: { type, room, period },
    });
    meters = pageData.content || [];
    totalItems = Number(pageData.totalElements || 0);
    totalPages = Math.max(1, Number(pageData.totalPages || 1));
    if (currentPage > totalPages) {
      currentPage = totalPages;
      return loadList();
    }
  } catch (error) {
    console.error("Load chi so search failed", error);
    meters = [];
    totalItems = 0;
    totalPages = 1;
  }

  await loadInvoiceLocksForCurrentMeters();
  render();
}

async function loadInvoiceLocksForCurrentMeters() {
  invoiceLockedKeys = new Set();
  if (!meters.length) return;

  try {
    const checks = await Promise.all(meters.map(async (meter) => {
      const roomId = meter?.phongTro?.phongTroId || null;
      const contractId = meter?.hopDong?.hopDongId || null;
      const period = meter?.ky || "";
      if (!roomId || !period) return null;
      try {
        const query = new URLSearchParams();
        if (contractId) query.set("hopDongId", String(contractId));
        query.set("phongTroId", String(roomId));
        query.set("kyHoaDon", period);
        const exists = await apiClient(`/api/hoa-don/exists?${query.toString()}`);
        return exists ? meterLockKey(contractId, roomId, period) : null;
      } catch (_) {
        return null;
      }
    }));

    checks.filter(Boolean).forEach((key) => {
      invoiceLockedKeys.add(key);
    });
  } catch (_) {
    invoiceLockedKeys = new Set();
  }
}

async function saveMeter() {
  const payload = buildPayload();
  if (!payload?.hopDongId) {
    alert("Vui long chon hop dong");
    return;
  }
  if (payload.chiSoMoi < payload.chiSoCu) {
    alert("Ch? s? m?i ph?i l?n hon ho?c b?ng chi so cu");
    return;
  }

  const meterId = $("meterId").value.trim();
  const path = meterId ? `/api/chi-so/${meterId}` : "/api/chi-so";
  const method = meterId ? "PUT" : "POST";

  try {
    await apiClient(path, { method, headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload) });
    closeModal();
    resetForm();
    await loadList();
  } catch (error) {
    alert(`Luu chi so th?t b?i: ${error.message}`);
  }
}

function bindFilters() {
  bindChanges?.(["filterType", "filterRoom"], () => {
    currentPage = 1;
    loadList();
  });
  bindInputs?.(["filterPeriod"], () => { currentPage = 1; loadList(); });
  $("resetFilterBtn").addEventListener("click", () => {
    $("filterType").value = "";
    $("filterRoom").value = "";
    $("filterPeriod").value = "";
    currentPage = 1;
    loadList();
  });
}

function bindPagination() {
  window.PageFilters?.bindPagination("prevPageBtn", "nextPageBtn", {
    get page() { return currentPage; },
    set page(v) { currentPage = v; },
    get totalPages() { return totalPages; },
  }, loadList);
}

function bindModal() {
  $("addMeterBtn").addEventListener("click", () => {
    resetForm();
    $("meterTitle").textContent = "Them chi so";
    openModal();
  });
  $("meterClose").addEventListener("click", closeModal);
  $("meterCancel").addEventListener("click", closeModal);
  $("meterBackdrop").addEventListener("click", closeModal);
  $("meterSave").addEventListener("click", saveMeter);
}

function bindTableActions() {
  $("meterList").addEventListener("click", async (event) => {
    const button = event.target.closest("button");
    if (!button) return;

    const meterId = Number(button.dataset.id);
    let meter = meters.find((item) => item.chiSoId === meterId);
    if (!meter) {
      try { meter = await apiClient(`/api/chi-so/${meterId}`); } catch (_) { return; }
    }
    if (!meter) return;
    if (isMeterLocked(meter)) {
      alert("Chi so ky nay da co hoa don. Hay xoa hoa don truoc khi sua/xoa chi so.");
      return;
    }

    if (button.dataset.action === "edit") {
      fillForm(meter);
      return;
    }

    if (button.dataset.action === "delete") {
      if (!confirm("Xoa chi so nay?")) return;
      try {
        await apiClient(`/api/chi-so/${meterId}`, { method: "DELETE" });
        await loadList();
      } catch (error) {
        alert(`Xoa chi so that bai: ${error.message}`);
      }
    }
  });
}

document.addEventListener("DOMContentLoaded", async () => {
  bindModal();
  bindFilters();
  bindPagination();
  bindTableActions();
  await loadMeta();
  await loadList();
});

})();


