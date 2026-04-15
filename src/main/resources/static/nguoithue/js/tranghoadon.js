const { api, money: fmtMoney, date: fmtDate } = window.AppUtils || {};
const { escapeHtml } = window.UiHelpers || {};
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
let payments = [];
let currentDetailInvoice = null;

const detailModal = document.getElementById("invoiceDetailModal");
const detailBody = document.getElementById("invoiceDetailBody");
const detailPayBtn = document.getElementById("invoiceDetailPay");
const detailCloseBtn = document.getElementById("invoiceDetailClose");
const detailOkBtn = document.getElementById("invoiceDetailOk");
const detailBackdrop = document.getElementById("invoiceDetailBackdrop");

function paymentOfInvoice(invoiceId) {
  return payments.find((payment) => payment.hoaDon?.hoaDonId === invoiceId) || null;
}

function normalizedStatus(inv) {
  const payment = paymentOfInvoice(inv.hoaDonId);
  if (inv.trangThai === "DA_THANH_TOAN" || payment?.trangThai === "THANH_CONG") return "DA_THANH_TOAN";
  return "CHUA_THANH_TOAN";
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
      <div class="inv-field"><span class="k">Phòng</span><span class="v">${escapeHtml(inv.phongTro?.tenPhong || "")}</span></div>
      <div class="inv-field"><span class="k">Mã hóa đơn</span><span class="v">HD${inv.hoaDonId || ""}</span></div>
      <div class="inv-field"><span class="k">Kỳ hóa đơn</span><span class="v">${escapeHtml(inv.kyHoaDon || "")}</span></div>
      <div class="inv-field"><span class="k">Ngày lập</span><span class="v">${escapeHtml(fmtDate(inv.ngayLap) || "")}</span></div>
      <div class="inv-field"><span class="k">Tổng tiền</span><span class="v strong">${escapeHtml(fmtMoney(inv.tongTien))}</span></div>
      <div class="inv-field"><span class="k">Trạng thái</span><span class="v">${paid ? "Đã thanh toán" : "Chưa thanh toán"}</span></div>
    </div>
    <div class="inv-qr-wrap">
      <div class="inv-qr-title">QR thanh toán MoMo</div>
      <img class="inv-qr-image" src="${qrUrl}" alt="Momo QR">
      <div class="hint">${paid ? "Hóa đơn đã thanh toán." : "Quét mã để thanh toán hóa đơn này."}</div>
    </div>`;
  detailPayBtn.style.display = paid ? "none" : "";
  detailModal?.classList.add("show");
  detailModal?.setAttribute("aria-hidden", "false");
}

function closeDetailModal() {
  detailModal?.classList.remove("show");
  detailModal?.setAttribute("aria-hidden", "true");
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

async function loadPayments() {
  payments = await window.fetchAllPages(api, "/api/tenant/thanh-toan", { sortBy: "ngayThanhToan", direction: "desc" }).catch(() => []);
}

async function loadInvoices() {
  const period = periodInput.value.trim();
  const status = statusSelect.value;
  try {
    const pageData = await window.fetchPage(api, "/api/tenant/hoa-don/search", {
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
  periodInput.addEventListener("input", () => { page = 1; loadInvoices(); });
  statusSelect.addEventListener("change", () => { page = 1; loadInvoices(); });
  resetBtn.addEventListener("click", () => {
    periodInput.value = "";
    statusSelect.value = "";
    page = 1;
    loadInvoices();
  });
  prevBtn.addEventListener("click", () => { if (page > 1) { page -= 1; loadInvoices(); } });
  nextBtn.addEventListener("click", () => { if (page < totalPages) { page += 1; loadInvoices(); } });
  detailCloseBtn?.addEventListener("click", closeDetailModal);
  detailOkBtn?.addEventListener("click", closeDetailModal);
  detailBackdrop?.addEventListener("click", closeDetailModal);
  detailPayBtn?.addEventListener("click", () => {
    if (!currentDetailInvoice) return;
    closeDetailModal();
    openPaymentPage(currentDetailInvoice.hoaDonId, "pay");
  });

  await loadPayments();
  await loadInvoices();
});
