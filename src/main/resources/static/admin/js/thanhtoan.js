(function () {
const PAGE_SIZE = 4;
const { buildOptions, textOrDash } = window.UiHelpers || {};
const { bindChanges } = window.PageFilters || {};
const { api: apiClient, money: fmtMoney, date: fmtDate, labels } = window.AppUtils || {};
const $ = (id) => document.getElementById(id);

let currentPage = 1;
let totalPages = 1;
let totalItems = 0;
let bills = [];
let failedPayments = [];
let roomNames = [];
let invoicePaymentsById = new Map();
let renderedItems = [];
let mode = "invoice";
let currentItem = null;
let pendingInvoiceId = null;

function openModal() {
  window.Modal?.open("payModal");
}

function closeModal() {
  window.Modal?.close("payModal");
  currentItem = null;
}

function renderStatusBadge(status) {
  if (status === "DA_THANH_TOAN") return '<span class="badge badge-green">Đã thanh toán</span>';
  if (status === "THAT_BAI") return '<span class="badge badge-red">Thất bại</span>';
  return '<span class="badge badge-wait">Chưa thanh toán</span>';
}

function statusText(status) {
  if (status === "DA_THANH_TOAN") return "Đã thanh toán";
  if (status === "THAT_BAI") return "Thất bại";
  return "Chưa thanh toán";
}

function toInvoiceItems() {
  return bills.map((bill) => {
    const payment = invoicePaymentsById.get(bill.hoaDonId) || null;
    const status = payment?.trangThai === "THAT_BAI"
      ? "THAT_BAI"
      : (payment?.trangThai === "THANH_CONG" || bill.trangThai === "DA_THANH_TOAN" ? "DA_THANH_TOAN" : "CHUA_THANH_TOAN");

    return {
      rowId: bill.hoaDonId,
      invoiceId: bill.hoaDonId,
      room: bill.phongTro?.tenPhong || "",
      period: bill.kyHoaDon || "",
      total: Number(bill.tongTien || 0),
      status,
      payment,
    };
  });
}

function toFailedItems() {
  return failedPayments.map((payment) => {
    const invoice = payment.hoaDon || null;
    return {
      rowId: payment.thanhToanId,
      invoiceId: invoice?.hoaDonId || null,
      room: invoice?.phongTro?.tenPhong || "",
      period: invoice?.kyHoaDon || "",
      total: Number(payment.soTien || invoice?.tongTien || 0),
      status: "THAT_BAI",
      payment,
    };
  });
}

function renderRoomFilter() {
  const currentValue = $("fPayRoom").value;
  $("fPayRoom").innerHTML = buildOptions(roomNames, (room) => room, (room) => room, "Tất cả");
  if (roomNames.includes(currentValue)) $("fPayRoom").value = currentValue;
}

function renderRow(item) {
  return `
    <tr data-id="${item.rowId}">
      <td class="name-cell">${textOrDash(item.room)}</td>
      <td>${textOrDash(item.period)}</td>
      <td>${fmtMoney(item.total)}</td>
      <td>${renderStatusBadge(item.status)}</td>
      <td>${textOrDash(fmtDate(item.payment?.ngayThanhToan))}</td>
      <td>${textOrDash(labels?.paymentMethod(item.payment?.phuongThuc) || item.payment?.phuongThuc)}</td>
      <td>
        <div class="action-group">
          <button class="btn-small btn-edit" type="button" data-act="detail" data-id="${item.rowId}">Chi tiết</button>
          ${item.invoiceId
            ? `<button class="btn-small btn-outline-card" type="button" data-act="invoice" data-id="${item.rowId}">Hóa đơn</button>`
            : '<span class="text-muted">-</span>'}
        </div>
      </td>
    </tr>`;
}

function render() {
  renderedItems = mode === "failed" ? toFailedItems() : toInvoiceItems();

  const endIndex = Math.min(currentPage * PAGE_SIZE, totalItems);
  const startIndex = totalItems ? (currentPage - 1) * PAGE_SIZE + 1 : 0;

  $("payCountText").textContent = `${totalItems} thanh toán được tìm thấy`;
  $("payPagingInfo").textContent = totalItems
    ? `Hiển thị ${startIndex} - ${endIndex} / ${totalItems} thanh toán`
    : "Hiển thị 0 thanh toán";
  $("pageInfo").textContent = `Trang ${currentPage} / ${totalPages}`;
  $("prevPageBtn").disabled = currentPage <= 1;
  $("nextPageBtn").disabled = currentPage >= totalPages;
  $("payEmptyState").style.display = totalItems ? "none" : "";
  $("payList").innerHTML = totalItems ? renderedItems.map(renderRow).join("") : "";
}

function applyIncomingQuery() {
  const params = new URLSearchParams(window.location.search);
  if (!params.toString()) return;

  const invoiceId = Number(params.get("invoiceId") || 0);
  if (invoiceId > 0) {
    pendingInvoiceId = invoiceId;
    if (!$("fPayStatus").value) {
      $("fPayStatus").value = "CHUA_THANH_TOAN";
    }
  }
  window.history.replaceState({}, document.title, "thanhtoan.html");
}

function applyPendingFocus() {
  if (!pendingInvoiceId) return;
  const item = renderedItems.find((entry) => Number(entry.invoiceId) === Number(pendingInvoiceId));
  if (!item) return;
  openDetail(item);
  pendingInvoiceId = null;
}

function openDetail(item) {
  currentItem = item;
  $("payTitle").textContent = `Chi tiết thanh toán - ${item.room || "Không rõ phòng"}`;
  $("payDetail").innerHTML = `
    <div style="line-height:1.9">
      <p><b>Phòng:</b> ${textOrDash(item.room)}</p>
      <p><b>Kỳ:</b> ${textOrDash(item.period)}</p>
      <p><b>Tổng tiền:</b> ${fmtMoney(item.total)}</p>
      <p><b>Trạng thái:</b> ${statusText(item.status)}</p>
      <p><b>Mã hóa đơn:</b> ${textOrDash(item.invoiceId)}</p>
      ${item.payment ? `
        <p><b>Ngày thanh toán:</b> ${textOrDash(fmtDate(item.payment.ngayThanhToan))}</p>
        <p><b>Phương thức:</b> ${textOrDash(labels?.paymentMethod(item.payment?.phuongThuc) || item.payment?.phuongThuc)}</p>
        <p><b>Ma GD:</b> ${textOrDash(item.payment.maGiaoDich)}</p>
      ` : ""}
    </div>`;

  $("payMarkPaid").style.display = (!item.invoiceId || item.status === "DA_THANH_TOAN") ? "none" : "";
  openModal();
}

async function loadMeta() {
  roomNames = await apiClient("/api/phong-tro/summary")
    .then((items) => [...new Set((items || []).map((room) => room.tenPhong).filter(Boolean))])
    .catch(() => []);
  renderRoomFilter();
}

async function hydrateInvoicePayments(invoiceItems) {
  const entries = await Promise.all(
    (invoiceItems || []).map(async (bill) => {
      try {
        const payment = await apiClient(`/api/thanh-toan/hoa-don/${bill.hoaDonId}`);
        return [bill.hoaDonId, payment];
      } catch (_) {
        return [bill.hoaDonId, null];
      }
    }),
  );
  invoicePaymentsById = new Map(entries);
}

async function loadList() {
  const room = $("fPayRoom").value;
  const status = $("fPayStatus").value;

  try {
    if (status === "THAT_BAI") {
      mode = "failed";
      const pageData = await window.fetchPage(apiClient, "/api/thanh-toan/search", {
        page: currentPage - 1,
        size: PAGE_SIZE,
        params: { room, status: "THAT_BAI" },
      });
      failedPayments = pageData.content || [];
      bills = [];
      invoicePaymentsById = new Map();
      totalItems = Number(pageData.totalElements || 0);
      totalPages = Math.max(1, Number(pageData.totalPages || 1));
      if (currentPage > totalPages) {
        currentPage = totalPages;
        return loadList();
      }
      render();
      applyPendingFocus();
      return;
    }

    mode = "invoice";
    const pageData = await window.fetchPage(apiClient, "/api/hoa-don/search", {
      page: currentPage - 1,
      size: PAGE_SIZE,
      params: { room, status },
    });
    bills = pageData.content || [];
    failedPayments = [];
    totalItems = Number(pageData.totalElements || 0);
    totalPages = Math.max(1, Number(pageData.totalPages || 1));
    if (currentPage > totalPages) {
      currentPage = totalPages;
      return loadList();
    }

    await hydrateInvoicePayments(bills);
  } catch (err) {
    console.error("Load thanh toán list failed", err);
    bills = [];
    failedPayments = [];
    invoicePaymentsById = new Map();
    totalItems = 0;
    totalPages = 1;
  }

  render();
  applyPendingFocus();
}

async function markPaid() {
  if (!currentItem?.invoiceId) return;

  try {
    await apiClient(`/api/thanh-toan/hoa-don/${currentItem.invoiceId}/xac-nhan`, { method: "PUT" });
    closeModal();
    await loadList();
  } catch (error) {
    alert(`Đánh dấu thất bại: ${error.message}`);
  }
}

function bindFilters() {
  bindChanges?.(["fPayRoom", "fPayStatus"], () => {
    currentPage = 1;
    loadList();
  });

  $("resetPayFilter").addEventListener("click", () => {
    $("fPayRoom").value = "";
    $("fPayStatus").value = "";
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
  $("payClose").addEventListener("click", closeModal);
  $("payCancel").addEventListener("click", closeModal);
  $("payBackdrop").addEventListener("click", closeModal);
  $("payMarkPaid").addEventListener("click", markPaid);

  $("payGoInvoice").addEventListener("click", () => {
    if (!currentItem?.invoiceId) return;
    window.location.href = `hoadon.html?invoiceId=${currentItem.invoiceId}`;
  });
}

function bindTableActions() {
  $("payList").addEventListener("click", (event) => {
    const button = event.target.closest("button");
    if (!button) return;

    const rowId = Number(button.dataset.id);
    const item = renderedItems.find((it) => Number(it.rowId) === rowId);
    if (!item) return;

    if (button.dataset.act === "detail") {
      openDetail(item);
      return;
    }

    if (button.dataset.act === "invoice" && item.invoiceId) {
      window.location.href = `hoadon.html?invoiceId=${item.invoiceId}`;
    }
  });
}

document.addEventListener("DOMContentLoaded", async () => {
  applyIncomingQuery();
  bindFilters();
  bindPagination();
  bindModal();
  bindTableActions();
  await loadMeta();
  await loadList();
});

})();

