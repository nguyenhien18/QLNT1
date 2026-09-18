import { useState } from "react";
import { api } from "../api";
import { label, toPayload, valueForField } from "../utils";

export function Status({ value }) {
  return <span className={`status ${String(value || "").toLowerCase()}`}>{label(value)}</span>;
}

export function Loading() {
  return <div className="loading">Đang tải dữ liệu...</div>;
}

export function ErrorMessage({ text }) {
  return <div className="error-box">{text}</div>;
}

export function Field({ field, value, onChange }) {
  if (field.type === "select") {
    return <select required={field.required} value={value} onChange={(event) => onChange(event.target.value)}>
      <option value="">Chọn</option>
      {field.options.map((option) => <option key={option} value={option}>{label(option)}</option>)}
    </select>;
  }
  return <input required={field.required} type={field.type || "text"} value={valueForField(value, field)} onChange={(event) => onChange(event.target.value)} />;
}

export function DataTable({ rows, config, reload }) {
  async function remove(row) {
    if (!window.confirm("Bạn có chắc muốn xóa bản ghi này?")) return;
    try {
      await api(config.deleteEndpoint(row), { method: "DELETE" });
      reload();
    } catch (error) {
      window.alert(error.message);
    }
  }

  const hasActions = config.deleteEndpoint || config.extraAction;
  return <div className="table-wrap"><table><thead><tr>
    {config.columns.map(([name]) => <th key={name}>{name}</th>)}
    {hasActions && <th>Thao tác</th>}
  </tr></thead><tbody>
    {rows.length === 0
      ? <tr><td colSpan={config.columns.length + (hasActions ? 1 : 0)} className="empty">Chưa có dữ liệu</td></tr>
      : rows.map((row, index) => <tr key={row[config.id] || index}>
        {config.columns.map(([name, accessor]) => <td key={name}>{typeof accessor === "function" ? accessor(row) : row[accessor] || "—"}</td>)}
        {hasActions && <td className="actions">{config.extraAction?.(row, reload)}{config.deleteEndpoint && <button className="button danger" onClick={() => remove(row)}>Xóa</button>}</td>}
      </tr>)}
  </tbody></table></div>;
}

export function CreateDialog({ config, close, reload }) {
  const [values, setValues] = useState(Object.fromEntries(config.fields.map((field) => [field.name, ""])));
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  async function submit(event) {
    event.preventDefault();
    setSaving(true);
    setError("");
    try {
      const payload = toPayload(values, config.fields);
      if (config.title === "Quản lý hợp đồng") payload.thanhVienKhachThueIds = [];
      await api(config.createEndpoint, { method: "POST", body: payload });
      reload();
      close();
    } catch (exception) {
      setError(exception.message);
    } finally {
      setSaving(false);
    }
  }

  return <div className="dialog-backdrop" role="presentation"><form className="dialog" onSubmit={submit}>
    <div className="dialog-header"><h2>Thêm {config.title.toLowerCase()}</h2><button type="button" className="icon-button" onClick={close}>×</button></div>
    <div className="form-grid">{config.fields.map((field) => <label key={field.name}>{field.label}
      <Field field={field} value={values[field.name]} onChange={(value) => setValues({ ...values, [field.name]: value })} />
    </label>)}</div>
    {error && <p className="form-error">{error}</p>}
    <div className="dialog-actions"><button type="button" className="button secondary" onClick={close}>Hủy</button><button className="button primary" disabled={saving}>{saving ? "Đang lưu..." : "Lưu"}</button></div>
  </form></div>;
}
