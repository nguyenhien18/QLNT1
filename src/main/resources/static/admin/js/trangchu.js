(function () {
const { api: apiClient, money: fmtMoney } = window.AppUtils || {};
const $ = (id) => document.getElementById(id);

document.addEventListener("DOMContentLoaded", async () => {
  try {
    const summary = await apiClient("/api/thong-ke/tong-quan");
    const total = Number(summary.totalRooms || 0);
    const rented = Number(summary.rentedRooms || 0);
    const empty = Number(summary.emptyRooms || Math.max(0, total - rented));
    const totalVip = Number(summary.totalVipRooms || 0);
    const rentedVip = Number(summary.rentedVipRooms || 0);
    const emptyRate = total ? ((empty / total) * 100).toFixed(1) : "0.0";

    $("totalRooms").textContent = total;
    $("totalRoomsSub").textContent = `${rented} dang thue`;

    $("vipRooms").textContent = totalVip;
    $("vipRoomsSub").textContent = `${rentedVip} dang thue`;

    $("emptyRooms").textContent = empty;
    $("emptyRoomsSub").textContent = `${emptyRate}% tong phong`;

    $("dueBills").textContent = Number(summary.unpaidInvoices || 0);
    $("expiringContracts").textContent = Number(summary.expiringContracts || 0);
    $("monthlyRevenue").textContent = fmtMoney(summary.monthlyRevenue || 0);
    $("yearRevenue").textContent = fmtMoney(summary.yearlyRevenue || 0);
  } catch (error) {
    console.error("Khong the tai dashboard summary:", error);
  }
});

})();
