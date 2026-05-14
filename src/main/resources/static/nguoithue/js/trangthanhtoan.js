(function () {
const { api: apiClient, money: fmtMoney, date: fmtDate, labels } = window.AppUtils || {};
const { escapeHtml } = window.UiHelpers || {};
const { bindChanges, bindInputs, bindPagination } = window.PageFilters || {};

const tbody = document.getElementById("payTbody");
const emptyPay = document.getElementById("emptyPay");
const periodInput = document.getElementById("periodInput");
const statusSelect = document.getElementById("statusSelect");
const resetBtn = document.getElementById("resetBtn");
const countText = document.getElementById("payCountText");
const pagingInfo = document.getElementById("payPagingInfo");
const pageInfo = document.getElementById("pageInfo");
const prevBtn = document.getElementById("prevPageBtn");
const nextBtn = document.getElementById("nextPageBtn");

const detailTitle = document.getElementById("paymentDetailTitle");
const detailBody = document.getElementById("paymentDetailBody");
const closeDetailBtn = document.getElementById("closeDetailBtn");
const detailBackdrop = document.getElementById("paymentDetailBackdrop");
const detailOkBtn = document.getElementById("detailOkBtn");
const detailPayBtn = document.getElementById("detailPayBtn");

const PAGE_SIZE = 5;
let page = 1;
let totalPages = 1;
let totalItems = 0;
let invoices = [];
let paymentItems = [];
let mode = "invoice";
let currentDetailInvoiceId = null;
let currentDetailPeriod = "";
let pendingInvoiceId = null;
let pendingMode = "";
let pendingPeriod = "";

function mapPaymentStatus(payment) {
  if (payment?.trangThai === "THANH_CONG") return "DA_THANH_TOAN";
  if (payment?.trangThai === "THAT_BAI") return "THAT_BAI";
  return "CHUA_THANH_TOAN";
}

function statusBadge(v) {
  if (v === "DA_THANH_TOAN") return '<span class="status status-done">DA THANH TOAN</span>';
  if (v === "THAT_BAI") return '<span class="status status-pending">THAT BAI</span>';
  return '<span class="status status-pending">CHUA THANH TOAN</span>';
}

function getInvoiceStatus(invoice) {
  return invoice?.trangThai === "DA_THANH_TOAN" ? "DA_THANH_TOAN" : "CHUA_THANH_TOAN";
}

function buildMomoQr(invoiceId, period, amount) {
  const payload = `MOMO|INV=${invoiceId}|PERIOD=${period || ""}|AMOUNT=${Number(amount || 0)}`;
  return `https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=${encodeURIComponent(payload)}`;
}

function openPaymentPage(invoiceId, modeValue = "pay", period = "") {
  const params = new URLSearchParams({ invoiceId: String(invoiceId), mode: modeValue, period });
  window.location.href = `trangthanhtoan.html?${params.toString()}`;
}

function showDetail(invoice, payment, status) {
  const invoiceId = invoice?.hoaDonId || payment?.hoaDon?.hoaDonId;
  if (!invoiceId) return;

  const period = invoice?.kyHoaDon || payment?.hoaDon?.kyHoaDon || "";
  const amount = payment?.soTien || invoice?.tongTien || 0;
  const qrUrl = buildMomoQr(invoiceId, period, amount);
  const isPaid = status === "DA_THANH_TOAN";

  currentDetailInvoiceId = invoiceId;
  currentDetailPeriod = period;

  detailTitle.textContent = `Chi tiet thanh toan #${invoiceId}`;
  detailBody.innerHTML = `
    <div class="inv-detail-grid">
      <div class="inv-field"><span class="k">Hoa don</span><span class="v">HD${escapeHtml(invoiceId)}</span></div>
      <div class="inv-field"><span class="k">Ky hoa don</span><span class="v">${escapeHtml(period)}</span></div>
      <div class="inv-field"><span class="k">So tien</span><span class="v strong">${escapeHtml(fmtMoney(amount))}</span></div>
      <div class="inv-field"><span class="k">Trang thai</span><span class="v">${isPaid ? "Da thanh toan" : status === "THAT_BAI" ? "That bai" : "Chua thanh toan"}</span></div>
      <div class="inv-field"><span class="k">Phuong thuc</span><span class="v">${escapeHtml(labels?.paymentMethod(payment?.phuongThuc || "") || "")}</span></div>
      <div class="inv-field"><span class="k">Ngay thanh toan</span><span class="v">${escapeHtml(fmtDate(payment?.ngayThanhToan || "") || "")}</span></div>
      <div class="inv-field"><span class="k">Ma giao dich</span><span class="v">${escapeHtml(payment?.maGiaoDich || "")}</span></div>
    </div>
    <div class="inv-qr-wrap">
      <div class="inv-qr-title">QR thanh toan MoMo</div>
      <img class="inv-qr-image" src="${qrUrl}" alt="Momo QR">
      <div class="hint">${isPaid ? "Hoa don da thanh toan." : "Quet ma de thanh toan hoa don nay."}</div>
    </div>`;

  if (detailPayBtn) {
    detailPayBtn.style.display = isPaid ? "none" : "";
  }

  window.Modal?.open("paymentDetailModal");
}

function hideDetail() {
  window.Modal?.close("paymentDetailModal");
  detailBody.innerHTML = "";
  currentDetailInvoiceId = null;
  currentDetailPeriod = "";
}

function applyIncomingQuery() {
  const params = new URLSearchParams(window.location.search);
  if (!params.toString()) return;

  const invoiceId = Number(params.get("invoiceId") || 0);
  const modeValue = params.get("mode") || "";
  const period = params.get("period") || "";

  pendingMode = modeValue;
  pendingPeriod = period;
  if (invoiceId > 0) {
    pendingInvoiceId = invoiceId;
  }

  window.history.replaceState({}, document.title, "trangthanhtoan.html");
}

async function loadItems() {
  const period = periodInput.value.trim();
  const status = statusSelect.value;

  try {
    if (status === "THAT_BAI" || status === "DA_THANH_TOAN") {
      mode = "payment";
      const paymentStatus = status === "DA_THANH_TOAN" ? "DA_THANH_TOAN" : "THAT_BAI";
      const pageData = await window.fetchPage(apiClient, "/api/tenant/thanh-toan/search", {
        page: page - 1,
        size: PAGE_SIZE,
        params: { period, status: paymentStatus },
      });
      paymentItems = pageData.content || [];
      invoices = [];
      totalItems = Number(pageData.totalElements || 0);
      totalPages = Math.max(1, Number(pageData.totalPages || 1));
      if (page > totalPages) {
        page = totalPages;
        return loadItems();
      }
    } else {
      mode = "invoice";
      const invoiceStatus = status === "CHUA_THANH_TOAN" ? "CHUA_THANH_TOAN" : "";
      const pageData = await window.fetchPage(apiClient, "/api/tenant/hoa-don/search", {
        page: page - 1,
        size: PAGE_SIZE,
        params: { period, status: invoiceStatus },
      });
      invoices = pageData.content || [];
      paymentItems = [];
      totalItems = Number(pageData.totalElements || 0);
      totalPages = Math.max(1, Number(pageData.totalPages || 1));
      if (page > totalPages) {
        page = totalPages;
        return loadItems();
      }
    }
  } catch (e) {
    console.error(e);
    invoices = [];
    paymentItems = [];
    totalItems = 0;
    totalPages = 1;
  }

  render();
  await applyPendingFocus();
}

async function applyPendingFocus() {
  if (!pendingInvoiceId) return;

  const targetInvoiceId = pendingInvoiceId;
  const invoice = invoices.find((item) => item.hoaDonId === targetInvoiceId);
  if (invoice) {
    showDetail(invoice, null, getInvoiceStatus(invoice));
    pendingInvoiceId = null;
    pendingMode = "";
    pendingPeriod = "";
    return;
  }

  const payment = paymentItems.find((item) => item.hoaDon?.hoaDonId === targetInvoiceId);
  if (payment) {
    showDetail(payment.hoaDon || null, payment, mapPaymentStatus(payment));
    pendingInvoiceId = null;
    pendingMode = "";
    pendingPeriod = "";
    return;
  }

  if (pendingMode === "pay") {
    try {
      const pageData = await window.fetchPage(apiClient, "/api/tenant/hoa-don/search", {
        page: 0,
        size: 100,
        params: { period: pendingPeriod, status: "" },
      });
      const pendingInvoice = (pageData.content || []).find((item) => item.hoaDonId === targetInvoiceId);
      if (pendingInvoice) {
        showDetail(pendingInvoice, null, getInvoiceStatus(pendingInvoice));
      }
    } catch (error) {
      console.error("Khong mo duoc hoa don tu query:", error);
    }
  }

  pendingInvoiceId = null;
  pendingMode = "";
  pendingPeriod = "";
}

function renderRowsForPayments() {
  return paymentItems.map((payment) => {
    const invoice = payment.hoaDon || null;
    const status = mapPaymentStatus(payment);
    return `
      <tr>
        <td>${escapeHtml(invoice?.kyHoaDon || "")}</td>
        <td>TT${payment.thanhToanId || ""}</td>
        <td>HD${invoice?.hoaDonId || ""}</td>
        <td>${fmtMoney(payment.soTien || 0)}</td>
        <td>${escapeHtml(labels?.paymentMethod(payment.phuongThuc || "") || "")}</td>
        <td>${fmtDate(payment.ngayThanhToan) || ""}</td>
        <td>${statusBadge(status)}</td>
        <td>
          <div class="action-group">
            <button class="btn-small btn-view" type="button" data-act="detail-payment" data-id="${payment.thanhToanId}">Xem chi tiet</button>
          </div>
        </td>
      </tr>`;
  }).join("");
}

function renderRowsForInvoices() {
  return invoices.map((invoice) => {
    const status = getInvoiceStatus(invoice);
    return `
      <tr>
        <td>${escapeHtml(invoice.kyHoaDon || "")}</td>
        <td></td>
        <td>HD${invoice.hoaDonId || ""}</td>
        <td>${fmtMoney(invoice.tongTien || 0)}</td>
        <td></td>
        <td>${fmtDate(invoice.ngayLap) || ""}</td>
        <td>${statusBadge(status)}</td>
        <td>
          <div class="action-group">
            <button class="btn-small btn-view" type="button" data-act="detail-invoice" data-id="${invoice.hoaDonId}">Xem chi tiet</button>
          </div>
        </td>
      </tr>`;
  }).join("");
}

function render() {
  const start = totalItems ? (page - 1) * PAGE_SIZE + 1 : 0;
  const end = Math.min(page * PAGE_SIZE, totalItems);

  countText.textContent = `${totalItems} muc thanh toan duoc tim thay`;
  pagingInfo.textContent = totalItems ? `Hien thi ${start} - ${end} / ${totalItems} muc` : "Hien thi 0 muc";
  pageInfo.textContent = `Trang ${page} / ${totalPages}`;
  prevBtn.disabled = page <= 1;
  nextBtn.disabled = page >= totalPages;

  if (!totalItems) {
    tbody.innerHTML = "";
    emptyPay.style.display = "";
    return;
  }

  emptyPay.style.display = "none";
  tbody.innerHTML = mode === "payment"
    ? renderRowsForPayments()
    : renderRowsForInvoices();
}

document.addEventListener("DOMContentLoaded", async () => {
  applyIncomingQuery();

  bindInputs?.(["periodInput"], () => { page = 1; loadItems(); });
  bindChanges?.(["statusSelect"], () => { page = 1; hideDetail(); loadItems(); });
  resetBtn.addEventListener("click", () => {
    periodInput.value = "";
    statusSelect.value = "";
    page = 1;
    hideDetail();
    loadItems();
  });
  bindPagination?.("prevPageBtn", "nextPageBtn", {
    get page() { return page; },
    set page(v) { page = v; },
    get totalPages() { return totalPages; },
  }, loadItems);

  closeDetailBtn?.addEventListener("click", hideDetail);
  detailOkBtn?.addEventListener("click", hideDetail);
  detailBackdrop?.addEventListener("click", hideDetail);
  detailPayBtn?.addEventListener("click", () => {
    if (!currentDetailInvoiceId) return;
    hideDetail();
    openPaymentPage(currentDetailInvoiceId, "pay", currentDetailPeriod);
  });

  tbody.addEventListener("click", (event) => {
    const btn = event.target.closest("button[data-act]");
    if (!btn) return;

    if (btn.dataset.act === "detail-invoice") {
      const invoiceId = Number(btn.dataset.id);
      const invoice = invoices.find((item) => item.hoaDonId === invoiceId);
      if (!invoice) return;
      showDetail(invoice, null, getInvoiceStatus(invoice));
      return;
    }

    if (btn.dataset.act === "detail-payment") {
      const paymentId = Number(btn.dataset.id);
      const payment = paymentItems.find((item) => item.thanhToanId === paymentId);
      if (!payment) return;
      showDetail(payment.hoaDon || null, payment, mapPaymentStatus(payment));
    }
  });

  await loadItems();
});

})();
