const { api } = window.AppUtils || {};
const { textOrDash } = window.UiHelpers || {};

const $ = (id) => document.getElementById(id);
const PAGE_SIZE = 4;

function displayText(value) { return textOrDash ? textOrDash(value) : (value ? value : '<span class="text-muted">-</span>'); }
function tenantStatusBadge(status) { return status === "HOAT_DONG" ? '<span class="status-badge status-ok">Hoat dong</span>' : '<span class="status-badge status-locked">Khoa</span>'; }
function genderText(value) {
  if (value === "NAM") return "Nam";
  if (value === "NU") return "Nữ";
  if (value === "KHAC") return "Khác";
  return "";
}

let currentPage = 1;
let totalPages = 1;
let totalItems = 0;
let tenants = [];
let contracts = [];
let members = [];

function openModal() {
  const modal = $("tenantModal");
  if (!modal) return;
  modal.classList.add("show");
  modal.setAttribute("aria-hidden", "false");
}

function closeModal() {
  const modal = $("tenantModal");
  if (!modal) return;
  modal.classList.remove("show");
  modal.setAttribute("aria-hidden", "true");
}

async function loadMeta() {
  [contracts, members] = await Promise.all([
    window.fetchAllPages(api, "/api/hop-dong", { sortBy: "ngayBatDau", direction: "desc" }).catch(() => []),
    window.fetchAllPages(api, "/api/thanh-vien-phong", { sortBy: "thanhVienId", direction: "desc" }).catch(() => []),
  ]);
}

function hasContractHistory(tenantId) {
  return contracts.some((c) => c.khachThue?.khachThueId === tenantId)
    || members.some((m) => m.khachThue?.khachThueId === tenantId);
}

async function loadList() {
  const keyword = ($("fTenantKeyword")?.value || "").trim();
  try {
    const pageData = await window.fetchPage(api, "/api/khach-thue/search", {
      page: currentPage - 1,
      size: PAGE_SIZE,
      params: { keyword },
    });
    tenants = pageData.content || [];
    totalItems = Number(pageData.totalElements || 0);
    totalPages = Math.max(1, Number(pageData.totalPages || 1));
    if (currentPage > totalPages) {
      currentPage = totalPages;
      return loadList();
    }
  } catch (err) {
    console.error("Loi load /api/khach-thue/search:", err);
    tenants = [];
    totalItems = 0;
    totalPages = 1;
  }
  render();
}

function render() {
  const listEl = $("tenantList");
  const countEl = $("tenantCountText");
  const pagingInfoEl = $("tenantPagingInfo");
  const pageInfoEl = $("pageInfo");
  const prevBtn = $("prevPageBtn");
  const nextBtn = $("nextPageBtn");

  if (!listEl) return;

  const start = totalItems ? (currentPage - 1) * PAGE_SIZE + 1 : 0;
  const end = Math.min(currentPage * PAGE_SIZE, totalItems);

  if (countEl) countEl.textContent = `${totalItems} khach thue duoc tim thay`;
  if (pagingInfoEl) pagingInfoEl.textContent = totalItems > 0 ? `Hien thi ${start} - ${end} / ${totalItems} khach thue` : "Hien thi 0 khach thue";
  if (pageInfoEl) pageInfoEl.textContent = `Trang ${currentPage} / ${totalPages}`;
  if (prevBtn) prevBtn.disabled = currentPage <= 1;
  if (nextBtn) nextBtn.disabled = currentPage >= totalPages;

  if (totalItems === 0) {
    listEl.innerHTML = '<tr><td colspan="10" class="empty-cell">Khong co khach thue phu hop</td></tr>';
    return;
  }

  listEl.innerHTML = tenants.map((t) => `
    <tr data-id="${t.khachThueId}">
      <td class="name-cell">${displayText(t.hoTen)}</td>
      <td>${displayText(genderText(t.gioiTinh))}</td>
      <td>${displayText(t.ngaySinh)}</td>
      <td>${displayText(t.sdt)}</td>
      <td>${displayText(t.email)}</td>
      <td>${displayText(t.cccd)}</td>
      <td>${displayText(t.diaChi)}</td>
      <td>${displayText(t.tenDangNhap)}</td>
      <td>${tenantStatusBadge(t.trangThai)}</td>
      <td>
        <div class="action-group">
          <button class="btn-small btn-edit" data-act="edit" data-id="${t.khachThueId}">Sua</button>
          <button class="btn-small btn-delete" data-act="del" data-id="${t.khachThueId}">Xoa</button>
        </div>
      </td>
    </tr>
  `).join("");
}

function clearForm() {
  if ($("tenantForm")) $("tenantForm").reset();
  $("tenantId").value = "";
  $("tenantName").value = "";
  $("tenantPhone").value = "";
  $("tenantGender").value = "";
  $("tenantDob").value = "";
  $("tenantEmail").value = "";
  $("tenantCccd").value = "";
  $("tenantRoom").value = "";
  $("tenantUsername").value = "";
  $("tenantPassword").value = "";
  $("tenantStatus").value = "HOAT_DONG";
}

function openForEdit(t) {
  $("tenantTitle").textContent = "Cap nhat khach thue";
  $("tenantId").value = t.khachThueId || "";
  $("tenantName").value = t.hoTen || "";
  $("tenantPhone").value = t.sdt || "";
  $("tenantGender").value = t.gioiTinh || "";
  $("tenantDob").value = t.ngaySinh || "";
  $("tenantEmail").value = t.email || "";
  $("tenantCccd").value = t.cccd || "";
  $("tenantRoom").value = t.diaChi || "";
  $("tenantUsername").value = t.tenDangNhap || "";
  $("tenantPassword").value = "";
  $("tenantStatus").value = t.trangThai || "HOAT_DONG";
  openModal();
}

async function saveTenant() {
  const id = $("tenantId").value.trim();
  const payload = {
    hoTen: $("tenantName").value.trim(),
    sdt: $("tenantPhone").value.trim(),
    gioiTinh: $("tenantGender").value || null,
    ngaySinh: $("tenantDob").value || null,
    email: $("tenantEmail").value.trim() || null,
    cccd: $("tenantCccd").value.trim(),
    diaChi: $("tenantRoom").value.trim(),
    tenDangNhap: $("tenantUsername").value.trim(),
    trangThai: $("tenantStatus").value
  };

  const password = $("tenantPassword").value.trim();
  if (!id || password) payload.matKhau = password;

  if (!payload.hoTen || !payload.sdt || !payload.cccd || !payload.diaChi || !payload.tenDangNhap) {
    alert("Vui long nhap day du thong tin bat buoc");
    return;
  }
  if (!id && !password) {
    alert("Vui long nhap mat khau cho khach thue moi");
    return;
  }

  try {
    if (id) {
      await api(`/api/khach-thue/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
    } else {
      await api("/api/khach-thue", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
    }

    closeModal();
    clearForm();
    await loadList();
  } catch (err) {
    console.error("Loi luu khach thue:", err);
    alert("Luu khach thue that bai: " + err.message);
  }
}

document.addEventListener("DOMContentLoaded", async () => {
  const addBtn = $("addTenantBtn");
  const closeBtn = $("tenantClose");
  const cancelBtn = $("tenantCancel");
  const backdrop = $("tenantBackdrop");
  const saveBtn = $("tenantSave");
  const keywordInput = $("fTenantKeyword");
  const resetBtn = $("resetTenantFilter");
  const prevBtn = $("prevPageBtn");
  const nextBtn = $("nextPageBtn");
  const listEl = $("tenantList");

  addBtn?.addEventListener("click", () => {
    clearForm();
    $("tenantTitle").textContent = "Them khach thue";
    openModal();
  });
  closeBtn?.addEventListener("click", closeModal);
  cancelBtn?.addEventListener("click", closeModal);
  backdrop?.addEventListener("click", closeModal);
  saveBtn?.addEventListener("click", saveTenant);
  keywordInput?.addEventListener("input", () => { currentPage = 1; loadList(); });
  resetBtn?.addEventListener("click", () => { if (keywordInput) keywordInput.value = ""; currentPage = 1; loadList(); });
  prevBtn?.addEventListener("click", () => { if (currentPage > 1) { currentPage--; loadList(); } });
  nextBtn?.addEventListener("click", () => { if (currentPage < totalPages) { currentPage++; loadList(); } });

  listEl?.addEventListener("click", async (e) => {
    const btn = e.target.closest("button[data-act]");
    if (!btn) return;
    const id = Number(btn.dataset.id);
    const tenant = tenants.find((x) => x.khachThueId === id);
    if (!tenant) return;

    if (btn.dataset.act === "edit") {
      openForEdit(tenant);
      return;
    }

    if (btn.dataset.act === "del") {
      if (hasContractHistory(id)) {
        alert("Khach thue da co lich su hop dong, khong the xoa.");
        return;
      }
      if (!confirm(`Xoa khach thue ${tenant.hoTen}?`)) return;
      try {
        await api(`/api/khach-thue/${id}`, { method: "DELETE" });
        await loadList();
      } catch (err) {
        alert("Xoa khach thue that bai: " + err.message);
      }
    }
  });

  await loadMeta();
  await loadList();
});
