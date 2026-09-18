import { useEffect, useState } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { api } from "../api";
import { date, label, money } from "../utils";
import { Shell } from "../components/Layout";
import { ErrorMessage, Loading, Status } from "../components/ui";
import ResourcePage from "../components/ResourcePage";

export function TenantRoutes() {
  return <Shell kind="tenant"><Routes>
    <Route path="profile" element={<TenantProfile />} />
    <Route path="contracts" element={<TenantTable title="Hợp đồng của tôi" endpoint="/api/tenant/hop-dong" columns={[["Phòng", "tenPhong"], ["Ngày bắt đầu", (r) => date(r.ngayBatDau)], ["Ngày kết thúc", (r) => date(r.ngayKetThuc)], ["Trạng thái", (r) => <Status value={r.trangThai} />]]} />} />
    <Route path="invoices" element={<TenantTable title="Hóa đơn của tôi" endpoint="/api/tenant/hoa-don/search" columns={[["Phòng", "tenPhong"], ["Kỳ", "kyHoaDon"], ["Tổng tiền", (r) => money(r.tongTien)], ["Trạng thái", (r) => <Status value={r.trangThai} />]]} />} />
    <Route path="meters" element={<TenantTable title="Chỉ số điện nước" endpoint="/api/tenant/chi-so/search" columns={[["Phòng", "tenPhong"], ["Loại", (r) => label(r.loai)], ["Kỳ", "ky"], ["Tiêu thụ", "luongTieuThu"], ["Thành tiền", (r) => money(r.thanhTien)]]} />} />
    <Route path="payments" element={<TenantTable title="Lịch sử thanh toán" endpoint="/api/tenant/thanh-toan/search" columns={[["Số tiền", (r) => money(r.soTien)], ["Ngày thanh toán", (r) => date(r.ngayThanhToan)], ["Phương thức", "phuongThuc"], ["Trạng thái", (r) => <Status value={r.trangThai} />]]} />} />
    <Route path="*" element={<Navigate to="profile" replace />} />
  </Routes></Shell>;
}

function TenantTable({ title, endpoint, columns }) {
  return <ResourcePage config={{ title, endpoint, columns }} />;
}

function TenantProfile() {
  const [tenant, setTenant] = useState(null);
  const [contract, setContract] = useState(null);
  const [error, setError] = useState("");
  useEffect(() => { Promise.all([api("/api/tenant/profile"), api("/api/tenant/hop-dong/current")]).then(([person, active]) => { setTenant(person); setContract(active); }).catch((exception) => setError(exception.message)); }, []);
  if (error) return <ErrorMessage text={error} />;
  if (!tenant) return <Loading />;
  const details = [["Họ và tên", tenant.hoTen], ["Tên đăng nhập", tenant.tenDangNhap], ["Email", tenant.email], ["Số điện thoại", tenant.sdt], ["CCCD", tenant.cccd], ["Địa chỉ", tenant.diaChi], ["Phòng hiện tại", contract?.tenPhong || contract?.phongTro?.tenPhong || "Chưa có hợp đồng"], ["Trạng thái", label(tenant.trangThai)]];
  return <section className="profile-card"><div className="avatar">{tenant.hoTen?.split(" ").map((word) => word[0]).slice(-2).join("")}</div><div><h2>{tenant.hoTen}</h2><p className="muted">Thông tin tài khoản người thuê</p></div><Status value={tenant.trangThai} /><div className="profile-details">{details.map(([name, value]) => <div key={name}><span>{name}</span><strong>{value || "—"}</strong></div>)}</div></section>;
}
