const { api, money: fmtMoney, date: fmtDate, labels } = window.AppUtils || {};
const { escapeHtml } = window.UiHelpers || {};

const tbody = document.getElementById('payTbody');
const emptyPay = document.getElementById('emptyPay');
const periodInput = document.getElementById('periodInput');
const statusSelect = document.getElementById('statusSelect');
const resetBtn = document.getElementById('resetBtn');
const countText = document.getElementById('payCountText');
const pagingInfo = document.getElementById('payPagingInfo');
const pageInfo = document.getElementById('pageInfo');
const prevBtn = document.getElementById('prevPageBtn');
const nextBtn = document.getElementById('nextPageBtn');

const detailModal = document.getElementById('paymentDetailModal');
const detailTitle = document.getElementById('paymentDetailTitle');
const detailBody = document.getElementById('paymentDetailBody');
const closeDetailBtn = document.getElementById('closeDetailBtn');
const detailBackdrop = document.getElementById('paymentDetailBackdrop');
const detailOkBtn = document.getElementById('detailOkBtn');
const detailPayBtn = document.getElementById('detailPayBtn');

const PAGE_SIZE = 5;
let page = 1;
let totalPages = 1;
let totalItems = 0;
let invoices = [];
let payments = [];
let failedPayments = [];
let currentDetailInvoiceId = null;
let currentDetailPeriod = '';

function paymentOfInvoice(invoiceId) {
  return payments.find((item) => item.hoaDon?.hoaDonId === invoiceId) || null;
}

function statusBadge(v) {
  if (v === 'DA_THANH_TOAN') return '<span class="status status-done">DA THANH TOAN</span>';
  if (v === 'THAT_BAI') return '<span class="status status-pending">THAT BAI</span>';
  return '<span class="status status-pending">CHUA THANH TOAN</span>';
}

function getInvoiceStatus(invoice, payment) {
  if (invoice?.trangThai === 'DA_THANH_TOAN' || payment?.trangThai === 'THANH_CONG') return 'DA_THANH_TOAN';
  if (payment?.trangThai === 'THAT_BAI') return 'THAT_BAI';
  return 'CHUA_THANH_TOAN';
}

function buildMomoQr(invoiceId, period, amount) {
  const payload = `MOMO|INV=${invoiceId}|PERIOD=${period || ''}|AMOUNT=${Number(amount || 0)}`;
  return `https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=${encodeURIComponent(payload)}`;
}

function openPaymentPage(invoiceId, mode = 'pay', period = '') {
  const params = new URLSearchParams({ invoiceId: String(invoiceId), mode, period });
  window.location.href = `trangthanhtoan.html?${params.toString()}`;
}

function showDetail(invoice, payment, status) {
  const invoiceId = invoice?.hoaDonId || payment?.hoaDon?.hoaDonId;
  if (!invoiceId) return;

  const period = invoice?.kyHoaDon || payment?.hoaDon?.kyHoaDon || '';
  const amount = payment?.soTien || invoice?.tongTien || 0;
  const qrUrl = buildMomoQr(invoiceId, period, amount);
  const isPaid = status === 'DA_THANH_TOAN';

  currentDetailInvoiceId = invoiceId;
  currentDetailPeriod = period;

  detailTitle.textContent = `Chi tiet thanh toan #${invoiceId}`;
  detailBody.innerHTML = `
    <div class="inv-detail-grid">
      <div class="inv-field"><span class="k">Hoa don</span><span class="v">HD${escapeHtml(invoiceId)}</span></div>
      <div class="inv-field"><span class="k">Ky hoa don</span><span class="v">${escapeHtml(period)}</span></div>
      <div class="inv-field"><span class="k">So tien</span><span class="v strong">${escapeHtml(fmtMoney(amount))}</span></div>
      <div class="inv-field"><span class="k">Trang thai</span><span class="v">${isPaid ? 'Da thanh toan' : status === 'THAT_BAI' ? 'That bai' : 'Chua thanh toan'}</span></div>
      <div class="inv-field"><span class="k">Phuong thuc</span><span class="v">${escapeHtml(labels?.paymentMethod(payment?.phuongThuc || '') || '')}</span></div>
      <div class="inv-field"><span class="k">Ngay thanh toan</span><span class="v">${escapeHtml(fmtDate(payment?.ngayThanhToan || '') || '')}</span></div>
      <div class="inv-field"><span class="k">Ma giao dich</span><span class="v">${escapeHtml(payment?.maGiaoDich || '')}</span></div>
    </div>
    <div class="inv-qr-wrap">
      <div class="inv-qr-title">QR thanh toan MoMo</div>
      <img class="inv-qr-image" src="${qrUrl}" alt="Momo QR">
      <div class="hint">${isPaid ? 'Hoa don da thanh toan.' : 'Quet ma de thanh toan hoa don nay.'}</div>
    </div>`;

  if (detailPayBtn) {
    detailPayBtn.style.display = isPaid ? 'none' : '';
  }

  detailModal?.classList.add('show');
  detailModal?.setAttribute('aria-hidden', 'false');
}

function hideDetail() {
  detailModal?.classList.remove('show');
  detailModal?.setAttribute('aria-hidden', 'true');
  detailBody.innerHTML = '';
  currentDetailInvoiceId = null;
  currentDetailPeriod = '';
}

async function loadPayments() {
  payments = await window.fetchAllPages(api, '/api/tenant/thanh-toan', { sortBy: 'ngayThanhToan', direction: 'desc' }).catch(() => []);
}

async function loadItems() {
  const period = periodInput.value.trim();
  const status = statusSelect.value;

  if (status === 'THAT_BAI') {
    try {
      const pageData = await window.fetchPage(api, '/api/tenant/thanh-toan/search', {
        page: page - 1,
        size: PAGE_SIZE,
        params: { period, status: 'THAT_BAI' }
      });
      failedPayments = pageData.content || [];
      invoices = [];
      totalItems = Number(pageData.totalElements || 0);
      totalPages = Math.max(1, Number(pageData.totalPages || 1));
      if (page > totalPages) {
        page = totalPages;
        return loadItems();
      }
    } catch (e) {
      console.error(e);
      failedPayments = [];
      invoices = [];
      totalItems = 0;
      totalPages = 1;
    }
    render();
    return;
  }

  try {
    const invoiceStatus = status === 'DA_THANH_TOAN' || status === 'CHUA_THANH_TOAN' ? status : '';
    const pageData = await window.fetchPage(api, '/api/tenant/hoa-don/search', {
      page: page - 1,
      size: PAGE_SIZE,
      params: { period, status: invoiceStatus }
    });
    invoices = pageData.content || [];
    failedPayments = [];
    totalItems = Number(pageData.totalElements || 0);
    totalPages = Math.max(1, Number(pageData.totalPages || 1));
    if (page > totalPages) {
      page = totalPages;
      return loadItems();
    }
  } catch (e) {
    console.error(e);
    invoices = [];
    failedPayments = [];
    totalItems = 0;
    totalPages = 1;
  }
  render();
}

function renderRowsForFailedPayments() {
  return failedPayments.map((payment) => {
    const invoice = payment.hoaDon || null;
    return `
      <tr>
        <td>${escapeHtml(invoice?.kyHoaDon || '')}</td>
        <td>TT${payment.thanhToanId || ''}</td>
        <td>HD${invoice?.hoaDonId || ''}</td>
        <td>${fmtMoney(payment.soTien || 0)}</td>
        <td>${escapeHtml(labels?.paymentMethod(payment.phuongThuc || '') || '')}</td>
        <td>${fmtDate(payment.ngayThanhToan) || ''}</td>
        <td>${statusBadge('THAT_BAI')}</td>
        <td>
          <div class="action-group">
            <button class="btn-small btn-view" type="button" data-act="detail-failed" data-id="${payment.thanhToanId}">Xem chi tiet</button>
          </div>
        </td>
      </tr>`;
  }).join('');
}

function renderRowsForInvoices() {
  return invoices.map((invoice) => {
    const payment = paymentOfInvoice(invoice.hoaDonId);
    const status = getInvoiceStatus(invoice, payment);
    return `
      <tr>
        <td>${escapeHtml(invoice.kyHoaDon || '')}</td>
        <td>${payment ? `TT${payment.thanhToanId}` : ''}</td>
        <td>HD${invoice.hoaDonId || ''}</td>
        <td>${fmtMoney(payment?.soTien || invoice.tongTien)}</td>
        <td>${escapeHtml(labels?.paymentMethod(payment?.phuongThuc || '') || '')}</td>
        <td>${fmtDate(payment?.ngayThanhToan || invoice.ngayLap) || ''}</td>
        <td>${statusBadge(status)}</td>
        <td>
          <div class="action-group">
            <button class="btn-small btn-view" type="button" data-act="detail-invoice" data-id="${invoice.hoaDonId}">Xem chi tiet</button>
          </div>
        </td>
      </tr>`;
  }).join('');
}

function render() {
  const start = totalItems ? (page - 1) * PAGE_SIZE + 1 : 0;
  const end = Math.min(page * PAGE_SIZE, totalItems);

  countText.textContent = `${totalItems} muc thanh toan duoc tim thay`;
  pagingInfo.textContent = totalItems ? `Hien thi ${start} - ${end} / ${totalItems} muc` : 'Hien thi 0 muc';
  pageInfo.textContent = `Trang ${page} / ${totalPages}`;
  prevBtn.disabled = page <= 1;
  nextBtn.disabled = page >= totalPages;

  if (!totalItems) {
    tbody.innerHTML = '';
    emptyPay.style.display = '';
    return;
  }

  emptyPay.style.display = 'none';
  tbody.innerHTML = statusSelect.value === 'THAT_BAI'
    ? renderRowsForFailedPayments()
    : renderRowsForInvoices();
}

document.addEventListener('DOMContentLoaded', async () => {
  periodInput.addEventListener('input', () => { page = 1; loadItems(); });
  statusSelect.addEventListener('change', () => { page = 1; hideDetail(); loadItems(); });
  resetBtn.addEventListener('click', () => {
    periodInput.value = '';
    statusSelect.value = '';
    page = 1;
    hideDetail();
    loadItems();
  });
  prevBtn.addEventListener('click', () => { if (page > 1) { page--; loadItems(); } });
  nextBtn.addEventListener('click', () => { if (page < totalPages) { page++; loadItems(); } });

  closeDetailBtn?.addEventListener('click', hideDetail);
  detailOkBtn?.addEventListener('click', hideDetail);
  detailBackdrop?.addEventListener('click', hideDetail);
  detailPayBtn?.addEventListener('click', () => {
    if (!currentDetailInvoiceId) return;
    hideDetail();
    openPaymentPage(currentDetailInvoiceId, 'pay', currentDetailPeriod);
  });

  tbody.addEventListener('click', (event) => {
    const btn = event.target.closest('button[data-act]');
    if (!btn) return;

    if (btn.dataset.act === 'detail-invoice') {
      const invoiceId = Number(btn.dataset.id);
      const invoice = invoices.find((item) => item.hoaDonId === invoiceId);
      if (!invoice) return;
      const payment = paymentOfInvoice(invoiceId);
      showDetail(invoice, payment, getInvoiceStatus(invoice, payment));
      return;
    }

    if (btn.dataset.act === 'detail-failed') {
      const paymentId = Number(btn.dataset.id);
      const payment = failedPayments.find((item) => item.thanhToanId === paymentId);
      if (!payment) return;
      showDetail(payment.hoaDon || null, payment, 'THAT_BAI');
    }
  });

  await loadPayments();
  await loadItems();
});
