(function () {
const PAGE_SIZE = 4;
const { buildOptions, textOrDash } = window.UiHelpers || {};
const { bindChanges, bindInputs } = window.PageFilters || {};
const { api: apiClient, money: fmtMoney, date: fmtDate, todayISO, currentPeriod } = window.AppUtils || {};
const $ = (id) => document.getElementById(id);

let currentPage = 1;
let totalPages = 1;
let totalItems = 0;
let bills = [];
let activeContracts = [];
let roomNames = [];
let preview = null;

function openModal(element) {
  window.Modal?.open(element);
}

function closeModal(element) {
  window.Modal?.close(element);
}

function totalOf(bill) {
  return Number(bill.tongTien || 0);
}

function renderContractOptions() {
  $("invRoom").innerHTML = buildOptions(
    activeContracts,
    (contract) => contract.phongTro.phongTroId,
    (contract) => `${contract.phongTro.tenPhong} - ${contract.khachThue?.hoTen || ""}`,
    "-- Chon phong --",
  );
}

function isExpiredContract(contract) {
  return !!(contract?.ngayKetThuc && contract.ngayKetThuc < todayISO());
}

function renderRoomFilters() {
  $("fRoom").innerHTML = buildOptions(roomNames, (room) => room, (room) => room, "Tat ca");
}

function renderInvoiceRow(bill) {
  const isPaid = bill.trangThai === "DA_THANH_TOAN";
  return `
    <tr data-id="${bill.hoaDonId}">
      <td class="name-cell">${textOrDash(bill.phongTro?.tenPhong)}</td>
      <td>${textOrDash(bill.kyHoaDon)}</td>
      <td>${textOrDash(fmtDate(bill.ngayLap))}</td>
      <td>${fmtMoney(totalOf(bill))}</td>
      <td><span class="badge ${isPaid ? "badge-paid" : "badge-wait"}">${isPaid ? "Da thanh toan" : "Chua thanh toan"}</span></td>
      <td>
        <div class="action-group">
          <button class="btn-small btn-edit" data-act="detail" data-id="${bill.hoaDonId}">Chi tiet</button>
          <button class="btn-small btn-outline-card" data-act="payment" data-id="${bill.hoaDonId}">Thanh toan</button>
          <button class="btn-small btn-delete" data-act="delete" data-id="${bill.hoaDonId}">Xoa</button>
        </div>
      </td>
    </tr>`;
}

function render() {
  const startIndex = totalItems ? (currentPage - 1) * PAGE_SIZE + 1 : 0;
  const endIndex = Math.min(currentPage * PAGE_SIZE, totalItems);

  $("invoiceCountText").textContent = `${totalItems} hoa don duoc tim thay`;
  $("invoicePagingInfo").textContent = totalItems
    ? `Hien thi ${startIndex} - ${endIndex} / ${totalItems} hoa don`
    : "Hien thi 0 hoa don";
  $("pageInfo").textContent = `Trang ${currentPage} / ${totalPages}`;
  $("prevPageBtn").disabled = currentPage <= 1;
  $("nextPageBtn").disabled = currentPage >= totalPages;
  $("emptyState").style.display = totalItems ? "none" : "block";
  $("invoiceList").innerHTML = totalItems ? bills.map(renderInvoiceRow).join("") : "";
}

function resetPreview() {
  preview = null;
  $("invContractId").value = "";
  $("previewRep").textContent = "-";
  $("previewRent").textContent = "0 d";
  $("previewElec").textContent = "0 d";
  $("previewWater").textContent = "0 d";
  $("previewService").textContent = "0 d";
  $("previewTotal").textContent = "0 d";
}

function fillPreview() {
  $("invContractId").value = preview.hopDongId || "";
  $("previewRep").textContent = preview.daiDien || "-";
  $("previewRent").textContent = fmtMoney(preview.tienPhong);
  $("previewElec").textContent = fmtMoney(preview.tienDien);
  $("previewWater").textContent = fmtMoney(preview.tienNuoc);
  $("previewService").textContent = fmtMoney(preview.tienDichVu);
  $("previewTotal").textContent = fmtMoney(preview.tongTien);
}

async function refreshPreview() {
  const roomId = Number($("invRoom").value || 0);
  const period = $("invPeriod").value.trim();
  if (!roomId || !period) {
    resetPreview();
    return;
  }

  try {
    preview = await apiClient(`/api/hoa-don/preview?phongTroId=${roomId}&kyHoaDon=${encodeURIComponent(period)}`);
    fillPreview();
  } catch (error) {
    resetPreview();
    alert(`Khong the lay du lieu hoa don tu dong: ${error.message}`);
  }
}

function openCreateModal() {
  $("invoiceForm").reset();
  $("invDate").value = todayISO();
  $("invPeriod").value = currentPeriod();
  $("invStatus").value = "UNPAID";
  resetPreview();
  refreshPreview();
  openModal($("invoiceModal"));
}

async function applyInvoiceFocus() {
  const params = new URLSearchParams(window.location.search);
  const rawInvoiceId = params.get("invoiceId");
  if (!rawInvoiceId) return;

  const focusId = Number(rawInvoiceId);
  
  if (!focusId) return;

  window.history.replaceState({}, document.title, "hoadon.html");

  try {
    const bill = await apiClient(`/api/hoa-don/${focusId}`);
    openDetail(bill);
  } catch (_) {}
}

function openDetail(bill) {
  const isPaid = bill.trangThai === "DA_THANH_TOAN";
  $("detailBody").innerHTML = `
    <p><b>Phong:</b> ${textOrDash(bill.phongTro?.tenPhong)}</p>
    <p><b>Ky hoa don:</b> ${textOrDash(bill.kyHoaDon)}</p>
    <p><b>Ngay lap:</b> ${textOrDash(fmtDate(bill.ngayLap))}</p>
    <p><b>Tien phong:</b> ${fmtMoney(bill.tienPhong)}</p>
    <p><b>Tien dien:</b> ${fmtMoney(bill.tienDien)}</p>
    <p><b>Tien nuoc:</b> ${fmtMoney(bill.tienNuoc)}</p>
    <p><b>Tien dich vu:</b> ${fmtMoney(bill.tienDichVu)}</p>
    <p><b>Tong:</b> ${fmtMoney(bill.tongTien)}</p>
    <p><b>Trang thai:</b> ${isPaid ? "Da thanh toan" : "Chua thanh toan"}</p>`;
  openModal($("detailModal"));
}

async function loadMeta() {
  const [contractPage, roomSummary] = await Promise.all([
    window.fetchPage(apiClient, "/api/hop-dong/trang-thai/CON_HIEU_LUC", {
      page: 0,
      size: 1000,
      sortBy: "ngayBatDau",
      direction: "desc",
    }).catch(() => ({ content: [] })),
    apiClient("/api/phong-tro/summary").catch(() => []),
  ]);

  activeContracts = (contractPage.content || [])
    .filter((contract) => !isExpiredContract(contract));

  roomNames = [...new Set((roomSummary || []).map((room) => room.tenPhong).filter(Boolean))];

  renderContractOptions();
  renderRoomFilters();
}

async function loadList() {
  const status = $("fStatus").value;
  const room = $("fRoom").value;
  const period = $("fPeriod").value.trim();

  try {
    const pageData = await window.fetchPage(apiClient, "/api/hoa-don/search", {
      page: currentPage - 1,
      size: PAGE_SIZE,
      params: { status, room, period },
    });
    bills = pageData.content || [];
    totalItems = Number(pageData.totalElements || 0);
    totalPages = Math.max(1, Number(pageData.totalPages || 1));
    if (currentPage > totalPages) {
      currentPage = totalPages;
      return loadList();
    }
  } catch (err) {
    console.error("Load hoa don search failed", err);
    bills = [];
    totalItems = 0;
    totalPages = 1;
  }

  render();
}

async function saveInvoice() {
  const contractId = Number($("invContractId").value || 0);
  if (!contractId || !preview) {
    alert("Vui long chon phong co hop dong hieu luc va ky hoa don hop le");
    return;
  }

  const payload = {
    hopDongId: contractId,
    kyHoaDon: $("invPeriod").value.trim(),
    ngayLap: $("invDate").value,
    // UI only allows creating unpaid invoice.
    trangThai: "CHUA_THANH_TOAN",
  };

  try {
    await apiClient("/api/hoa-don", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    closeModal($("invoiceModal"));
    await loadMeta();
    await loadList();
  } catch (error) {
    alert(`Tao hoa don that bai: ${error.message}`);
  }
}

function bindFilters() {
  bindChanges?.(["fStatus", "fRoom"], () => { currentPage = 1; loadList(); });
  bindInputs?.(["fPeriod"], () => { currentPage = 1; loadList(); });
  $("btnResetFilter")?.addEventListener("click", () => {
    $("fStatus").value = "";
    $("fRoom").value = "";
    $("fPeriod").value = "";
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

function bindModals() {
  $("btnCreateInvoice")?.addEventListener("click", openCreateModal);
  $("invoiceClose")?.addEventListener("click", () => closeModal($("invoiceModal")));
  $("invoiceCancel")?.addEventListener("click", () => closeModal($("invoiceModal")));
  $("invoiceBackdrop")?.addEventListener("click", () => closeModal($("invoiceModal")));
  $("detailClose")?.addEventListener("click", () => closeModal($("detailModal")));
  $("detailOk")?.addEventListener("click", () => closeModal($("detailModal")));
  $("detailBackdrop")?.addEventListener("click", () => closeModal($("detailModal")));
  $("invoiceSave")?.addEventListener("click", saveInvoice);
  $("invRoom")?.addEventListener("change", refreshPreview);
  $("invPeriod")?.addEventListener("input", refreshPreview);
}

function bindTableActions() {
  $("invoiceList")?.addEventListener("click", async (event) => {
    const button = event.target.closest("button");
    if (!button) return;

    const invoiceId = Number(button.dataset.id);
    let bill = bills.find((item) => item.hoaDonId === invoiceId);
    if (!bill) {
      try {
        bill = await apiClient(`/api/hoa-don/${invoiceId}`);
      } catch (_) {
        return;
      }
    }
    if (!bill) return;

    if (button.dataset.act === "detail") {
      openDetail(bill);
      return;
    }

    if (button.dataset.act === "payment") {
      if (bill.trangThai === "DA_THANH_TOAN") {
        alert("Hoa don nay da thanh toan.");
        return;
      }
      window.location.href = `thanhtoan.html?invoiceId=${invoiceId}`;
      return;
    }

    if (button.dataset.act === "delete") {
      if (!confirm("Xoa hoa don nay?")) return;
      try {
        await apiClient(`/api/hoa-don/${invoiceId}`, { method: "DELETE" });
        await loadMeta();
        await loadList();
      } catch (error) {
        alert(`Xoa hoa don that bai: ${error.message}`);
      }
    }
  });
}

document.addEventListener("DOMContentLoaded", async () => {
  bindModals();
  bindFilters();
  bindPagination();
  bindTableActions();
  await loadMeta();
  await loadList();
  await applyInvoiceFocus();
});


})();
