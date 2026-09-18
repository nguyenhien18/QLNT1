import { useEffect, useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { api, authStore, login } from "../api";
import { Loading } from "../components/ui";

export function HomeRedirect() {
  return <Navigate to={authStore.role === "ADMIN" ? "/admin/dashboard" : authStore.role === "USER" ? "/tenant/profile" : "/login"} replace />;
}

export function Protected({ role, children }) {
  const [profile, setProfile] = useState(null);
  const [error, setError] = useState("");
  useEffect(() => {
    if (!authStore.token) return setError("Phiên đăng nhập đã hết hạn");
    api("/api/auth/me").then(setProfile).catch((exception) => {
      authStore.clear();
      setError(exception.message);
    });
  }, []);
  if (error) return <Navigate to="/login" replace />;
  if (!profile) return <Loading />;
  if (profile.role !== role) return <Navigate to={profile.role === "ADMIN" ? "/admin/dashboard" : "/tenant/profile"} replace />;
  return children;
}

export function LoginPage() {
  const navigate = useNavigate();
  const [values, setValues] = useState({ username: "", password: "" });
  const [error, setError] = useState("");
  const [pending, setPending] = useState(false);

  async function submit(event) {
    event.preventDefault();
    setPending(true);
    setError("");
    try {
      const result = await login(values.username, values.password);
      navigate(result.role === "ADMIN" ? "/admin/dashboard" : "/tenant/profile", { replace: true });
    } catch (exception) {
      setError(exception.message);
    } finally {
      setPending(false);
    }
  }

  return <main className="login-shell"><form className="login-card" onSubmit={submit}>
    <div className="brand-mark">QL</div><p className="eyebrow">Quản lý nhà trọ</p><h1>Đăng nhập</h1>
    <p className="muted">Dùng tài khoản chủ trọ hoặc người thuê để tiếp tục.</p>
    <label>Tài khoản<input required value={values.username} onChange={(event) => setValues({ ...values, username: event.target.value })} autoComplete="username" /></label>
    <label>Mật khẩu<input required type="password" value={values.password} onChange={(event) => setValues({ ...values, password: event.target.value })} autoComplete="current-password" /></label>
    {error && <p className="form-error">{error}</p>}
    <button className="button primary full" disabled={pending}>{pending ? "Đang đăng nhập..." : "Đăng nhập"}</button>
  </form></main>;
}
