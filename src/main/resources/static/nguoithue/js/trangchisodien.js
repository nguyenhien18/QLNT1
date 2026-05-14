(function () {
const { api: apiClient, money: fmtMoney } = window.AppUtils || {};
const { escapeHtml } = window.UiHelpers || {};
const { bindChanges, bindInputs, bindPagination } = window.PageFilters || {};

const typeSelect = document.getElementById('typeSelect');
const periodInput = document.getElementById('periodInput');
const resetBtn = document.getElementById('resetBtn');
const meterTbody = document.getElementById('meterTbody');
const emptyState = document.getElementById('emptyState');
const countText = document.getElementById('meterCountText');
const pagingInfo = document.getElementById('meterPagingInfo');
const pageInfo = document.getElementById('pageInfo');
const prevBtn = document.getElementById('prevPageBtn');
const nextBtn = document.getElementById('nextPageBtn');

const PAGE_SIZE = 5;
let page = 1;
let totalPages = 1;
let totalItems = 0;
let meters = [];

function typeBadge(type) {
  return type === 'DIEN'
    ? '<span class="tag tag-electric">Dien</span>'
    : '<span class="tag tag-water">Nuoc</span>';
}

function row(m) {
  const cons = Number(m.luongTieuThu ?? (Number(m.chiSoMoi || 0) - Number(m.chiSoCu || 0)));
  const unit = m.loai === 'DIEN' ? 'kWh' : 'm3';
  return `
    <tr>
      <td>${escapeHtml(m.ky || '')}</td>
      <td>${typeBadge(m.loai)}</td>
      <td>${escapeHtml(m.chiSoCu ?? 0)}</td>
      <td>${escapeHtml(m.chiSoMoi ?? 0)}</td>
      <td>${escapeHtml(cons)} ${unit}</td>
      <td>${escapeHtml(fmtMoney(m.donGia))} / ${unit}</td>
      <td>${escapeHtml(fmtMoney(m.thanhTien))}</td>
    </tr>`;
}

function render() {
  const start = totalItems ? (page - 1) * PAGE_SIZE + 1 : 0;
  const end = Math.min(page * PAGE_SIZE, totalItems);

  countText.textContent = `${totalItems} chi so duoc tim thay`;
  pagingInfo.textContent = totalItems ? `Hien thi ${start} - ${end} / ${totalItems} chi so` : 'Hien thi 0 chi so';
  pageInfo.textContent = `Trang ${page} / ${totalPages}`;
  prevBtn.disabled = page <= 1;
  nextBtn.disabled = page >= totalPages;

  if (!totalItems) {
    meterTbody.innerHTML = '';
    emptyState.style.display = '';
    return;
  }

  emptyState.style.display = 'none';
  meterTbody.innerHTML = meters.map(row).join('');
}

async function loadMeters() {
  const type = typeSelect.value;
  const period = periodInput.value.trim();
  try {
    const pageData = await window.fetchPage(apiClient, '/api/tenant/chi-so/search', {
      page: page - 1,
      size: PAGE_SIZE,
      params: { type, period }
    });
    meters = pageData.content || [];
    totalItems = Number(pageData.totalElements || 0);
    totalPages = Math.max(1, Number(pageData.totalPages || 1));
    if (page > totalPages) {
      page = totalPages;
      return loadMeters();
    }
  } catch (e) {
    console.error(e);
    meters = [];
    totalItems = 0;
    totalPages = 1;
  }
  render();
}

document.addEventListener('DOMContentLoaded', async () => {
  bindChanges?.(['typeSelect'], () => { page = 1; loadMeters(); });
  bindInputs?.(['periodInput'], () => { page = 1; loadMeters(); });
  resetBtn.addEventListener('click', () => {
    typeSelect.value = 'ALL';
    periodInput.value = '';
    page = 1;
    loadMeters();
  });
  bindPagination?.('prevPageBtn', 'nextPageBtn', {
    get page() { return page; },
    set page(v) { page = v; },
    get totalPages() { return totalPages; },
  }, loadMeters);

  await loadMeters();
});

})();
