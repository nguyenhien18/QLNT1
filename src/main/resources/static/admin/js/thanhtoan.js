const PAGE_SIZE = 4;
const { buildOptions, textOrDash } = window.UiHelpers || {};
const { bindChanges } = window.PageFilters || {};
const { api, money: fmtMoney, date: fmtDate, labels } = window.AppUtils || {};
const $ = (id) => document.getElementById(id);

let currentPage = 1;
let totalPages = 1;
let totalItems = 0;
let payments = [];
let bills = [];
let rooms = [];
let currentItem = null;

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

function toPaymentItems() {
  return bills.map((bill) => {
    const payment = payments.find((item) => item.hoaDon?.hoaDonId === bill.hoaDonId) || null;
    return {
      id: bill.hoaDonId,
      room: bill.phongTro?.tenPhong || "",
      period: bill.kyHoaDon || "",
      total: Number(bill.tongTien || 0),
      status: payment?.trangThai === "THAT_BAI"
        ? "THAT_BAI"
        : (payment?.trangThai === "THANH_CONG" || bill.trangThai === "DA_THANH_TOAN" ? "DA_THANH_TOAN" : "CHUA_THANH_TOAN"),
      payment,
      invoice: bill,
    };
  });
}

function renderRoomFilter() {
  const currentValue = $("fPayRoom").value;
  $("fPayRoom").innerHTML = buildOptions(rooms, (room) => room, (room) => room, "Tất cả");
  if (rooms.includes(currentValue)) $("fPayRoom").value = currentValue;
}

function renderRow(item) {
  return `
    <tr data-id="${item.id}">
      <td class="name-cell">${textOrDash(item.room)}</td>
      <td>${textOrDash(item.period)}</td>
      <td>${fmtMoney(item.total)}</td>
      <td>${renderStatusBadge(item.status)}</td>
      <td>${textOrDash(fmtDate(item.payment?.ngayThanhToan))}</td>
      <td>${textOrDash(labels?.paymentMethod(item.payment?.phuongThuc) || item.payment?.phuongThuc)}</td>
      <td>
        <div class="action-group">
          <button class="btn-small btn-edit" type="button" data-act="detail" data-id="${item.id}">Chi tiết</button>
          <button class="btn-small btn-outline-card" type="button" data-act="invoice" data-id="${item.id}">Hóa đơn</button>
        </div>
      </td>
    </tr>`;
}

function render() {
  const pageItems = toPaymentItems();
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
  $("payList").innerHTML = totalItems ? pageItems.map(renderRow).join("") : "";
}

function openDetail(item) {
  currentItem = item;
  $("payTitle").textContent = `Chi tiết thanh toán - ${item.room}`;
  $("payDetail").innerHTML = `
    <div style="line-height:1.9">
      <p><b>Phòng:</b> ${textOrDash(item.room)}</p>
      <p><b>Kỳ:</b> ${textOrDash(item.period)}</p>
      <p><b>Tổng tiền:</b> ${fmtMoney(item.total)}</p>
      <p><b>Trạng thái:</b> ${item.status === "DA_THANH_TOAN" ? "Đã thanh toán" : "Chưa thanh toán"}</p>
      <p><b>Mã hóa đơn:</b> ${item.invoice.hoaDonId}</p>
      ${item.payment ? `
        <p><b>Ngày thanh toán:</b> ${textOrDash(fmtDate(item.payment.ngayThanhToan))}</p>
        <p><b>Phương thức:</b> ${textOrDash(labels?.paymentMethod(item.payment?.phuongThuc) || item.payment?.phuongThuc)}</p>
        <p><b>Mã GD:</b> ${textOrDash(item.payment.maGiaoDich)}</p>
      ` : ""}
    </div>`;
  $("payMarkPaid").style.display = item.status === "DA_THANH_TOAN" ? "none" : "";
  openModal();
}

async function loadMeta() {
  [payments, rooms] = await Promise.all([
    window.fetchAllPages(api, "/api/thanh-toan", { sortBy: "ngayThanhToan", direction: "desc" }).catch(() => []),
    window.fetchAllPages(api, "/api/phong-tro", { sortBy: "tenPhong", direction: "asc" })
      .then((items) => [...new Set((items || []).map((r) => r.tenPhong).filter(Boolean))])
      .catch(() => []),
  ]);
  renderRoomFilter();
}

async function loadList() {
  const room = $("fPayRoom").value;
  const status = $("fPayStatus").value;

  try {
    if (status === "THAT_BAI") {
      const allBills = await window.fetchAllPages(api, "/api/hoa-don/search", {
        params: { room, status: "" },
      }).catch(() => []);
      const failedItems = allBills.filter((bill) => {
        const payment = payments.find((item) => item.hoaDon?.hoaDonId === bill.hoaDonId);
        return payment?.trangThai === "THAT_BAI";
      });
      totalItems = failedItems.length;
      totalPages = Math.max(1, Math.ceil(totalItems / PAGE_SIZE));
      if (currentPage > totalPages) {
        currentPage = totalPages;
      }
      const start = (currentPage - 1) * PAGE_SIZE;
      bills = failedItems.slice(start, start + PAGE_SIZE);
      render();
      return;
    }

    const pageData = await window.fetchPage(api, "/api/hoa-don/search", {
      page: currentPage - 1,
      size: PAGE_SIZE,
      params: { room, status },
    });
    bills = pageData.content || [];
    totalItems = Number(pageData.totalElements || 0);
    totalPages = Math.max(1, Number(pageData.totalPages || 1));
    if (currentPage > totalPages) {
      currentPage = totalPages;
      return loadList();
    }
  } catch (err) {
    console.error("Load thanh toán list failed", err);
    bills = [];
    totalItems = 0;
    totalPages = 1;
  }

  render();
}

async function markPaid() {
  if (!currentItem) return;

  try {
    await api(`/api/thanh-toan/hoa-don/${currentItem.invoice.hoaDonId}/xac-nhan`, { method: "PUT" });
    closeModal();
    await loadMeta();
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
    if (!currentItem) return;
    localStorage.setItem("admin_invoice_focus", String(currentItem.invoice.hoaDonId));
    window.location.href = "hoadon.html";
  });
}

function bindTableActions() {
  $("payList").addEventListener("click", (event) => {
    const button = event.target.closest("button");
    if (!button) return;

    const itemId = Number(button.dataset.id);
    const item = toPaymentItems().find((paymentItem) => paymentItem.id === itemId);
    if (!item) return;

    if (button.dataset.act === "detail") {
      openDetail(item);
      return;
    }

    if (button.dataset.act === "invoice") {
      localStorage.setItem("admin_invoice_focus", String(item.invoice.hoaDonId));
      window.location.href = "hoadon.html";
    }
  });
}

document.addEventListener("DOMContentLoaded", async () => {
  bindFilters();
  bindPagination();
  bindModal();
  bindTableActions();
  await loadMeta();
  await loadList();
});
