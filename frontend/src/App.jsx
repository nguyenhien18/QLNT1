import { Route, Routes } from "react-router-dom";
import { HomeRedirect, LoginPage, Protected } from "./pages/AuthPages";
import { AdminRoutes } from "./pages/AdminPages";
import { TenantRoutes } from "./pages/TenantPages";

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

export default App;
