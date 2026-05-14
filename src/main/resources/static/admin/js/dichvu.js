(function () {
const { api: apiClient, money: fmtMoney } = window.AppUtils || {};
const { bindPagination } = window.PageFilters || {};

const $ = (id) => document.getElementById(id);
const PAGE_SIZE = 8;

let services = [];
let page = 1;
let totalPages = 1;
let totalItems = 0;

function openModal() {
  window.Modal?.open("serviceModal");
}

function closeModal() {
  window.Modal?.close("serviceModal");
}

async function fetchServices() {
  try {
    const pageData = await window.fetchPage(apiClient, "/api/dich-vu", {
      page: page - 1,
      size: PAGE_SIZE,
      sortBy: "dichVuId",
      direction: "desc",
    });
    services = pageData.content || [];
    totalItems = Number(pageData.totalElements || 0);
    totalPages = Math.max(1, Number(pageData.totalPages || 1));
    if (page > totalPages) {
      page = totalPages;
      return fetchServices();
    }
  } catch (error) {
    console.error("Khong tai duoc danh sach dich vu:", error);
    services = [];
    totalItems = 0;
    totalPages = 1;
  }
  render();
}

function render() {
  const list = $("serviceList");
  const start = totalItems ? (page - 1) * PAGE_SIZE + 1 : 0;
  const end = Math.min(page * PAGE_SIZE, totalItems);

  $("serviceCountText").textContent = `${totalItems} dich vu duoc tim thay`;
  $("servicePagingInfo").textContent = totalItems
    ? `Hien thi ${start} - ${end} / ${totalItems} dich vu`
    : "Hien thi 0 dich vu";
  $("pageInfo").textContent = `Trang ${page} / ${totalPages}`;
  $("prevPageBtn").disabled = page <= 1;
  $("nextPageBtn").disabled = page >= totalPages;

  if (!services.length) {
    list.innerHTML = '<div class="card">Chua co dich vu nao.</div>';
    return;
  }

  list.innerHTML = services.map((service) => `
    <div class="service-card" data-id="${service.dichVuId}">
      <h3>${service.tenDichVu || ""}</h3>
      <p><b>Gia dich vu:</b> ${fmtMoney(service.giaDichVu)} ${service.donViTinh || ""}</p>
      <div class="service-actions">
        <button class="btn-edit" type="button" data-action="edit">Sua</button>
        <button class="btn-delete" type="button" data-action="delete">Xoa</button>
      </div>
    </div>
  `).join("");
}

function resetForm() {
  $("serviceId").value = "";
  $("serviceForm").reset();
}

async function saveService() {
  const body = {
    tenDichVu: $("serviceName").value.trim(),
    giaDichVu: Number($("servicePrice").value || 0),
    donViTinh: $("serviceUnit").value.trim() || null,
  };

  if (!body.tenDichVu) {
    alert("Vui long nhap ten dich vu");
    return;
  }

  try {
    const id = $("serviceId").value.trim();
    if (id) {
      await apiClient(`/api/dich-vu/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });
    } else {
      await apiClient("/api/dich-vu", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });
    }

    closeModal();
    resetForm();
    await fetchServices();
  } catch (error) {
    alert(`Luu dich vu that bai: ${error.message}`);
  }
}

document.addEventListener("DOMContentLoaded", async () => {
  $("addServiceBtn").addEventListener("click", () => {
    resetForm();
    $("serviceTitle").textContent = "Them dich vu moi";
    openModal();
  });

  $("serviceClose").addEventListener("click", closeModal);
  $("serviceCancel").addEventListener("click", closeModal);
  $("serviceBackdrop").addEventListener("click", closeModal);
  $("serviceSave").addEventListener("click", saveService);

  $("serviceList").addEventListener("click", async (event) => {
    const button = event.target.closest("button");
    if (!button) return;

    const card = event.target.closest(".service-card");
    if (!card?.dataset?.id) return;
    const id = Number(card.dataset.id);
    const service = services.find((item) => item.dichVuId === id);
    if (!service) return;

    if (button.dataset.action === "edit") {
      $("serviceTitle").textContent = "Cap nhat dich vu";
      $("serviceId").value = service.dichVuId;
      $("serviceName").value = service.tenDichVu || "";
      $("servicePrice").value = service.giaDichVu || 0;
      $("serviceUnit").value = service.donViTinh || "";
      openModal();
      return;
    }

    if (button.dataset.action === "delete") {
      if (!confirm(`Xoa dich vu "${service.tenDichVu}"?`)) return;
      try {
        await apiClient(`/api/dich-vu/${id}`, { method: "DELETE" });
        await fetchServices();
      } catch (error) {
        alert(`Xoa dich vu that bai: ${error.message}`);
      }
    }
  });

  bindPagination?.("prevPageBtn", "nextPageBtn", {
    get page() { return page; },
    set page(v) { page = v; },
    get totalPages() { return totalPages; },
  }, fetchServices);

  await fetchServices();
});

})();
