const { api, money: fmtMoney, date: fmtDate, labels } = window.AppUtils || {};
const { escapeHtml } = window.UiHelpers || {};

const listEl = document.querySelector('.contract-inner');

async function loadContracts() {
  try {
    const data = await window.fetchAllPages(api, '/api/tenant/hop-dong', {
      sortBy: 'ngayBatDau',
      direction: 'desc'
    });
    const items = Array.isArray(data) ? data : (data.content || []);

    if (!items.length) {
      listEl.innerHTML = '<div class="contract-card">Chua co hop dong.</div>';
      return;
    }

    listEl.innerHTML = items.map((c) => `
      <div class="contract-card">
        <div class="contract-title">Hop dong #${c.hopDongId}</div>
        <div>Phong: <b>${escapeHtml(c.phongTro?.tenPhong || '')}</b></div>
        <div>Dai dien: <b>${escapeHtml(c.khachThue?.hoTen || '')}</b></div>
        <div>Ngay bat dau: ${escapeHtml(fmtDate(c.ngayBatDau) || '')}</div>
        <div>Ngay ket thuc: ${escapeHtml(fmtDate(c.ngayKetThuc) || '-')}</div>
        <div>Tien coc: ${escapeHtml(fmtMoney(c.tienCoc) || '')}</div>
        <div>Trang thai: <b>${escapeHtml(labels?.contractStatus(c.trangThai) || c.trangThai || '')}</b></div>
      </div>`).join('');
  } catch (e) {
    console.error(e);
    listEl.innerHTML = '<div class="contract-card">Khong tai duoc hop dong.</div>';
  }
}

document.addEventListener('DOMContentLoaded', loadContracts);
