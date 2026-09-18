import { NavLink, useLocation, useNavigate } from "react-router-dom";
import { logout } from "../api";

export const adminNav = [
  ["/admin/dashboard", "Tổng quan"], ["/admin/rooms", "Phòng trọ"], ["/admin/tenants", "Khách thuê"],
  ["/admin/contracts", "Hợp đồng"], ["/admin/invoices", "Hóa đơn"], ["/admin/meters", "Chỉ số"],
  ["/admin/services", "Dịch vụ"], ["/admin/payments", "Thanh toán"],
];

export const tenantNav = [
  ["/tenant/profile", "Cá nhân"], ["/tenant/contracts", "Hợp đồng"], ["/tenant/invoices", "Hóa đơn"],
  ["/tenant/meters", "Chỉ số"], ["/tenant/payments", "Thanh toán"],
];

export function Shell({ kind, children }) {
  const navigate = useNavigate();
  const location = useLocation();
  const nav = kind === "admin" ? adminNav : tenantNav;
  const title = nav.find(([path]) => location.pathname === path)?.[1] || "Quản lý nhà trọ";

  async function signOut() {
    await logout();
    navigate("/login");
  }

  return <div className="app-shell"><aside className="sidebar">
    <div className="sidebar-brand"><span>QL</span><div><strong>QL Nhà Trọ</strong><small>{kind === "admin" ? "Chủ trọ" : "Người thuê"}</small></div></div>
    <nav>{nav.map(([path, text]) => <NavLink key={path} to={path} className={({ isActive }) => isActive ? "active" : ""}>{text}</NavLink>)}</nav>
    <button className="sign-out" onClick={signOut}>Đăng xuất</button>
  </aside><main className="content"><header><div><p className="eyebrow">{kind === "admin" ? "Khu vực quản trị" : "Cổng người thuê"}</p><h1>{title}</h1></div></header>{children}</main></div>;
}
