(function () {
  const isFileProtocol = window.location.protocol === "file:";
  const originBase = window.location.protocol.startsWith("http") ? window.location.origin : "";

  const defaultBase = isFileProtocol
    ? "http://localhost:8087"
    : (originBase || "http://localhost:8087");

  window.API_BASE = defaultBase;
  if (!isFileProtocol && originBase) {
    window.localStorage.setItem("QLPT_API_BASE", originBase);
  }

  function redirectToLoginIfUnauthorized(status, path) {
    if (status !== 401) return;

    const pathname = window.location.pathname || "";
    const isProtectedPage = pathname.includes("/admin/") || pathname.includes("/nguoithue/");
    const isLoginPage = pathname.includes("/dangnhap/login.html");
    if (!isProtectedPage || isLoginPage) return;

    const normalizedPath = String(path || "").toLowerCase();
    const isAuthCheck = normalizedPath.startsWith("/api/auth/me");
    if (!isAuthCheck) return;

    const hasCachedToken = Boolean(window.localStorage.getItem("QLPT_AUTH_TOKEN"));
    const hasCachedRole = Boolean(window.localStorage.getItem("QLPT_AUTH_ROLE"));
    if (hasCachedToken || hasCachedRole) return;

    window.location.href = `${window.API_BASE}/dangnhap/login.html`;
  }

  function normalizeLegacyShape(input) {
    if (Array.isArray(input)) {
      return input.map(normalizeLegacyShape);
    }
    if (!input || typeof input !== "object") {
      return input;
    }

    const data = {};
    Object.keys(input).forEach((key) => {
      data[key] = normalizeLegacyShape(input[key]);
    });

    if (!data.hopDong && data.hopDongId != null) {
      data.hopDong = { hopDongId: data.hopDongId };
    }

    if (!data.hoaDon && data.hoaDonId != null) {
      data.hoaDon = {
        hoaDonId: data.hoaDonId,
        tongTien: data.tongTien ?? null,
        kyHoaDon: data.kyHoaDon ?? null,
      };
    }

    if (!data.phongTro && (data.phongTroId != null || data.tenPhong != null || data.giaThue != null)) {
      data.phongTro = {
        phongTroId: data.phongTroId ?? null,
        tenPhong: data.tenPhong ?? null,
        giaThue: data.giaThue ?? null,
      };
    }

    if (!data.khachThue && (data.khachThueId != null || data.tenKhachThue != null || data.hoTen != null)) {
      data.khachThue = {
        khachThueId: data.khachThueId ?? null,
        hoTen: data.tenKhachThue ?? data.hoTen ?? null,
        sdt: data.sdt ?? null,
        cccd: data.cccd ?? null,
        tenDangNhap: data.tenDangNhap ?? null,
      };
    }

    return data;
  }

  window.ApiClient = window.ApiClient || {
    async request(path, options = {}) {
      const { headers = {}, auth = true, ...rest } = options;
      const finalHeaders = auth && window.buildAuthHeaders
        ? window.buildAuthHeaders(headers)
        : headers;

      const response = await fetch(`${window.API_BASE}${path}`, {
        ...rest,
        credentials: "include",
        headers: finalHeaders,
      });

      const contentType = response.headers.get("content-type") || "";
      const payload = contentType.includes("application/json")
        ? await response.json()
        : await response.text();

      if (!response.ok) {
        redirectToLoginIfUnauthorized(response.status, path);
        const error = new Error(payload?.message || payload || `HTTP ${response.status}`);
        error.status = response.status;
        error.payload = payload;
        throw error;
      }

      if (payload && typeof payload === "object" && Object.prototype.hasOwnProperty.call(payload, "result")) {
        return normalizeLegacyShape(payload.result);
      }
      return normalizeLegacyShape(payload);
    },
  };

  window.Formatters = window.Formatters || {
    money(value) {
      return `${Number(value || 0).toLocaleString("vi-VN")} VND`;
    },
    date(value) {
      if (!value) return "";
      const date = new Date(value);
      return Number.isNaN(date.getTime()) ? value : date.toLocaleDateString("vi-VN");
    },
  };  const LABEL_MAPS = {
    contractStatus: {
      CON_HIEU_LUC: "Con hieu luc",
      HET_HIEU_LUC: "Het hieu luc",
      HUY: "Da huy",
    },
    roomStatus: {
      TRONG: "Trong",
      DA_CHO_THUE: "Da cho thue",
    },
    roomType: {
      THUONG: "Thuong",
      VIP: "VIP",
    },
    invoiceStatus: {
      CHUA_THANH_TOAN: "Chua thanh toan",
      DA_THANH_TOAN: "Da thanh toan",
      PAID: "Da thanh toan",
      UNPAID: "Chua thanh toan",
    },
    paymentStatus: {
      THANH_CONG: "Thanh cong",
      THAT_BAI: "That bai",
      DA_THANH_TOAN: "Da thanh toan",
      CHUA_THANH_TOAN: "Chua thanh toan",
      PAID: "Da thanh toan",
      UNPAID: "Chua thanh toan",
      FAILED: "That bai",
    },
    meterType: {
      DIEN: "Dien",
      NUOC: "Nuoc",
    },
    memberRole: {
      DAI_DIEN: "Dai dien",
      O_CUNG: "O cung",
    },
    paymentMethod: {
      ADMIN_XAC_NHAN: "Xac nhan thu cong",
      MOMO: "MoMo",
      CHUYEN_KHOAN: "Chuyen khoan",
      TIEN_MAT: "Tien mat",
    },
  };

  function humanize(rawValue) {
    if (rawValue === null || rawValue === undefined) return "";
    return String(rawValue)
      .replace(/_/g, " ")
      .toLowerCase()
      .replace(/\b\w/g, (ch) => ch.toUpperCase());
  }

  function labelOf(group, rawValue) {
    if (rawValue === null || rawValue === undefined || rawValue === "") return "";
    const key = String(rawValue).trim().toUpperCase();
    const map = LABEL_MAPS[group] || {};
    return map[key] || humanize(rawValue);
  }

  window.AppUtils = window.AppUtils || {
    api(path, options = {}) {
      return window.ApiClient.request(path, options);
    },
    money(value) {
      return window.Formatters.money(value);
    },
    date(value) {
      return window.Formatters.date(value);
    },
    labels: {
      contractStatus(value) { return labelOf("contractStatus", value); },
      roomStatus(value) { return labelOf("roomStatus", value); },
      roomType(value) { return labelOf("roomType", value); },
      invoiceStatus(value) { return labelOf("invoiceStatus", value); },
      paymentStatus(value) { return labelOf("paymentStatus", value); },
      meterType(value) { return labelOf("meterType", value); },
      memberRole(value) { return labelOf("memberRole", value); },
      paymentMethod(value) { return labelOf("paymentMethod", value); },
    },
    todayISO() {
      const today = new Date();
      return `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}-${String(today.getDate()).padStart(2, "0")}`;
    },
    currentPeriod() {
      const today = new Date();
      return `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}`;
    },
  };

  window.fetchPage = async function fetchPage(apiFn, path, options = {}) {
    const {
      page = 0,
      size = 10,
      sortBy,
      direction,
      params = {},
    } = options;

    const [rawPath, rawQuery = ""] = path.split("?");
    const query = new URLSearchParams(rawQuery);

    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") {
        query.set(key, String(value));
      }
    });

    query.set("page", String(Math.max(0, Number(page) || 0)));
    query.set("size", String(Math.max(1, Number(size) || 10)));
    if (sortBy) query.set("sortBy", sortBy);
    if (direction) query.set("direction", direction);

    const data = await apiFn(`${rawPath}?${query.toString()}`);
    if (Array.isArray(data)) {
      return {
        content: data,
        totalElements: data.length,
        totalPages: 1,
        number: 0,
        size: data.length,
      };
    }

    return {
      content: Array.isArray(data?.content) ? data.content : [],
      totalElements: Number(data?.totalElements ?? 0),
      totalPages: Number(data?.totalPages ?? 1),
      number: Number(data?.number ?? page ?? 0),
      size: Number(data?.size ?? size ?? 10),
    };
  };
})();
