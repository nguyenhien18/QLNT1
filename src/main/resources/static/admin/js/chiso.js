const PAGE_SIZE = 4;
const { buildOptions, textOrDash } = window.UiHelpers || {};
const { bindChanges, bindInputs } = window.PageFilters || {};
const { api, money: fmtMoney, date: fmtDate, todayISO, labels } = window.AppUtils || {};
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
function getTypeLabel(type) { return labels?.meterType(type) || (type === "DIEN" ? "Điện" : "Nước"); }
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

function renderOptions() {
  const selectableContracts = (contracts || [])
    .filter((c) => c?.trangThai === "CON_HIEU_LUC")
    .filter((c) => !isExpiredContract(c));
  $("meterContract").innerHTML = '<option value="">-- Chọn hợp đồng --</option>' + selectableContracts
    .map((c) => {
      const room = c?.phongTro?.tenPhong || "-";
      const tenant = c?.khachThue?.tenDangNhap || c?.khachThue?.hoTen || "-";
      return `<option value="${c.hopDongId}">#${c.hopDongId} - ${room} - ${tenant}</option>`;
    })
    .join("");
  $("filterRoom").innerHTML = buildOptions(rooms, (room) => room.tenPhong, (room) => room.tenPhong, "Tất cả");
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
          <button class="btn-small btn-edit" type="button" data-action="edit" data-id="${meter.chiSoId}">Sửa</button>
          <button class="btn-small btn-delete" type="button" data-action="delete" data-id="${meter.chiSoId}">Xóa</button>
        </div>
      </td>
    </tr>`;
}

function render() {
  const startIndex = totalItems ? (currentPage - 1) * PAGE_SIZE + 1 : 0;
  const endIndex = Math.min(currentPage * PAGE_SIZE, totalItems);

  $("meterCountText").textContent = `${totalItems} chỉ số được tìm thấy`;
  $("meterPagingInfo").textContent = totalItems ? `Hiển thị ${startIndex} - ${endIndex} / ${totalItems} chỉ số` : "Hiển thị 0 chỉ số";
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
  $("meterTitle").textContent = "Cập nhật chỉ số";
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
  return {
    hopDong: { hopDongId: contractId },
    phongTro: contract?.phongTro?.phongTroId ? { phongTroId: Number(contract.phongTro.phongTroId) } : undefined,
    loai: $("meterType").value,
    ky: $("meterPeriod").value.trim(),
    thoiDiem: $("meterDate").value,
    chiSoCu: Number($("meterOld").value || 0),
    chiSoMoi: Number($("meterNew").value || 0),
    donGia: Number($("meterUnitPrice").value || 0),
  };
}

async function loadMeta() {
  [rooms, contracts] = await Promise.all([
    window.fetchAllPages(api, "/api/phong-tro", { sortBy: "phongTroId", direction: "desc" }).catch(() => []),
    window.fetchAllPages(api, "/api/hop-dong", { sortBy: "ngayBatDau", direction: "desc" }).catch(() => []),
  ]);
  renderOptions();
}

function inferContractIdForMeter(meter) {
  const roomId = meter?.phongTro?.phongTroId;
  if (!roomId) return "";
  const meterDate = meter?.thoiDiem ? new Date(meter.thoiDiem) : null;
  const matched = (contracts || [])
    .filter((c) => c?.trangThai !== "HUY")
    .filter((c) => c?.phongTro?.phongTroId === roomId)
    .filter((c) => {
      if (!meterDate) return true;
      const start = c?.ngayBatDau ? new Date(c.ngayBatDau) : null;
      const end = c?.ngayKetThuc ? new Date(c.ngayKetThuc) : null;
      const afterStart = !start || meterDate >= start;
      const beforeEnd = !end || meterDate <= end;
      return afterStart && beforeEnd;
    })
    .sort((a, b) => String(b?.ngayBatDau || "").localeCompare(String(a?.ngayBatDau || "")));
  return matched[0]?.hopDongId || "";
}

async function loadList() {
  const type = $("filterType").value;
  const room = $("filterRoom").value;
  const period = $("filterPeriod").value.trim();

  try {
    const pageData = await window.fetchPage(api, "/api/chi-so/search", {
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
    console.error("Load chỉ số search failed", error);
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

  const roomIds = [...new Set(meters.map((m) => m?.phongTro?.phongTroId).filter(Boolean))];
  if (!roomIds.length) return;

  try {
    const invoiceByRoom = await Promise.all(
      roomIds.map((roomId) =>
        window.fetchAllPages(api, `/api/hoa-don/phong/${roomId}`, { sortBy: "ngayLap", direction: "desc" }).catch(() => [])
      )
    );
    invoiceByRoom.forEach((items, idx) => {
      const roomId = roomIds[idx];
      (items || []).forEach((bill) => {
        if (!bill?.kyHoaDon) return;
        const contractId = bill?.hopDong?.hopDongId || null;
        invoiceLockedKeys.add(meterLockKey(contractId, roomId, bill.kyHoaDon));
        invoiceLockedKeys.add(meterLockKey(null, roomId, bill.kyHoaDon));
      });
    });
  } catch (_) {
    invoiceLockedKeys = new Set();
  }
}

async function saveMeter() {
  const payload = buildPayload();
  if (!payload?.hopDong?.hopDongId) {
    alert("Vui lòng chọn hợp đồng");
    return;
  }
  if (payload.chiSoMoi < payload.chiSoCu) {
    alert("Chỉ số mới phải lớn hơn hoặc bằng chỉ số cũ");
    return;
  }

  const meterId = $("meterId").value.trim();
  const path = meterId ? `/api/chi-so/${meterId}` : "/api/chi-so";
  const method = meterId ? "PUT" : "POST";

  try {
    await api(path, { method, headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload) });
    closeModal();
    resetForm();
    await loadList();
  } catch (error) {
    alert(`Lưu chỉ số thất bại: ${error.message}`);
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
    $("meterTitle").textContent = "Thêm chỉ số";
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
      try { meter = await api(`/api/chi-so/${meterId}`); } catch (_) { return; }
    }
    if (!meter) return;
    if (isMeterLocked(meter)) {
      alert("Chỉ số kỳ này đã có hóa đơn. Hãy xóa hóa đơn trước khi sửa/xóa chỉ số.");
      return;
    }

    if (button.dataset.action === "edit") {
      fillForm(meter);
      return;
    }

    if (button.dataset.action === "delete") {
      if (!confirm("Xóa chỉ số này?")) return;
      try {
        await api(`/api/chi-so/${meterId}`, { method: "DELETE" });
        await loadList();
      } catch (error) {
        alert(`Xóa chỉ số thất bại: ${error.message}`);
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
