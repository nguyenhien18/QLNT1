const { api, money: fmtMoney, date: fmtDate, todayISO, labels } = window.AppUtils || {};
const { escapeHtml, textOrDash } = window.UiHelpers || {};
const { bindChanges, bindInputs, bindPagination } = window.PageFilters || {};

const $ = (id) => document.getElementById(id);
const PAGE_SIZE = 4;
let page = 1;
let totalPages = 1;
let totalItems = 0;
let contracts = [];
let allContracts = [];
let rooms = [];
let tenants = [];
let members = [];
let baseAvailableRooms = [];
let selectedMemberIds = [];
let activeContractIds = new Set();

function badgeStatus(s) {
  if (s === "CON_HIEU_LUC") return `<span class="badge badge-green">${labels?.contractStatus(s) || "Còn hiệu lực"}</span>`;
  if (s === "HET_HIEU_LUC") return `<span class="badge badge-wait">${labels?.contractStatus(s) || "Hết hiệu lực"}</span>`;
  return `<span class="badge badge-red">${labels?.contractStatus(s) || "Đã hủy"}</span>`;
}
function displayText(value) { return textOrDash ? textOrDash(value) : (value ? value : '<span class="text-muted">-</span>'); }
function openModal() { window.Modal?.open("contractModal"); }
function closeModal() { window.Modal?.close("contractModal"); }
function isPastOrToday(isoDate) { return !!isoDate && isoDate <= todayISO(); }
function canCancelContract(c) { return c?.trangThai === "CON_HIEU_LUC" && !isPastOrToday(c.ngayBatDau); }
function canEndContract(c) { return c?.trangThai === "CON_HIEU_LUC" && isPastOrToday(c.ngayBatDau); }
function canEditContract(c) { return c?.trangThai === "CON_HIEU_LUC" && !isPastOrToday(c.ngayBatDau); }

function rebuildActiveContractIds() {
  activeContractIds = new Set(
    (allContracts || [])
      .filter((c) => c?.trangThai === "CON_HIEU_LUC")
      .map((c) => Number(c.hopDongId))
      .filter((id) => Number.isFinite(id))
  );
}

function isActiveContractId(hopDongId) {
  const id = Number(hopDongId);
  return Number.isFinite(id) && activeContractIds.has(id);
}

function activeTenantIds(currentContractId = null) {
  const ids = new Set();
  const editingContractId = Number(currentContractId || 0);

  // Dai dien trong hop dong active
  (allContracts || []).forEach((c) => {
    const id = Number(c?.hopDongId);
    if (!isActiveContractId(id)) return;
    if (editingContractId && id === editingContractId) return;
    const repId = Number(c?.khachThue?.khachThueId);
    if (Number.isFinite(repId) && repId > 0) {
      ids.add(repId);
    }
  });

  // Thanh vien o cung trong hop dong active
  members.forEach((m) => {
    const hdId = Number(m?.hopDong?.hopDongId);
    if (!isActiveContractId(hdId)) return;
    if (editingContractId && hdId === editingContractId) return;
    const tenantId = Number(m?.khachThue?.khachThueId);
    if (Number.isFinite(tenantId) && tenantId > 0) {
      ids.add(tenantId);
    }
  });
  return ids;
}

function getSelectedRoom() {
  const roomId = Number($("contractRoom").value || 0);
  return rooms.find((r) => r.phongTroId === roomId) || null;
}

function maxCompanionsAllowed() {
  const room = getSelectedRoom();
  const cap = Number(room?.sucChua || 0);
  return cap > 0 ? Math.max(0, cap - 1) : 0;
}

function renderMemberPicker() {
  const picker = $("memberPicker");
  if (!picker) return;
  const repId = Number($("contractRep").value || 0);
  const currentId = Number($("contractId").value || 0);
  const occupiedIds = activeTenantIds(currentId);

  picker.innerHTML = '<option value="">-- Chọn khách thuê ở cùng --</option>' + tenants
    .filter((t) => t.khachThueId !== repId)
    .filter((t) => !occupiedIds.has(t.khachThueId) || selectedMemberIds.includes(t.khachThueId))
    .map((t) => `<option value="${t.khachThueId}" ${selectedMemberIds.includes(t.khachThueId) ? "disabled" : ""}>${escapeHtml ? escapeHtml(t.hoTen || "") : (t.hoTen || "")} - ${escapeHtml ? escapeHtml(t.sdt || t.email || "") : (t.sdt || t.email || "")}</option>`)
    .join("");
}

function renderSelectedMembers() {
  const box = $("selectedMembersBox");
  if (!box) return;

  box.innerHTML = selectedMemberIds.map((id) => {
    const t = tenants.find((x) => x.khachThueId === id);
    if (!t) return "";
    return `<div class="member-chip"><span>${escapeHtml ? escapeHtml(t.hoTen || "") : (t.hoTen || "")} - ${escapeHtml ? escapeHtml(t.sdt || t.email || "") : (t.sdt || t.email || "")}</span><button type="button" class="remove-member-btn" data-id="${id}">Xóa</button></div>`;
  }).join("");

  const room = getSelectedRoom();
  const max = Number(room?.sucChua || 0);
  $("memberLimitText").textContent = room ? `Đang chọn ${1 + selectedMemberIds.length}/${max} người` : "Chọn phòng để thêm người ở cùng";
  renderMemberPicker();
}

function renderOptions(selectedRoomId = "", selectedRepId = "") {
  const currentId = Number($("contractId").value || 0);
  const occupiedIds = activeTenantIds(currentId);

  $("contractRoom").innerHTML = '<option value="">-- Chọn phòng --</option>' + rooms
    .map((r) => `<option value="${r.phongTroId}" ${String(selectedRoomId) === String(r.phongTroId) ? "selected" : ""}>${escapeHtml ? escapeHtml(r.tenPhong || "") : (r.tenPhong || "")} - ${Number(r.giaThue || 0).toLocaleString("vi-VN")} đ</option>`)
    .join("");

  $("contractRep").innerHTML = '<option value="">-- Chọn khách thuê đại diện --</option>' + tenants
    .filter((t) => !occupiedIds.has(t.khachThueId) || String(selectedRepId) === String(t.khachThueId))
    .map((t) => `<option value="${t.khachThueId}" ${String(selectedRepId) === String(t.khachThueId) ? "selected" : ""}>${t.hoTen} - ${escapeHtml ? escapeHtml(t.tenDangNhap || "") : (t.tenDangNhap || "")}</option>`)
    .join("");

  renderSelectedMembers();
}

async function loadMeta() {
  [allContracts, baseAvailableRooms, tenants, members] = await Promise.all([
    window.fetchAllPages(api, "/api/hop-dong", { sortBy: "ngayBatDau", direction: "desc" }).catch(() => []),
    api("/api/hop-dong/phong-trong").catch(() => []),
    window.fetchAllPages(api, "/api/khach-thue", { sortBy: "khachThueId", direction: "desc" }).catch(() => []),
    window.fetchAllPages(api, "/api/thanh-vien-phong", { sortBy: "thanhVienId", direction: "desc" }).catch(() => []),
  ]);
  rebuildActiveContractIds();
  rooms = [...baseAvailableRooms];
  renderOptions();
}

async function loadList() {
  const room = $("fContractRoom").value.trim();
  const status = $("fContractStatus").value;

  try {
    const pageData = await window.fetchPage(api, "/api/hop-dong/search", {
      page: page - 1,
      size: PAGE_SIZE,
      params: { room, status },
    });
    contracts = pageData.content || [];
    totalItems = Number(pageData.totalElements || 0);
    totalPages = Math.max(1, Number(pageData.totalPages || 1));
    if (page > totalPages) {
      page = totalPages;
      return loadList();
    }
  } catch (err) {
    console.error("Load hợp đồng search failed", err);
    contracts = [];
    totalItems = 0;
    totalPages = 1;
  }
  render();
}

function memberNames(contractId) {
  return members
    .filter((m) => m.hopDong?.hopDongId === contractId && m.vaiTro === "O_CUNG")
    .map((m) => m.hoTen)
    .join(", ");
}

function render() {
  const start = totalItems ? (page - 1) * PAGE_SIZE + 1 : 0;
  const end = Math.min(page * PAGE_SIZE, totalItems);

  $("contractCountText").textContent = `${totalItems} hợp đồng được tìm thấy`;
  $("contractPagingInfo").textContent = totalItems ? `Hiển thị ${start} - ${end} / ${totalItems} hợp đồng` : "Hiển thị 0 hợp đồng";
  $("pageInfo").textContent = `Trang ${page} / ${totalPages}`;
  $("prevPageBtn").disabled = page <= 1;
  $("nextPageBtn").disabled = page >= totalPages;

  const list = $("contractList");
  if (!totalItems) {
    list.innerHTML = '<tr><td colspan="9" class="empty-cell">Không có hợp đồng phù hợp</td></tr>';
    return;
  }

  const buildActionButtons = (c) => {
    const actions = [];
    if (canEditContract(c)) actions.push(`<button class="btn-small btn-edit" data-act="edit" data-id="${c.hopDongId}">Sửa</button>`);
    if (canCancelContract(c)) actions.push(`<button class="btn-small btn-delete" data-act="cancel" data-id="${c.hopDongId}">Hủy</button>`);
    else if (canEndContract(c)) actions.push(`<button class="btn-small btn-delete" data-act="end" data-id="${c.hopDongId}">Kết thúc</button>`);
    return actions.length ? actions.join("") : '<span class="text-muted">-</span>';
  };

  list.innerHTML = contracts.map((c) => `
    <tr data-id="${c.hopDongId}">
      <td class="name-cell">#${displayText(c.hopDongId)}</td>
      <td class="name-cell">${displayText(c.phongTro?.tenPhong)}</td>
      <td>${displayText(c.khachThue?.hoTen)}</td>
      <td>${displayText(memberNames(c.hopDongId) || "Không có")}</td>
      <td>${displayText(fmtDate(c.ngayBatDau))}</td>
      <td>${displayText(fmtDate(c.ngayKetThuc))}</td>
      <td>${fmtMoney(c.phongTro?.giaThue ?? c.tienCoc ?? 0)}</td>
      <td>${badgeStatus(c.trangThai)}</td>
      <td>
        <div class="action-group">
          ${buildActionButtons(c)}
        </div>
      </td>
    </tr>`).join("");
}

function clearForm() {
  $("contractForm").reset();
  $("contractId").value = "";
  $("contractStart").value = todayISO();
  $("contractStatus").value = "CON_HIEU_LUC";
  selectedMemberIds = [];
  rooms = [...baseAvailableRooms];
  renderOptions();
}

function openEdit(c) {
  $("contractTitle").textContent = "Cập nhật hợp đồng";
  $("contractId").value = c.hopDongId;
  $("contractStart").value = c.ngayBatDau || "";
  $("contractEnd").value = c.ngayKetThuc || "";
  $("contractDeposit").value = c.tienCoc || 0;
  $("contractStatus").value = c.trangThai || "CON_HIEU_LUC";
  selectedMemberIds = members
    .filter((m) => m.hopDong?.hopDongId === c.hopDongId && m.vaiTro === "O_CUNG" && m.khachThue?.khachThueId)
    .map((m) => Number(m.khachThue.khachThueId));

  const editRooms = [...baseAvailableRooms];
  if (!editRooms.find((r) => r.phongTroId === c.phongTro?.phongTroId) && c.phongTro) editRooms.unshift(c.phongTro);
  rooms = editRooms;

  renderOptions(c.phongTro?.phongTroId || "", c.khachThue?.khachThueId || "");
  openModal();
}

function addMember() {
  const room = getSelectedRoom();
  const repId = Number($("contractRep").value || 0);
  const memberId = Number($("memberPicker").value || 0);
  if (!room) return alert("Vui lòng chọn phòng trước");
  if (!repId) return alert("Vui lòng chọn người đại diện trước");
  if (!memberId) return alert("Vui lòng chọn khách thuê ở cùng");
  if (selectedMemberIds.includes(memberId)) return;
  if (memberId === repId) return alert("Người đại diện không thể đồng thời là người ở cùng");
  if (selectedMemberIds.length >= maxCompanionsAllowed()) return alert(`Phòng này chỉ cho tối đa ${room.sucChua} người`);
  selectedMemberIds.push(memberId);
  renderSelectedMembers();
}

async function saveContract() {
  const roomId = Number($("contractRoom").value || 0);
  const repId = Number($("contractRep").value || 0);
  const memberIds = selectedMemberIds.filter((id) => id !== repId);
  if (!roomId || !repId) return alert("Vui lòng chọn phòng và người đại diện");

  const body = {
    phongTroId: roomId,
    daiDienKhachThueId: repId,
    thanhVienKhachThueIds: memberIds,
    ngayBatDau: $("contractStart").value,
    ngayKetThuc: $("contractEnd").value || null,
    tienCoc: Number($("contractDeposit").value || 0),
  };

  const id = $("contractId").value.trim();
  if (!id) {
    body.trangThai = "CON_HIEU_LUC";
  }

  try {
    if (id) {
      await api(`/api/hop-dong/${id}`, { method: "PUT", headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) });
    } else {
      await api("/api/hop-dong", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) });
    }
    closeModal();
    clearForm();
    await loadMeta();
    await loadList();
  } catch (e) {
    alert("Lưu hợp đồng thất bại: " + e.message);
  }
}

document.addEventListener("DOMContentLoaded", async () => {
  $("addContractBtn")?.addEventListener("click", () => { clearForm(); $("contractTitle").textContent = "Tạo hợp đồng"; openModal(); });
  $("contractClose")?.addEventListener("click", closeModal);
  $("contractCancel")?.addEventListener("click", closeModal);
  $("contractBackdrop")?.addEventListener("click", closeModal);
  $("contractSave")?.addEventListener("click", saveContract);
  $("addMemberBtn")?.addEventListener("click", addMember);
  $("selectedMembersBox")?.addEventListener("click", (e) => {
    const btn = e.target.closest(".remove-member-btn");
    if (!btn) return;
    const id = Number(btn.dataset.id);
    selectedMemberIds = selectedMemberIds.filter((x) => x !== id);
    renderSelectedMembers();
  });
  $("contractRoom")?.addEventListener("change", renderSelectedMembers);
  $("contractRep")?.addEventListener("change", () => {
    selectedMemberIds = selectedMemberIds.filter((id) => id !== Number($("contractRep").value || 0));
    renderSelectedMembers();
  });
  bindInputs?.(["fContractRoom"], () => { page = 1; loadList(); });
  bindChanges?.(["fContractStatus"], () => { page = 1; loadList(); });
  $("resetContractFilter")?.addEventListener("click", () => { $("fContractRoom").value = ""; $("fContractStatus").value = ""; page = 1; loadList(); });
  bindPagination?.("prevPageBtn", "nextPageBtn", {
    get page() { return page; },
    set page(v) { page = v; },
    get totalPages() { return totalPages; },
  }, loadList);

  $("contractList")?.addEventListener("click", async (e) => {
    const btn = e.target.closest("button[data-act]");
    if (!btn) return;
    const id = Number(btn.dataset.id);
    const contract = contracts.find((x) => x.hopDongId === id);
    if (!contract) return;

    if (btn.dataset.act === "edit") {
      if (!canEditContract(contract)) {
        alert("Chi sua duoc hop dong con hieu luc va chua toi ngay hieu luc.");
        return;
      }
      openEdit(contract);
      return;
    }

    if (btn.dataset.act === "cancel") {
      if (!canCancelContract(contract)) {
        alert("Chỉ hủy được hợp đồng chưa tới ngày hiệu lực.");
        return;
      }
      if (!confirm(`Hủy hợp đồng #${id}?`)) return;
      try {
        await api(`/api/hop-dong/${id}/huy`, { method: "PUT", headers: { "Content-Type": "application/json" } });
        await loadMeta();
        await loadList();
      } catch (err) {
        alert("Hủy hợp đồng thất bại: " + err.message);
      }
      return;
    }

    if (btn.dataset.act === "end") {
      if (!canEndContract(contract)) {
        alert("Chi ket thuc duoc hop dong con hieu luc va da toi ngay hieu luc.");
        return;
      }
      if (!confirm(`Ket thuc hop dong #${id}?`)) return;
      try {
        await api(`/api/hop-dong/${id}/ket-thuc`, { method: "PUT", headers: { "Content-Type": "application/json" } });
        await loadMeta();
        await loadList();
      } catch (err) {
        alert("Ket thuc hop dong that bai: " + err.message);
      }
    }
  });

  await loadMeta();
  await loadList();
});


