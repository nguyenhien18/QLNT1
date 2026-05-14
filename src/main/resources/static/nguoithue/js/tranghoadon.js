(function () {
const { api: apiClient, money: fmtMoney, date: fmtDate } = window.AppUtils || {};
const { escapeHtml } = window.UiHelpers || {};
const { bindChanges, bindInputs, bindPagination } = window.PageFilters || {};
const periodInput = document.getElementById("periodInput");
const statusSelect = document.getElementById("statusSelect");
const resetBtn = document.getElementById("resetBtn");
const invoiceTbody = document.getElementById("invoiceTbody");
const emptyState = document.getElementById("emptyState");
const countText = document.getElementById("invoiceCountText");
const pagingInfo = document.getElementById("invoicePagingInfo");
const pageInfo = document.getElementById("pageInfo");
const prevBtn = document.getElementById("prevPageBtn");
const nextBtn = document.getElementById("nextPageBtn");

const PAGE_SIZE = 5;
let page = 1;
let totalPages = 1;
let totalItems = 0;
let invoices = [];
let currentDetailInvoice = null;

const detailBody = document.getElementById("invoiceDetailBody");
const detailPayBtn = document.getElementById("invoiceDetailPay");
const detailCloseBtn = document.getElementById("invoiceDetailClose");
const detailOkBtn = document.getElementById("invoiceDetailOk");
const detailBackdrop = document.getElementById("invoiceDetailBackdrop");

function normalizedStatus(inv) {
  return inv?.trangThai === "DA_THANH_TOAN" ? "DA_THANH_TOAN" : "CHUA_THANH_TOAN";
}

function statusBadge(inv) {
  return normalizedStatus(inv) === "DA_THANH_TOAN"
    ? '<span class="status status-done">DA THANH TOAN</span>'
    : '<span class="status status-pending">CHUA THANH TOAN</span>';
}

function openPaymentPage(invoiceId, mode = "pay") {
  const inv = invoices.find((item) => item.hoaDonId === Number(invoiceId));
  if (!inv) {
    alert("Khong tim thay hoa don.");
    return;
  }
  const params = new URLSearchParams({ invoiceId: String(inv.hoaDonId), mode, period: inv.kyHoaDon || "" });
  window.location.href = `trangthanhtoan.html?${params.toString()}`;
}

function openDetailModal(inv) {
  currentDetailInvoice = inv;
  const paid = normalizedStatus(inv) === "DA_THANH_TOAN";
  const qrUrl = buildMomoQr(inv);
  detailBody.innerHTML = `
    <div class="inv-detail-grid">
      <div class="inv-field"><span class="k">Phong</span><span class="v">${escapeHtml(inv.phongTro?.tenPhong || "")}</span></div>
      <div class="inv-field"><span class="k">Ma hoa don</span><span class="v">HD${inv.hoaDonId || ""}</span></div>
      <div class="inv-field"><span class="k">Ky hoa don</span><span class="v">${escapeHtml(inv.kyHoaDon || "")}</span></div>
      <div class="inv-field"><span class="k">Ngay lap</span><span class="v">${escapeHtml(fmtDate(inv.ngayLap) || "")}</span></div>
      <div class="inv-field"><span class="k">Tong tien</span><span class="v strong">${escapeHtml(fmtMoney(inv.tongTien))}</span></div>
      <div class="inv-field"><span class="k">Trang thai</span><span class="v">${paid ? "Da thanh toan" : "Chua thanh toan"}</span></div>
    </div>
    <div class="inv-qr-wrap">
      <div class="inv-qr-title">QR thanh toan MoMo</div>
      <img class="inv-qr-image" src="${qrUrl}" alt="Momo QR">
      <div class="hint">${paid ? "Hoa don da thanh toan." : "Quet ma de thanh toan hoa don nay."}</div>
    </div>`;
  detailPayBtn.style.display = paid ? "none" : "";
  window.Modal?.open("invoiceDetailModal");
}

function closeDetailModal() {
  window.Modal?.close("invoiceDetailModal");
  detailBody.innerHTML = "";
  currentDetailInvoice = null;
}

function buildMomoQr(inv) {
  const amount = Number(inv?.tongTien || 0);
  const invoiceId = inv?.hoaDonId || "";
  const room = inv?.phongTro?.tenPhong || "";
  const period = inv?.kyHoaDon || "";
  const payload = `MOMO|INV=${invoiceId}|ROOM=${room}|PERIOD=${period}|AMOUNT=${amount}`;
  return `https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=${encodeURIComponent(payload)}`;
}

async function loadInvoices() {
  const period = periodInput.value.trim();
  const status = statusSelect.value;
  try {
    const pageData = await window.fetchPage(apiClient, "/api/tenant/hoa-don/search", {
      page: page - 1,
      size: PAGE_SIZE,
      params: { period, status },
    });
    invoices = pageData.content || [];
    totalItems = Number(pageData.totalElements || 0);
    totalPages = Math.max(1, Number(pageData.totalPages || 1));
    if (page > totalPages) {
      page = totalPages;
      return loadInvoices();
    }
    render();
  } catch (e) {
    console.error("Load hoa don failed", e);
    invoices = [];
    totalItems = 0;
    totalPages = 1;
    render();
  }
}

function row(inv) {
  const canPay = normalizedStatus(inv) !== "DA_THANH_TOAN";
  return `
    <tr>
      <td>${escapeHtml(inv.kyHoaDon || "")}</td>
      <td>HD${inv.hoaDonId || ""}</td>
      <td>${escapeHtml(inv.phongTro?.tenPhong || "")}</td>
      <td>${escapeHtml(fmtDate(inv.ngayLap) || "")}</td>
      <td>${escapeHtml(fmtMoney(inv.tongTien))}</td>
      <td>${statusBadge(inv)}</td>
      <td>
        <div class="action-group">
          <button class="btn-small btn-view" type="button" data-act="detail" data-id="${inv.hoaDonId}">Xem chi tiet</button>
          ${canPay
            ? `<button class="btn-small btn-pay" type="button" data-act="pay" data-id="${inv.hoaDonId}">Thanh toan</button>`
            : `<button class="btn-small btn-paid" type="button" disabled>Da thanh toan</button>`}
        </div>
      </td>
    </tr>`;
}

function render() {
  const start = totalItems ? (page - 1) * PAGE_SIZE + 1 : 0;
  const end = Math.min(page * PAGE_SIZE, totalItems);

  countText.textContent = `${totalItems} hoa don duoc tim thay`;
  pagingInfo.textContent = totalItems ? `Hien thi ${start} - ${end} / ${totalItems} hoa don` : "Hien thi 0 hoa don";
  pageInfo.textContent = `Trang ${page} / ${totalPages}`;
  prevBtn.disabled = page <= 1;
  nextBtn.disabled = page >= totalPages;

  if (!totalItems) {
    invoiceTbody.innerHTML = "";
    emptyState.style.display = "";
    return;
  }

  emptyState.style.display = "none";
  invoiceTbody.innerHTML = invoices.map(row).join("");
}

document.addEventListener("click", (e) => {
  const btn = e.target.closest("[data-act]");
  if (!btn) return;
  const id = Number(btn.dataset.id);
  const inv = invoices.find((x) => x.hoaDonId === id);
  if (!inv) return;
  if (btn.dataset.act === "pay" && normalizedStatus(inv) !== "DA_THANH_TOAN") openPaymentPage(inv.hoaDonId, "pay");
  if (btn.dataset.act === "detail") openDetailModal(inv);
});

document.addEventListener("DOMContentLoaded", async () => {
  bindInputs?.(["periodInput"], () => { page = 1; loadInvoices(); });
  bindChanges?.(["statusSelect"], () => { page = 1; loadInvoices(); });
  resetBtn.addEventListener("click", () => {
    periodInput.value = "";
    statusSelect.value = "";
    page = 1;
    loadInvoices();
  });
  bindPagination?.("prevPageBtn", "nextPageBtn", {
    get page() { return page; },
    set page(v) { page = v; },
    get totalPages() { return totalPages; },
  }, loadInvoices);
  detailCloseBtn?.addEventListener("click", closeDetailModal);
  detailOkBtn?.addEventListener("click", closeDetailModal);
  detailBackdrop?.addEventListener("click", closeDetailModal);
  detailPayBtn?.addEventListener("click", () => {
    if (!currentDetailInvoice) return;
    closeDetailModal();
    openPaymentPage(currentDetailInvoice.hoaDonId, "pay");
  });

  await loadInvoices();
});

})();
