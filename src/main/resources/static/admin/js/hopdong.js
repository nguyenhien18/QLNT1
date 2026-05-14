(function () {
const { api: apiClient, money: fmtMoney, date: fmtDate, todayISO, labels } = window.AppUtils || {};
const { escapeHtml, textOrDash } = window.UiHelpers || {};
const { bindChanges, bindPagination } = window.PageFilters || {};

const $ = (id) => document.getElementById(id);
const PAGE_SIZE = 4;
const LARGE_PAGE_SIZE = 1000;

let page = 1;
let totalPages = 1;
let totalItems = 0;
let contracts = [];
let rooms = [];
let baseAvailableRooms = [];
let filterRoomNames = [];
let tenants = [];
let selectedMemberIds = [];
let memberMap = new Map();

function badgeStatus(status) {
  if (status === "CON_HIEU_LUC") return `<span class="badge badge-green">${labels?.contractStatus(status) || "Con hieu luc"}</span>`;
  if (status === "HET_HIEU_LUC") return `<span class="badge badge-wait">${labels?.contractStatus(status) || "Het hieu luc"}</span>`;
  return `<span class="badge badge-red">${labels?.contractStatus(status) || "Da huy"}</span>`;
}

function displayText(value) {
  return textOrDash ? textOrDash(value) : (value ? value : '<span class="text-muted">-</span>');
}

function openModal() { window.Modal?.open("contractModal"); }
function closeModal() { window.Modal?.close("contractModal"); }
function isPastOrToday(isoDate) { return !!isoDate && isoDate <= todayISO(); }
function canCancelContract(contract) { return contract?.trangThai === "CON_HIEU_LUC" && !isPastOrToday(contract.ngayBatDau); }
function canEndContract(contract) { return contract?.trangThai === "CON_HIEU_LUC" && isPastOrToday(contract.ngayBatDau); }
function canEditContract(contract) { return contract?.trangThai === "CON_HIEU_LUC" && !isPastOrToday(contract.ngayBatDau); }

function getSelectedRoom() {
  const roomId = Number($("contractRoom").value || 0);
  return rooms.find((room) => room.phongTroId === roomId) || null;
}

function maxCompanionsAllowed() {
  const room = getSelectedRoom();
  const cap = Number(room?.sucChua || 0);
  return cap > 0 ? Math.max(0, cap - 1) : 0;
}

function memberNames(contractId) {
  return (memberMap.get(Number(contractId)) || [])
    .filter((member) => member?.vaiTro === "O_CUNG")
    .map((member) => member?.hoTen)
    .filter(Boolean)
    .join(", ");
}

function renderMemberPicker() {
  const picker = $("memberPicker");
  if (!picker) return;

  const repId = Number($("contractRep").value || 0);
  picker.innerHTML = '<option value="">-- Chon khach thue o cung --</option>' + tenants
    .filter((tenant) => Number(tenant.khachThueId) !== repId)
    .map((tenant) => {
      const id = Number(tenant.khachThueId);
      const disabled = selectedMemberIds.includes(id) ? "disabled" : "";
      const name = escapeHtml ? escapeHtml(tenant.hoTen || "") : (tenant.hoTen || "");
      const contact = escapeHtml ? escapeHtml(tenant.sdt || tenant.email || "") : (tenant.sdt || tenant.email || "");
      return `<option value="${id}" ${disabled}>${name} - ${contact}</option>`;
    })
    .join("");
}

function renderSelectedMembers() {
  const box = $("selectedMembersBox");
  if (!box) return;

  box.innerHTML = selectedMemberIds.map((id) => {
    const tenant = tenants.find((item) => Number(item.khachThueId) === Number(id));
    if (!tenant) return "";
    const name = escapeHtml ? escapeHtml(tenant.hoTen || "") : (tenant.hoTen || "");
    const contact = escapeHtml ? escapeHtml(tenant.sdt || tenant.email || "") : (tenant.sdt || tenant.email || "");
    return `<div class="member-chip"><span>${name} - ${contact}</span><button type="button" class="remove-member-btn" data-id="${id}">Xoa</button></div>`;
  }).join("");

  const room = getSelectedRoom();
  const max = Number(room?.sucChua || 0);
  $("memberLimitText").textContent = room
    ? `Dang chon ${1 + selectedMemberIds.length}/${max} nguoi`
    : "Chon phong de them nguoi o cung";
  renderMemberPicker();
}

function renderOptions(selectedRoomId = "", selectedRepId = "") {
  $("contractRoom").innerHTML = '<option value="">-- Chon phong --</option>' + rooms
    .map((room) => {
      const selected = String(selectedRoomId) === String(room.phongTroId) ? "selected" : "";
      const roomName = escapeHtml ? escapeHtml(room.tenPhong || "") : (room.tenPhong || "");
      const rent = Number(room.giaThue || 0).toLocaleString("vi-VN");
      return `<option value="${room.phongTroId}" ${selected}>${roomName} - ${rent} d</option>`;
    })
    .join("");

  $("contractRep").innerHTML = '<option value="">-- Chon khach thue dai dien --</option>' + tenants
    .map((tenant) => {
      const selected = String(selectedRepId) === String(tenant.khachThueId) ? "selected" : "";
      const name = escapeHtml ? escapeHtml(tenant.hoTen || "") : (tenant.hoTen || "");
      const username = escapeHtml ? escapeHtml(tenant.tenDangNhap || "") : (tenant.tenDangNhap || "");
      return `<option value="${tenant.khachThueId}" ${selected}>${name} - ${username}</option>`;
    })
    .join("");

  renderSelectedMembers();
}

function renderContractRoomFilter() {
  const roomFilter = $("fContractRoom");
  if (!roomFilter) return;

  const currentValue = roomFilter.value || "";
  roomFilter.innerHTML = '<option value="">Tat ca</option>' + filterRoomNames
    .map((roomName) => {
      const safeRoomName = escapeHtml ? escapeHtml(roomName) : roomName;
      return `<option value="${safeRoomName}">${safeRoomName}</option>`;
    })
    .join("");

  if (currentValue && filterRoomNames.includes(currentValue)) {
    roomFilter.value = currentValue;
  }
}

async function loadAvailableTenants(hopDongId = null) {
  const query = hopDongId ? `?hopDongId=${hopDongId}` : "";
  tenants = await apiClient(`/api/hop-dong/khach-thue-trong${query}`).catch(() => []);
}

async function loadMeta() {
  const [availableRooms, roomSummary] = await Promise.all([
    apiClient("/api/hop-dong/phong-trong").catch(() => []),
    apiClient("/api/phong-tro/summary").catch(() => []),
  ]);

  await loadAvailableTenants();
  baseAvailableRooms = availableRooms || [];
  rooms = [...baseAvailableRooms];
  const roomSource = (roomSummary || []).length ? roomSummary : baseAvailableRooms;
  filterRoomNames = [...new Set((roomSource || [])
    .map((room) => String(room?.tenPhong || "").trim())
    .filter(Boolean))]
    .sort((left, right) => left.localeCompare(right, "vi", { numeric: true, sensitivity: "base" }));
  renderContractRoomFilter();
  renderOptions();
}

async function fetchMembersForContract(contractId) {
  const pageData = await window.fetchPage(apiClient, `/api/thanh-vien-phong/hop-dong/${contractId}`, {
    page: 0,
    size: LARGE_PAGE_SIZE,
    sortBy: "thanhVienId",
    direction: "asc",
  }).catch(() => ({ content: [] }));
  return pageData.content || [];
}

async function loadMembersForCurrentPage() {
  const entries = await Promise.all(
    contracts.map(async (contract) => {
      const items = await fetchMembersForContract(contract.hopDongId);
      return [Number(contract.hopDongId), items];
    }),
  );
  memberMap = new Map(entries);
}

async function loadList() {
  const room = $("fContractRoom").value.trim();
  const status = $("fContractStatus").value;

  try {
    const pageData = await window.fetchPage(apiClient, "/api/hop-dong/search", {
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
    await loadMembersForCurrentPage();
  } catch (error) {
    console.error("Load hop dong search failed", error);
    contracts = [];
    memberMap = new Map();
    totalItems = 0;
    totalPages = 1;
  }
  render();
}

function render() {
  const start = totalItems ? (page - 1) * PAGE_SIZE + 1 : 0;
  const end = Math.min(page * PAGE_SIZE, totalItems);

  $("contractCountText").textContent = `${totalItems} hop dong duoc tim thay`;
  $("contractPagingInfo").textContent = totalItems
    ? `Hien thi ${start} - ${end} / ${totalItems} hop dong`
    : "Hien thi 0 hop dong";
  $("pageInfo").textContent = `Trang ${page} / ${totalPages}`;
  $("prevPageBtn").disabled = page <= 1;
  $("nextPageBtn").disabled = page >= totalPages;

  const list = $("contractList");
  if (!totalItems) {
    list.innerHTML = '<tr><td colspan="9" class="empty-cell">Khong co hop dong phu hop</td></tr>';
    return;
  }

  const buildActionButtons = (contract) => {
    const actions = [];
    if (canEditContract(contract)) actions.push(`<button class="btn-small btn-edit" data-act="edit" data-id="${contract.hopDongId}">Sua</button>`);
    if (canCancelContract(contract)) actions.push(`<button class="btn-small btn-delete" data-act="cancel" data-id="${contract.hopDongId}">Huy</button>`);
    else if (canEndContract(contract)) actions.push(`<button class="btn-small btn-delete" data-act="end" data-id="${contract.hopDongId}">Ket thuc</button>`);
    return actions.length ? actions.join("") : '<span class="text-muted">-</span>';
  };

  list.innerHTML = contracts.map((contract) => `
    <tr data-id="${contract.hopDongId}">
      <td class="name-cell">#${displayText(contract.hopDongId)}</td>
      <td class="name-cell">${displayText(contract.phongTro?.tenPhong)}</td>
      <td>${displayText(contract.khachThue?.hoTen)}</td>
      <td>${displayText(memberNames(contract.hopDongId) || "Khong co")}</td>
      <td>${displayText(fmtDate(contract.ngayBatDau))}</td>
      <td>${displayText(fmtDate(contract.ngayKetThuc))}</td>
      <td>${fmtMoney(contract.phongTro?.giaThue ?? contract.tienCoc ?? 0)}</td>
      <td>${badgeStatus(contract.trangThai)}</td>
      <td><div class="action-group">${buildActionButtons(contract)}</div></td>
    </tr>
  `).join("");
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

async function openEdit(contract) {
  $("contractTitle").textContent = "Cap nhat hop dong";
  $("contractId").value = contract.hopDongId;
  $("contractStart").value = contract.ngayBatDau || "";
  $("contractEnd").value = contract.ngayKetThuc || "";
  $("contractDeposit").value = contract.tienCoc || 0;
  $("contractStatus").value = contract.trangThai || "CON_HIEU_LUC";

  let memberItems = memberMap.get(Number(contract.hopDongId));
  if (!memberItems) {
    memberItems = await fetchMembersForContract(contract.hopDongId);
    memberMap.set(Number(contract.hopDongId), memberItems);
  }

  selectedMemberIds = (memberItems || [])
    .filter((member) => member?.vaiTro === "O_CUNG" && member?.khachThue?.khachThueId)
    .map((member) => Number(member.khachThue.khachThueId));

  const editableRooms = [...baseAvailableRooms];
  if (!editableRooms.find((room) => room.phongTroId === contract.phongTro?.phongTroId) && contract.phongTro) {
    editableRooms.unshift(contract.phongTro);
  }
  rooms = editableRooms;
  await loadAvailableTenants(contract.hopDongId);

  renderOptions(contract.phongTro?.phongTroId || "", contract.khachThue?.khachThueId || "");
  openModal();
}

function addMember() {
  const room = getSelectedRoom();
  const repId = Number($("contractRep").value || 0);
  const memberId = Number($("memberPicker").value || 0);

  if (!room) return alert("Vui long chon phong truoc");
  if (!repId) return alert("Vui long chon nguoi dai dien truoc");
  if (!memberId) return alert("Vui long chon khach thue o cung");
  if (selectedMemberIds.includes(memberId)) return;
  if (memberId === repId) return alert("Nguoi dai dien khong the dong thoi la nguoi o cung");
  if (selectedMemberIds.length >= maxCompanionsAllowed()) {
    return alert(`Phong nay chi cho toi da ${room.sucChua} nguoi`);
  }

  selectedMemberIds.push(memberId);
  renderSelectedMembers();
}

async function saveContract() {
  const roomId = Number($("contractRoom").value || 0);
  const repId = Number($("contractRep").value || 0);
  const memberIds = selectedMemberIds.filter((id) => id !== repId);
  if (!roomId || !repId) {
    alert("Vui long chon phong va nguoi dai dien");
    return;
  }

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
      await apiClient(`/api/hop-dong/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });
    } else {
      await apiClient("/api/hop-dong", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });
    }
    closeModal();
    clearForm();
    await loadMeta();
    await loadList();
  } catch (error) {
    alert(`Luu hop dong that bai: ${error.message}`);
  }
}

document.addEventListener("DOMContentLoaded", async () => {
  $("addContractBtn")?.addEventListener("click", async () => {
    await loadAvailableTenants();
    clearForm();
    $("contractTitle").textContent = "Tao hop dong";
    openModal();
  });
  $("contractClose")?.addEventListener("click", closeModal);
  $("contractCancel")?.addEventListener("click", closeModal);
  $("contractBackdrop")?.addEventListener("click", closeModal);
  $("contractSave")?.addEventListener("click", saveContract);
  $("addMemberBtn")?.addEventListener("click", addMember);
  $("selectedMembersBox")?.addEventListener("click", (event) => {
    const btn = event.target.closest(".remove-member-btn");
    if (!btn) return;
    const id = Number(btn.dataset.id);
    selectedMemberIds = selectedMemberIds.filter((value) => value !== id);
    renderSelectedMembers();
  });
  $("contractRoom")?.addEventListener("change", renderSelectedMembers);
  $("contractRep")?.addEventListener("change", () => {
    const repId = Number($("contractRep").value || 0);
    selectedMemberIds = selectedMemberIds.filter((id) => id !== repId);
    renderSelectedMembers();
  });

  bindChanges?.(["fContractRoom", "fContractStatus"], () => {
    page = 1;
    loadList();
  });
  $("resetContractFilter")?.addEventListener("click", () => {
    $("fContractRoom").value = "";
    $("fContractStatus").value = "";
    page = 1;
    loadList();
  });
  bindPagination?.("prevPageBtn", "nextPageBtn", {
    get page() { return page; },
    set page(v) { page = v; },
    get totalPages() { return totalPages; },
  }, loadList);

  $("contractList")?.addEventListener("click", async (event) => {
    const btn = event.target.closest("button[data-act]");
    if (!btn) return;

    const id = Number(btn.dataset.id);
    const contract = contracts.find((item) => item.hopDongId === id);
    if (!contract) return;

    if (btn.dataset.act === "edit") {
      if (!canEditContract(contract)) {
        alert("Chi sua duoc hop dong con hieu luc va chua toi ngay hieu luc.");
        return;
      }
      await openEdit(contract);
      return;
    }

    if (btn.dataset.act === "cancel") {
      if (!canCancelContract(contract)) {
        alert("Chi huy duoc hop dong chua toi ngay hieu luc.");
        return;
      }
      if (!confirm(`Huy hop dong #${id}?`)) return;
      try {
        await apiClient(`/api/hop-dong/${id}/huy`, { method: "PUT", headers: { "Content-Type": "application/json" } });
        await loadMeta();
        await loadList();
      } catch (error) {
        alert(`Huy hop dong that bai: ${error.message}`);
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
        await apiClient(`/api/hop-dong/${id}/ket-thuc`, { method: "PUT", headers: { "Content-Type": "application/json" } });
        await loadMeta();
        await loadList();
      } catch (error) {
        alert(`Ket thuc hop dong that bai: ${error.message}`);
      }
    }
  });

  await loadMeta();
  await loadList();
});

})();
