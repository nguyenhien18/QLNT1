import { useEffect, useMemo, useState } from "react";
import { api, pageContent } from "../api";
import { CreateDialog, DataTable, ErrorMessage, Loading } from "./ui";

export default function ResourcePage({ config }) {
  const [rows, setRows] = useState([]);
  const [pending, setPending] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [showForm, setShowForm] = useState(false);

  const load = () => {
    setPending(true);
    setError("");
    const separator = config.endpoint.includes("?") ? "&" : "?";
    api(`${config.endpoint}${separator}page=0&size=100`)
      .then((data) => setRows(pageContent(data)))
      .catch((exception) => setError(exception.message))
      .finally(() => setPending(false));
  };

  useEffect(load, [config.endpoint]);
  const filteredRows = useMemo(() => rows.filter((row) => JSON.stringify(row).toLocaleLowerCase().includes(search.toLocaleLowerCase())), [rows, search]);

  return <section className="page-section"><div className="toolbar"><div className="search"><input placeholder="Tìm kiếm..." value={search} onChange={(event) => setSearch(event.target.value)} /></div>{config.createEndpoint && <button className="button primary" onClick={() => setShowForm(true)}>+ Thêm mới</button>}</div>{error ? <ErrorMessage text={error} /> : pending ? <Loading /> : <DataTable rows={filteredRows} config={config} reload={load} />}{showForm && <CreateDialog config={config} close={() => setShowForm(false)} reload={load} />}</section>;
}
