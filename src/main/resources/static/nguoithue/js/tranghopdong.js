(function () {
  const { api: apiClient, money: fmtMoney, date: fmtDate, labels } = window.AppUtils || {};
  const { escapeHtml } = window.UiHelpers || {};

  const contractList = document.getElementById("contractList");
  const emptyState = document.getElementById("emptyState");
  const countText = document.getElementById("contractCountText");

  let contracts = [];

  function normalizedStatus(value) {
    return String(value || "").trim().toUpperCase();
  }

  function statusClass(value) {
    const status = normalizedStatus(value);
    if (status === "CON_HIEU_LUC") return "contract-status--active";
    if (status === "HET_HIEU_LUC") return "contract-status--ended";
    if (status === "HUY") return "contract-status--cancel";
    return "contract-status--default";
  }

  function displayStatus(value) {
    const status = normalizedStatus(value);
    return labels?.contractStatus(status) || status || "Khong ro";
  }

  function displayRoom(contract) {
    return contract?.phongTro?.tenPhong || contract?.tenPhong || "-";
  }

  function displayRepresentative(contract) {
    return contract?.khachThue?.hoTen || contract?.tenKhachThue || "-";
  }

  function byStartDateDesc(left, right) {
    const leftMs = Date.parse(left?.ngayBatDau || "");
    const rightMs = Date.parse(right?.ngayBatDau || "");
    const safeLeft = Number.isNaN(leftMs) ? -Infinity : leftMs;
    const safeRight = Number.isNaN(rightMs) ? -Infinity : rightMs;
    return safeRight - safeLeft;
  }

  function renderCard(contract) {
    const status = normalizedStatus(contract?.trangThai);
    const depositText = contract?.tienCoc === null || contract?.tienCoc === undefined
      ? "-"
      : fmtMoney(contract.tienCoc);
    return `
      <article class="contract-card">
        <div class="contract-card-head">
          <div>
            <p class="contract-code">HD${contract?.hopDongId ?? ""}</p>
            <h4 class="contract-room">${escapeHtml(displayRoom(contract))}</h4>
          </div>
          <span class="contract-status ${statusClass(status)}">${escapeHtml(displayStatus(status))}</span>
        </div>

        <div class="contract-info-grid">
          <div class="contract-info-item">
            <span>Dai dien</span>
            <strong>${escapeHtml(displayRepresentative(contract))}</strong>
          </div>
          <div class="contract-info-item">
            <span>Tien coc</span>
            <strong>${escapeHtml(depositText)}</strong>
          </div>
          <div class="contract-info-item">
            <span>Ngay bat dau</span>
            <strong>${escapeHtml(fmtDate(contract?.ngayBatDau) || "-")}</strong>
          </div>
          <div class="contract-info-item">
            <span>Ngay ket thuc</span>
            <strong>${escapeHtml(fmtDate(contract?.ngayKetThuc) || "-")}</strong>
          </div>
        </div>
      </article>`;
  }

  function render() {
    const total = contracts.length;

    if (countText) countText.textContent = `${total} hop dong duoc tim thay`;

    if (!total) {
      if (contractList) contractList.innerHTML = "";
      if (emptyState) emptyState.style.display = "";
      return;
    }

    if (emptyState) emptyState.style.display = "none";
    if (contractList) contractList.innerHTML = contracts.map(renderCard).join("");
  }

  async function fetchAllContracts() {
    const size = 50;
    let currentPage = 0;
    let totalPages = 1;
    const rows = [];

    while (currentPage < totalPages) {
      const pageData = await window.fetchPage(apiClient, "/api/tenant/hop-dong", {
        page: currentPage,
        size,
        sortBy: "ngayBatDau",
        direction: "desc",
      });
      rows.push(...(pageData?.content || []));
      totalPages = Math.max(1, Number(pageData?.totalPages || 1));
      currentPage += 1;
      if (currentPage > 500) break;
    }

    return rows.sort(byStartDateDesc);
  }

  async function loadContracts() {
    try {
      contracts = await fetchAllContracts();
    } catch (error) {
      console.error("Khong tai duoc hop dong:", error);
      contracts = [];
    }
    render();
  }

  document.addEventListener("DOMContentLoaded", loadContracts);
})();
