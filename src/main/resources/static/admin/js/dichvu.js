const { api, money: fmtMoney } = window.AppUtils || {};

const $ = (id) => document.getElementById(id);
let services = [];

const unitMetaKey = "service_unit_meta";

function loadUnitMeta() {
  try { return JSON.parse(localStorage.getItem(unitMetaKey) || "{}"); }
  catch { return {}; }
}

function saveUnitMeta(v) {
  localStorage.setItem(unitMetaKey, JSON.stringify(v));
}

function openModal() {
  $("serviceModal").classList.add("show");
  $("serviceModal").setAttribute("aria-hidden", "false");
}

function closeModal() {
  $("serviceModal").classList.remove("show");
  $("serviceModal").setAttribute("aria-hidden", "true");
}

async function fetchServices() {
  services = await window.fetchAllPages(api, "/api/dich-vu", { sortBy: "dichVuId", direction: "desc" }).catch(() => []);
  render();
}

function render() {
  const list = $("serviceList");
  const meta = loadUnitMeta();

  if (!services.length) {
    list.innerHTML = '<div class="card">Chưa có dịch vụ nào.</div>';
    return;
  }

  list.innerHTML = services.map(s => `
    <div class="service-card" data-id="${s.dichVuId}">
      <h3>${s.tenDichVu}</h3>
      <p><b>Giá dịch vụ:</b> ${fmtMoney(s.giaDichVu)} ${meta[s.dichVuId] || ""}</p>
      <div class="service-actions">
        <button class="btn-edit" type="button" data-action="edit">Sửa</button>
        <button class="btn-delete" type="button" data-action="delete">Xóa</button>
      </div>
    </div>
  `).join("");
}

function resetForm() {
  $("serviceId").value = "";
  $("serviceForm").reset();
}

document.addEventListener("DOMContentLoaded", async () => {
  $("addServiceBtn").addEventListener("click", () => {
    resetForm();
    $("serviceTitle").textContent = "Thêm dịch vụ mới";
    openModal();
  });

  $("serviceClose").addEventListener("click", closeModal);
  $("serviceCancel").addEventListener("click", closeModal);
  $("serviceBackdrop").addEventListener("click", closeModal);

  $("serviceSave").addEventListener("click", async () => {
    const body = {
      tenDichVu: $("serviceName").value.trim(),
      giaDichVu: Number($("servicePrice").value || 0)
    };

    if (!body.tenDichVu) {
      alert("Vui lòng nhập tên dịch vụ");
      return;
    }

    try {
      let saved;
      const id = $("serviceId").value.trim();

      if (id) {
        saved = await api(`/api/dich-vu/${id}`, {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(body)
        });
      } else {
        saved = await api("/api/dich-vu", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(body)
        });
      }

      const meta = loadUnitMeta();
      meta[saved.dichVuId || id] = $("serviceUnit").value.trim();
      saveUnitMeta(meta);

      closeModal();
      resetForm();
      await fetchServices();
    } catch (e) {
      alert("Lưu dịch vụ thất bại: " + e.message);
    }
  });

  $("serviceList").addEventListener("click", async (e) => {
    const btn = e.target.closest("button");
    if (!btn) return;

    const card = e.target.closest(".service-card");
    if (!card?.dataset?.id) return;
    const id = Number(card.dataset.id);
    const s = services.find(x => x.dichVuId === id);
    if (!s) return;

    const meta = loadUnitMeta();

    if (btn.dataset.action === "edit") {
      $("serviceTitle").textContent = "Cập nhật dịch vụ";
      $("serviceId").value = s.dichVuId;
      $("serviceName").value = s.tenDichVu || "";
      $("servicePrice").value = s.giaDichVu || 0;
      $("serviceUnit").value = meta[s.dichVuId] || "";
      openModal();
    }

    if (btn.dataset.action === "delete") {
      if (!confirm(`Xóa dịch vụ "${s.tenDichVu}"?`)) return;
      try {
        await api(`/api/dich-vu/${id}`, { method: "DELETE" });
        delete meta[id];
        saveUnitMeta(meta);
        await fetchServices();
      } catch (err) {
        alert("Xóa dịch vụ thất bại: " + err.message);
      }
    }
  });

  await fetchServices();
});




