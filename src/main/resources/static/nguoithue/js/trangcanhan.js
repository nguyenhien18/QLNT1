const { api } = window.AppUtils || {};
function fmtDate(v) {
  if (!v) return "";
  if (typeof v === "string" && /^\d{4}-\d{2}-\d{2}/.test(v)) {
    const [y, m, d] = v.slice(0, 10).split("-");
    return `${d}-${m}-${y}`;
  }
  const date = new Date(v);
  return Number.isNaN(date.getTime()) ? String(v) : date.toLocaleDateString("vi-VN");
}

function genderText(v) { return v === "NAM" ? "Nam" : v === "NU" ? "Nữ" : "Khác"; }
function setText(id, value) { const el = document.getElementById(id); if (el) el.textContent = value ?? ""; }

async function loadProfile() {
  try {
    const [tenant, contractData] = await Promise.all([
      api('/api/tenant/profile'),
      window.fetchAllPages(api, '/api/tenant/hop-dong', { sortBy: 'ngayBatDau', direction: 'desc' })
    ]);
    const contracts = Array.isArray(contractData) ? contractData : (contractData.content || []);
    const activeContract = contracts.find(c => c.trangThai === 'CON_HIEU_LUC') || contracts[0] || null;
    setText('fullName', tenant.hoTen || '');
    setText('email', tenant.email || '');
    setText('phone', tenant.sdt || '');
    setText('cccd', tenant.cccd || '');
    setText('address', tenant.diaChi || '');
    setText('dob', fmtDate(tenant.ngaySinh));
    setText('gender', genderText(tenant.gioiTinh));
    setText('username', tenant.tenDangNhap || '');
    const isRepresentative = Boolean(activeContract && tenant?.khachThueId && activeContract?.khachThue?.khachThueId && Number(activeContract.khachThue.khachThueId) === Number(tenant.khachThueId));
    setText('room', activeContract?.phongTro?.tenPhong || 'Chưa có');
    setText('roleInRoom', activeContract ? (isRepresentative ? 'Đại diện' : 'Ở cùng') : 'Chưa có');
  } catch (e) {
    console.error('Lỗi load trang cá nhân:', e);
    alert('Không tải được thông tin cá nhân.');
  }
}

document.addEventListener('DOMContentLoaded', loadProfile);


