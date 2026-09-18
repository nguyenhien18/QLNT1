import { useEffect, useMemo, useState } from "react";
import { NavLink, Navigate, Route, Routes, useLocation, useNavigate } from "react-router-dom";
import { api, authStore, login, logout, pageContent } from "./api";
import { date, label, money, toPayload, valueForField } from "./utils";

const adminNav = [
  ["/admin/dashboard", "Tổng quan"],
  ["/admin/rooms", "Phòng trọ"],
  ["/admin/tenants", "Khách thuê"],
  ["/admin/contracts", "Hợp đồng"],
  ["/admin/invoices", "Hóa đơn"],
  ["/admin/meters", "Chỉ số"],
  ["/admin/services", "Dịch vụ"],
  ["/admin/payments", "Thanh toán"],
];

const tenantNav = [
  ["/tenant/profile", "Cá nhân"],
  ["/tenant/contracts", "Hợp đồng"],
  ["/tenant/invoices", "Hóa đơn"],
  ["/tenant/meters", "Chỉ số"],
  ["/tenant/payments", "Thanh toán"],
];

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
  { name: "hoTen", label: "Họ tên", required: true },
  { name: "cccd", label: "CCCD" },
  { name: "sdt", label: "Số điện thoại" },
  { name: "email", label: "Email", type: "email" },
  { name: "ngaySinh", label: "Ngày sinh", type: "date" },
  { name: "gioiTinh", label: "Giới tính", type: "select", options: ["NAM", "NU"] },
  { name: "diaChi", label: "Địa chỉ" },
  { name: "tenDangNhap", label: "Tên đăng nhập", required: true },
  { name: "matKhau", label: "Mật khẩu", type: "password", required: true },
  { name: "trangThai", label: "Trạng thái", type: "select", options: ["HOAT_DONG", "KHOA"] },
];

const adminPages = {
  rooms: {
    title: "Quản lý phòng",
    endpoint: "/api/phong-tro/search",
    createEndpoint: "/api/phong-tro",
    fields: roomFields,
    columns: [["Tên phòng", "tenPhong"], ["Loại", (r) => label(r.loaiPhong)], ["Giá thuê", (r) => money(r.giaThue)], ["Sức chứa", "sucChua"], ["Trạng thái", (r) => <Status value={r.trangThai} />]],
    id: "phongTroId",
    deleteEndpoint: (r) => `/api/phong-tro/${r.phongTroId}`,
  },
  tenants: {
    title: "Quản lý khách thuê",
    endpoint: "/api/khach-thue/search",
    createEndpoint: "/api/khach-thue",
    fields: tenantFields,
    columns: [["Họ tên", "hoTen"], ["Tài khoản", "tenDangNhap"], ["Điện thoại", "sdt"], ["Email", "email"], ["Trạng thái", (r) => <Status value={r.trangThai} />]],
    id: "khachThueId",
    deleteEndpoint: (r) => `/api/khach-thue/${r.khachThueId}`,
  },
  contracts: {
    title: "Quản lý hợp đồng",
    endpoint: "/api/hop-dong/search",
    createEndpoint: "/api/hop-dong",
    fields: [
      { name: "phongTroId", label: "ID phòng", type: "number", required: true },
      { name: "daiDienKhachThueId", label: "ID khách đại diện", type: "number", required: true },
      { name: "ngayBatDau", label: "Ngày bắt đầu", type: "date", required: true },
      { name: "ngayKetThuc", label: "Ngày kết thúc", type: "date" },
      { name: "tienCoc", label: "Tiền cọc", type: "number" },
    ],
    columns: [["Phòng", "tenPhong"], ["Đại diện", "tenKhachThue"], ["Bắt đầu", (r) => date(r.ngayBatDau)], ["Kết thúc", (r) => date(r.ngayKetThuc)], ["Trạng thái", (r) => <Status value={r.trangThai} />]],
    id: "hopDongId",
    extraAction: (r, reload) => r.trangThai === "CON_HIEU_LUC" && <button className="button outline" onClick={() => runAction(`/api/hop-dong/${r.hopDongId}/ket-thuc`, reload)}>Kết thúc</button>,
  },
  invoices: {
    title: "Quản lý hóa đơn",
    endpoint: "/api/hoa-don/search",
    createEndpoint: "/api/hoa-don",
    fields: [
      { name: "hopDongId", label: "ID hợp đồng", type: "number", required: true },
      { name: "kyHoaDon", label: "Kỳ hóa đơn", type: "month", required: true },
      { name: "ngayLap", label: "Ngày lập", type: "date" },
    ],
    columns: [["Phòng", "tenPhong"], ["Kỳ", "kyHoaDon"], ["Ngày lập", (r) => date(r.ngayLap)], ["Tổng tiền", (r) => money(r.tongTien)], ["Trạng thái", (r) => <Status value={r.trangThai} />]],
    id: "hoaDonId",
    deleteEndpoint: (r) => `/api/hoa-don/${r.hoaDonId}`,
    extraAction: (r, reload) => r.trangThai !== "DA_THANH_TOAN" && <button className="button outline" onClick={() => runAction(`/api/hoa-don/${r.hoaDonId}/da-thanh-toan`, reload)}>Đã thu</button>,
  },
  meters: {
    title: "Chỉ số điện nước",
    endpoint: "/api/chi-so/search",
    createEndpoint: "/api/chi-so",
    fields: [
      { name: "hopDongId", label: "ID hợp đồng", type: "number", required: true },
      { name: "loai", label: "Loại", type: "select", options: ["DIEN", "NUOC"], required: true },
      { name: "ky", label: "Kỳ", type: "month", required: true },
      { name: "thoiDiem", label: "Ngày ghi", type: "date" },
      { name: "chiSoCu", label: "Chỉ số cũ", type: "number", required: true },
      { name: "chiSoMoi", label: "Chỉ số mới", type: "number", required: true },
      { name: "donGia", label: "Đơn giá", type: "number" },
    ],
    columns: [["Phòng", "tenPhong"], ["Loại", (r) => label(r.loai)], ["Kỳ", "ky"], ["Cũ/Mới", (r) => `${r.chiSoCu} / ${r.chiSoMoi}`], ["Thành tiền", (r) => money(r.thanhTien)]],
    id: "chiSoId",
    deleteEndpoint: (r) => `/api/chi-so/${r.chiSoId}`,
  },
  services: {
    title: "Dịch vụ",
    endpoint: "/api/dich-vu",
    createEndpoint: "/api/dich-vu",
    fields: [{ name: "tenDichVu", label: "Tên dịch vụ", required: true }, { name: "giaDichVu", label: "Đơn giá", type: "number", required: true }, { name: "donViTinh", label: "Đơn vị tính" }],
    columns: [["Dịch vụ", "tenDichVu"], ["Đơn giá", (r) => money(r.giaDichVu)], ["Đơn vị", "donViTinh"]],
    id: "dichVuId",
    deleteEndpoint: (r) => `/api/dich-vu/${r.dichVuId}`,
  },
  payments: {
    title: "Theo dõi thanh toán",
    endpoint: "/api/thanh-toan/summary",
    columns: [["Phòng", "tenPhong"], ["Kỳ", "kyHoaDon"], ["Tổng tiền", (r) => money(r.tongTien)], ["Ngày thanh toán", (r) => date(r.ngayThanhToan)], ["Trạng thái", (r) => <Status value={r.trangThai} />]],
    id: "hoaDonId",
    extraAction: (r, reload) => r.trangThai !== "THANH_CONG" && <button className="button outline" onClick={() => runAction(`/api/thanh-toan/hoa-don/${r.hoaDonId}/xac-nhan`, reload)}>Xác nhận</button>,
  },
};

function runAction(path, reload) {
  api(path, { method: "PUT" }).then(reload).catch((error) => window.alert(error.message));
}

function Status({ value }) {
  return <span className={`status ${String(value || "").toLowerCase()}`}>{label(value)}</span>;
}

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/admin/*" element={<Protected role="ADMIN"><AdminRoutes /></Protected>} />
      <Route path="/tenant/*" element={<Protected role="USER"><TenantRoutes /></Protected>} />
      <Route path="*" element={<HomeRedirect />} />
    </Routes>
  );
}

function HomeRedirect() {
  return <Navigate to={authStore.role === "ADMIN" ? "/admin/dashboard" : authStore.role === "USER" ? "/tenant/profile" : "/login"} replace />;
}

function Protected({ role, children }) {
  const [profile, setProfile] = useState(null);
  const [error, setError] = useState("");
  useEffect(() => {
    if (!authStore.token) return setError("Phiên đăng nhập đã hết hạn");
    api("/api/auth/me").then(setProfile).catch((e) => {
      authStore.clear();
      setError(e.message);
    });
  }, []);
  if (error) return <Navigate to="/login" replace />;
  if (!profile) return <Loading />;
  if (profile.role !== role) return <Navigate to={profile.role === "ADMIN" ? "/admin/dashboard" : "/tenant/profile"} replace />;
  return children;
}

function LoginPage() {
  const navigate = useNavigate();
  const [values, setValues] = useState({ username: "", password: "" });
  const [error, setError] = useState("");
  const [pending, setPending] = useState(false);
  async function submit(event) {
    event.preventDefault();
    setPending(true); setError("");
    try {
      const result = await login(values.username, values.password);
      navigate(result.role === "ADMIN" ? "/admin/dashboard" : "/tenant/profile", { replace: true });
    } catch (e) { setError(e.message); } finally { setPending(false); }
  }
  return <main className="login-shell"><form className="login-card" onSubmit={submit}>
    <div className="brand-mark">QL</div><p className="eyebrow">Quản lý nhà trọ</p><h1>Đăng nhập</h1>
    <p className="muted">Dùng tài khoản chủ trọ hoặc người thuê để tiếp tục.</p>
    <label>Tài khoản<input required value={values.username} onChange={(e) => setValues({ ...values, username: e.target.value })} autoComplete="username" /></label>
    <label>Mật khẩu<input required type="password" value={values.password} onChange={(e) => setValues({ ...values, password: e.target.value })} autoComplete="current-password" /></label>
    {error && <p className="form-error">{error}</p>}
    <button className="button primary full" disabled={pending}>{pending ? "Đang đăng nhập..." : "Đăng nhập"}</button>
  </form></main>;
}

function Shell({ kind, children }) {
  const navigate = useNavigate();
  const location = useLocation();
  const nav = kind === "admin" ? adminNav : tenantNav;
  const title = nav.find(([path]) => location.pathname === path)?.[1] || "Quản lý nhà trọ";
  async function signOut() { await logout(); navigate("/login"); }
  return <div className="app-shell">
    <aside className="sidebar"><div className="sidebar-brand"><span>QL</span><div><strong>QL Nhà Trọ</strong><small>{kind === "admin" ? "Chủ trọ" : "Người thuê"}</small></div></div>
      <nav>{nav.map(([path, text]) => <NavLink key={path} to={path} className={({ isActive }) => isActive ? "active" : ""}>{text}</NavLink>)}</nav>
      <button className="sign-out" onClick={signOut}>Đăng xuất</button>
    </aside>
    <main className="content"><header><div><p className="eyebrow">{kind === "admin" ? "Khu vực quản trị" : "Cổng người thuê"}</p><h1>{title}</h1></div></header>{children}</main>
  </div>;
}

function AdminRoutes() {
  return <Shell kind="admin"><Routes>
    <Route path="dashboard" element={<Dashboard />} />
    {Object.entries(adminPages).map(([key, config]) => <Route key={key} path={key} element={<ResourcePage config={config} />} />)}
    <Route path="*" element={<Navigate to="dashboard" replace />} />
  </Routes></Shell>;
}

function TenantRoutes() {
  return <Shell kind="tenant"><Routes>
    <Route path="profile" element={<TenantProfile />} />
    <Route path="contracts" element={<TenantTable title="Hợp đồng của tôi" endpoint="/api/tenant/hop-dong" columns={[["Phòng", "tenPhong"], ["Ngày bắt đầu", (r) => date(r.ngayBatDau)], ["Ngày kết thúc", (r) => date(r.ngayKetThuc)], ["Trạng thái", (r) => <Status value={r.trangThai} />]]} />} />
    <Route path="invoices" element={<TenantTable title="Hóa đơn của tôi" endpoint="/api/tenant/hoa-don/search" columns={[["Phòng", "tenPhong"], ["Kỳ", "kyHoaDon"], ["Tổng tiền", (r) => money(r.tongTien)], ["Trạng thái", (r) => <Status value={r.trangThai} />]]} />} />
    <Route path="meters" element={<TenantTable title="Chỉ số điện nước" endpoint="/api/tenant/chi-so/search" columns={[["Phòng", "tenPhong"], ["Loại", (r) => label(r.loai)], ["Kỳ", "ky"], ["Tiêu thụ", "luongTieuThu"], ["Thành tiền", (r) => money(r.thanhTien)]]} />} />
    <Route path="payments" element={<TenantTable title="Lịch sử thanh toán" endpoint="/api/tenant/thanh-toan/search" columns={[["Số tiền", (r) => money(r.soTien)], ["Ngày thanh toán", (r) => date(r.ngayThanhToan)], ["Phương thức", "phuongThuc"], ["Trạng thái", (r) => <Status value={r.trangThai} />]]} />} />
    <Route path="*" element={<Navigate to="profile" replace />} />
  </Routes></Shell>;
}

function Dashboard() {
  const [summary, setSummary] = useState(null);
  const [error, setError] = useState("");
  useEffect(() => { api("/api/thong-ke/tong-quan").then(setSummary).catch((e) => setError(e.message)); }, []);
  if (error) return <ErrorMessage text={error} />;
  if (!summary) return <Loading />;
  const cards = [
    ["Tổng số phòng", summary.totalRooms, `${summary.rentedRooms} phòng đang thuê`],
    ["Phòng trống", summary.emptyRooms, "Sẵn sàng cho thuê"],
    ["Hóa đơn chưa thanh toán", summary.unpaidInvoices, "Cần theo dõi"],
    ["Hợp đồng sắp hết hạn", summary.expiringContracts, "Cần gia hạn"],
    ["Doanh thu tháng", money(summary.monthlyRevenue), "Theo hóa đơn đã thu"],
    ["Doanh thu năm", money(summary.yearlyRevenue), "Theo hóa đơn đã thu"],
  ];
  return <><section className="stat-grid">{cards.map(([name, amount, description]) => <article className="stat-card" key={name}><p>{name}</p><strong>{amount}</strong><small>{description}</small></article>)}</section>
    <section className="panel"><h2>Tình trạng phòng</h2><div className="progress-group"><div><span>Đã thuê</span><b>{summary.rentedRooms} / {summary.totalRooms}</b></div><progress value={summary.rentedRooms} max={Math.max(summary.totalRooms, 1)} /></div>
      <div className="progress-group"><div><span>Phòng VIP đã thuê</span><b>{summary.rentedVipRooms} / {summary.totalVipRooms}</b></div><progress value={summary.rentedVipRooms} max={Math.max(summary.totalVipRooms, 1)} /></div></section></>;
}

function ResourcePage({ config }) {
  const [rows, setRows] = useState([]);
  const [pending, setPending] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [showForm, setShowForm] = useState(false);
  const load = () => {
    setPending(true); setError("");
    const separator = config.endpoint.includes("?") ? "&" : "?";
    api(`${config.endpoint}${separator}page=0&size=100`).then((data) => setRows(pageContent(data))).catch((e) => setError(e.message)).finally(() => setPending(false));
  };
  useEffect(load, [config.endpoint]);
  const filteredRows = useMemo(() => rows.filter((row) => JSON.stringify(row).toLocaleLowerCase().includes(search.toLocaleLowerCase())), [rows, search]);
  return <section className="page-section"><div className="toolbar"><div className="search"><input placeholder="Tìm kiếm..." value={search} onChange={(e) => setSearch(e.target.value)} /></div>{config.createEndpoint && <button className="button primary" onClick={() => setShowForm(true)}>+ Thêm mới</button>}</div>
    {error ? <ErrorMessage text={error} /> : pending ? <Loading /> : <DataTable rows={filteredRows} config={config} reload={load} />}
    {showForm && <CreateDialog config={config} close={() => setShowForm(false)} reload={load} />}
  </section>;
}

function TenantTable({ title, endpoint, columns }) {
  const config = { title, endpoint, columns };
  return <section className="page-section"><div className="table-heading"><h2>{title}</h2></div><ResourcePage config={config} /></section>;
}

function DataTable({ rows, config, reload }) {
  async function remove(row) {
    if (!window.confirm("Bạn có chắc muốn xóa bản ghi này?")) return;
    try { await api(config.deleteEndpoint(row), { method: "DELETE" }); reload(); } catch (e) { window.alert(e.message); }
  }
  return <div className="table-wrap"><table><thead><tr>{config.columns.map(([name]) => <th key={name}>{name}</th>)}{(config.deleteEndpoint || config.extraAction) && <th>Thao tác</th>}</tr></thead>
    <tbody>{rows.length === 0 ? <tr><td colSpan={config.columns.length + 1} className="empty">Chưa có dữ liệu</td></tr> : rows.map((row, index) => <tr key={row[config.id] || index}>{config.columns.map(([name, accessor]) => <td key={name}>{typeof accessor === "function" ? accessor(row) : row[accessor] || "—"}</td>)}{(config.deleteEndpoint || config.extraAction) && <td className="actions">{config.extraAction?.(row, reload)}{config.deleteEndpoint && <button className="button danger" onClick={() => remove(row)}>Xóa</button>}</td>}</tr>)}</tbody></table></div>;
}

function CreateDialog({ config, close, reload }) {
  const [values, setValues] = useState(Object.fromEntries(config.fields.map((field) => [field.name, ""])));
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
  async function submit(event) {
    event.preventDefault(); setSaving(true); setError("");
    try {
      const payload = toPayload(values, config.fields);
      if (config.title === "Quản lý hợp đồng") payload.thanhVienKhachThueIds = [];
      await api(config.createEndpoint, { method: "POST", body: payload });
      reload(); close();
    } catch (e) { setError(e.message); } finally { setSaving(false); }
  }
  return <div className="dialog-backdrop" role="presentation"><form className="dialog" onSubmit={submit}><div className="dialog-header"><h2>Thêm {config.title.toLowerCase()}</h2><button type="button" className="icon-button" onClick={close}>×</button></div>
    <div className="form-grid">{config.fields.map((field) => <label key={field.name}>{field.label}<Field field={field} value={values[field.name]} onChange={(value) => setValues({ ...values, [field.name]: value })} /></label>)}</div>
    {error && <p className="form-error">{error}</p>}<div className="dialog-actions"><button type="button" className="button secondary" onClick={close}>Hủy</button><button className="button primary" disabled={saving}>{saving ? "Đang lưu..." : "Lưu"}</button></div>
  </form></div>;
}

function Field({ field, value, onChange }) {
  if (field.type === "select") return <select required={field.required} value={value} onChange={(e) => onChange(e.target.value)}><option value="">Chọn</option>{field.options.map((option) => <option key={option} value={option}>{label(option)}</option>)}</select>;
  return <input required={field.required} type={field.type || "text"} value={valueForField(value, field)} onChange={(e) => onChange(e.target.value)} />;
}

function TenantProfile() {
  const [tenant, setTenant] = useState(null);
  const [contract, setContract] = useState(null);
  const [error, setError] = useState("");
  useEffect(() => { Promise.all([api("/api/tenant/profile"), api("/api/tenant/hop-dong/current")]).then(([person, active]) => { setTenant(person); setContract(active); }).catch((e) => setError(e.message)); }, []);
  if (error) return <ErrorMessage text={error} />; if (!tenant) return <Loading />;
  const details = [["Họ và tên", tenant.hoTen], ["Tên đăng nhập", tenant.tenDangNhap], ["Email", tenant.email], ["Số điện thoại", tenant.sdt], ["CCCD", tenant.cccd], ["Địa chỉ", tenant.diaChi], ["Phòng hiện tại", contract?.tenPhong || contract?.phongTro?.tenPhong || "Chưa có hợp đồng"], ["Trạng thái", label(tenant.trangThai)]];
  return <section className="profile-card"><div className="avatar">{tenant.hoTen?.split(" ").map((word) => word[0]).slice(-2).join("")}</div><div><h2>{tenant.hoTen}</h2><p className="muted">Thông tin tài khoản người thuê</p></div><Status value={tenant.trangThai} />
    <div className="profile-details">{details.map(([name, value]) => <div key={name}><span>{name}</span><strong>{value || "—"}</strong></div>)}</div></section>;
}

function Loading() { return <div className="loading">Đang tải dữ liệu...</div>; }
function ErrorMessage({ text }) { return <div className="error-box">{text}</div>; }

export default App;
