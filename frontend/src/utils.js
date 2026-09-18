export const money = (value) =>
  new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND", maximumFractionDigits: 0 })
    .format(Number(value || 0));

export const date = (value) => {
  if (!value) return "—";
  const raw = String(value).slice(0, 10);
  const [year, month, day] = raw.split("-");
  return year && month && day ? `${day}/${month}/${year}` : String(value);
};

export const label = (value) => String(value || "—").replaceAll("_", " ").toLowerCase()
  .replace(/\b\w/g, (letter) => letter.toUpperCase());

export function valueForField(value, field) {
  if (value === null || value === undefined) return "";
  return field?.type === "date" ? String(value).slice(0, 10) : String(value);
}

export function toPayload(values, fields) {
  return Object.fromEntries(fields.map((field) => {
    const raw = values[field.name];
    if (raw === "" || raw === undefined) return [field.name, null];
    if (field.type === "number") return [field.name, Number(raw)];
    return [field.name, raw];
  }));
}
