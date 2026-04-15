const { api, money: fmtMoney } = window.AppUtils || {};
const $ = (id) => document.getElementById(id);

document.addEventListener("DOMContentLoaded", async () => {
  try {
    const [rooms, contracts, bills, payments] = await Promise.all([
      window.fetchAllPages(api, "/api/phong-tro", { sortBy: "phongTroId", direction: "desc" }).catch(() => []),
      window.fetchAllPages(api, "/api/hop-dong", { sortBy: "ngayBatDau", direction: "desc" }).catch(() => []),
      window.fetchAllPages(api, "/api/hoa-don", { sortBy: "ngayLap", direction: "desc" }).catch(() => []),
      window.fetchAllPages(api, "/api/thanh-toan", { sortBy: "ngayThanhToan", direction: "desc" }).catch(() => [])
    ]);

    const total = rooms.length;
    const rented = rooms.filter(r => r.trangThai === "DA_CHO_THUE").length;
    const empty = rooms.filter(r => r.trangThai === "TRONG").length;
    const emptyRate = total ? ((empty / total) * 100).toFixed(1) : "0.0";

    $("totalRooms").textContent = total;
    $("totalRoomsSub").textContent = `${rented} đang thuê`;

    $("emptyRooms").textContent = empty;
    $("emptyRoomsSub").textContent = `${emptyRate}% tổng phòng`;

    const unpaid = bills.filter(b => b.trangThai === "CHUA_THANH_TOAN").length;
    $("dueBills").textContent = unpaid;

    const today = new Date();
    const expiring = contracts.filter(c => {
      if (c.trangThai !== "CON_HIEU_LUC" || !c.ngayKetThuc) return false;
      const end = new Date(c.ngayKetThuc);
      const diff = (end - today) / 86400000;
      return diff >= 0 && diff <= 30;
    }).length;
    $("expiringContracts").textContent = expiring;

    const now = new Date();
    let monthRevenue = 0;
    let yearRevenue = 0;

    payments.forEach(payment => {
      if (payment.trangThai !== "THANH_CONG") return;
      const d = new Date(payment.ngayThanhToan);
      if (Number.isNaN(d.getTime())) return;
      const amount = Number(payment.soTien || payment.hoaDon?.tongTien || 0);

      if (d.getFullYear() === now.getFullYear()) {
        yearRevenue += amount;
        if (d.getMonth() === now.getMonth()) {
          monthRevenue += amount;
        }
      }
    });

    $("monthlyRevenue").textContent = fmtMoney(monthRevenue);
    $("yearRevenue").textContent = fmtMoney(yearRevenue);
  } catch (e) {
    console.error(e);
  }
});






