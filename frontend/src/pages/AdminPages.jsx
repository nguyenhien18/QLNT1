import { useEffect, useState } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { api } from "../api";
import { date, label, money } from "../utils";
import { Shell } from "../components/Layout";
import { ErrorMessage, Loading, Status } from "../components/ui";
import ResourcePage from "../components/ResourcePage";

const roomFields = [
  { name: "chuTroId", label: "ID chủ trọ", type: "number", required: true },
  { name: "tenPhong", label: "Tên phòng", required: true },
  { name: "loaiPhong", label: "Loại phòng", type: "select", options: ["THUONG", "VIP"], required: true },
  { name: "giaThue", label: "Giá thuê", type: "number", required: true },
  { name: "sucChua", label: "Sức chứa", type: "number", required: true },
  { name: "moTa", label: "Mô tả" },
  { name: "trangThai", label: "Trạng thái", type: "select", options: ["TRONG", "DA_CHO_THUE"] },
];

const tenantFields = [
  { name: "hoTen", label: "Họ tên", required: true }, { name: "cccd", label: "CCCD" },
  { name: "sdt", label: "Số điện thoại" }, { name: "email", label: "Email", type: "email" },
  { name: "ngaySinh", label: "Ngày sinh", type: "date" },
  { name: "gioiTinh", label: "Giới tính", type: "select", options: ["NAM", "NU"] },
  { name: "diaChi", label: "Địa chỉ" }, { name: "tenDangNhap", label: "Tên đăng nhập", required: true },
  { name: "matKhau", label: "Mật khẩu", type: "password", required: true },
  { name: "trangThai", label: "Trạng thái", type: "select", options: ["HOAT_DONG", "KHOA"] },
];

const adminPages = {
  rooms: {
    title: "Quản lý phòng", endpoint: "/api/phong-tro/search", createEndpoint: "/api/phong-tro", fields: roomFields,
    columns: [["Tên phòng", "tenPhong"], ["Loại", (r) => label(r.loaiPhong)], ["Giá thuê", (r) => money(r.giaThue)], ["Sức chứa", "sucChua"], ["Trạng thái", (r) => <Status value={r.trangThai} />]],
    id: "phongTroId", deleteEndpoint: (r) => `/api/phong-tro/${r.phongTroId}`,
  },
  tenants: {
    title: "Quản lý khách thuê", endpoint: "/api/khach-thue/search", createEndpoint: "/api/khach-thue", fields: tenantFields,
    columns: [["Họ tên", "hoTen"], ["Tài khoản", "tenDangNhap"], ["Điện thoại", "sdt"], ["Email", "email"], ["Trạng thái", (r) => <Status value={r.trangThai} />]],
    id: "khachThueId", deleteEndpoint: (r) => `/api/khach-thue/${r.khachThueId}`,
  },
  contracts: {
    title: "Quản lý hợp đồng", endpoint: "/api/hop-dong/search", createEndpoint: "/api/hop-dong",
    fields: [{ name: "phongTroId", label: "ID phòng", type: "number", required: true }, { name: "daiDienKhachThueId", label: "ID khách đại diện", type: "number", required: true }, { name: "ngayBatDau", label: "Ngày bắt đầu", type: "date", required: true }, { name: "ngayKetThuc", label: "Ngày kết thúc", type: "date" }, { name: "tienCoc", label: "Tiền cọc", type: "number" }],
    columns: [["Phòng", "tenPhong"], ["Đại diện", "tenKhachThue"], ["Bắt đầu", (r) => date(r.ngayBatDau)], ["Kết thúc", (r) => date(r.ngayKetThuc)], ["Trạng thái", (r) => <Status value={r.trangThai} />]],
    id: "hopDongId", extraAction: (r, reload) => r.trangThai === "CON_HIEU_LUC" && <button className="button outline" onClick={() => runAction(`/api/hop-dong/${r.hopDongId}/ket-thuc`, reload)}>Kết thúc</button>,
  },
  invoices: {
    title: "Quản lý hóa đơn", endpoint: "/api/hoa-don/search", createEndpoint: "/api/hoa-don",
    fields: [{ name: "hopDongId", label: "ID hợp đồng", type: "number", required: true }, { name: "kyHoaDon", label: "Kỳ hóa đơn", type: "month", required: true }, { name: "ngayLap", label: "Ngày lập", type: "date" }],
    columns: [["Phòng", "tenPhong"], ["Kỳ", "kyHoaDon"], ["Ngày lập", (r) => date(r.ngayLap)], ["Tổng tiền", (r) => money(r.tongTien)], ["Trạng thái", (r) => <Status value={r.trangThai} />]],
    id: "hoaDonId", deleteEndpoint: (r) => `/api/hoa-don/${r.hoaDonId}`, extraAction: (r, reload) => r.trangThai !== "DA_THANH_TOAN" && <button className="button outline" onClick={() => runAction(`/api/hoa-don/${r.hoaDonId}/da-thanh-toan`, reload)}>Đã thu</button>,
  },
  meters: {
    title: "Chỉ số điện nước", endpoint: "/api/chi-so/search", createEndpoint: "/api/chi-so",
    fields: [{ name: "hopDongId", label: "ID hợp đồng", type: "number", required: true }, { name: "loai", label: "Loại", type: "select", options: ["DIEN", "NUOC"], required: true }, { name: "ky", label: "Kỳ", type: "month", required: true }, { name: "thoiDiem", label: "Ngày ghi", type: "date" }, { name: "chiSoCu", label: "Chỉ số cũ", type: "number", required: true }, { name: "chiSoMoi", label: "Chỉ số mới", type: "number", required: true }, { name: "donGia", label: "Đơn giá", type: "number" }],
    columns: [["Phòng", "tenPhong"], ["Loại", (r) => label(r.loai)], ["Kỳ", "ky"], ["Cũ/Mới", (r) => `${r.chiSoCu} / ${r.chiSoMoi}`], ["Thành tiền", (r) => money(r.thanhTien)]],
    id: "chiSoId", deleteEndpoint: (r) => `/api/chi-so/${r.chiSoId}`,
  },
  services: {
    title: "Dịch vụ", endpoint: "/api/dich-vu", createEndpoint: "/api/dich-vu",
    fields: [{ name: "tenDichVu", label: "Tên dịch vụ", required: true }, { name: "giaDichVu", label: "Đơn giá", type: "number", required: true }, { name: "donViTinh", label: "Đơn vị tính" }],
    columns: [["Dịch vụ", "tenDichVu"], ["Đơn giá", (r) => money(r.giaDichVu)], ["Đơn vị", "donViTinh"]],
    id: "dichVuId", deleteEndpoint: (r) => `/api/dich-vu/${r.dichVuId}`,
  },
  payments: {
    title: "Theo dõi thanh toán", endpoint: "/api/thanh-toan/summary",
    columns: [["Phòng", "tenPhong"], ["Kỳ", "kyHoaDon"], ["Tổng tiền", (r) => money(r.tongTien)], ["Ngày thanh toán", (r) => date(r.ngayThanhToan)], ["Trạng thái", (r) => <Status value={r.trangThai} />]],
    id: "hoaDonId", extraAction: (r, reload) => r.trangThai !== "THANH_CONG" && <button className="button outline" onClick={() => runAction(`/api/thanh-toan/hoa-don/${r.hoaDonId}/xac-nhan`, reload)}>Xác nhận</button>,
  },
};

function runAction(path, reload) {
  api(path, { method: "PUT" }).then(reload).catch((error) => window.alert(error.message));
}

export function AdminRoutes() {
  return <Shell kind="admin"><Routes><Route path="dashboard" element={<Dashboard />} />{Object.entries(adminPages).map(([key, config]) => <Route key={key} path={key} element={<ResourcePage config={config} />} />)}<Route path="*" element={<Navigate to="dashboard" replace />} /></Routes></Shell>;
}

function Dashboard() {
  const [summary, setSummary] = useState(null);
  const [error, setError] = useState("");
  useEffect(() => { api("/api/thong-ke/tong-quan").then(setSummary).catch((exception) => setError(exception.message)); }, []);
  if (error) return <ErrorMessage text={error} />;
  if (!summary) return <Loading />;
  const cards = [["Tổng số phòng", summary.totalRooms, `${summary.rentedRooms} phòng đang thuê`], ["Phòng trống", summary.emptyRooms, "Sẵn sàng cho thuê"], ["Hóa đơn chưa thanh toán", summary.unpaidInvoices, "Cần theo dõi"], ["Hợp đồng sắp hết hạn", summary.expiringContracts, "Cần gia hạn"], ["Doanh thu tháng", money(summary.monthlyRevenue), "Theo hóa đơn đã thu"], ["Doanh thu năm", money(summary.yearlyRevenue), "Theo hóa đơn đã thu"]];
  return <><section className="stat-grid">{cards.map(([name, amount, description]) => <article className="stat-card" key={name}><p>{name}</p><strong>{amount}</strong><small>{description}</small></article>)}</section><section className="panel"><h2>Tình trạng phòng</h2><div className="progress-group"><div><span>Đã thuê</span><b>{summary.rentedRooms} / {summary.totalRooms}</b></div><progress value={summary.rentedRooms} max={Math.max(summary.totalRooms, 1)} /></div><div className="progress-group"><div><span>Phòng VIP đã thuê</span><b>{summary.rentedVipRooms} / {summary.totalVipRooms}</b></div><progress value={summary.rentedVipRooms} max={Math.max(summary.totalVipRooms, 1)} /></div></section></>;
}
