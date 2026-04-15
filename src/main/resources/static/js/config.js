(function () {
  const isFileProtocol = window.location.protocol === "file:";
  const isLocalHost = ["localhost", "127.0.0.1"].includes(window.location.hostname);
  const defaultBase = isFileProtocol
    ? "http://localhost:8086"
    : (isLocalHost && window.location.port !== "8086"
        ? `${window.location.protocol}//${window.location.hostname}:8086`
        : window.location.origin);

  window.API_BASE = window.API_BASE || defaultBase;

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
        headers: finalHeaders,
      });

      const contentType = response.headers.get("content-type") || "";
      const payload = contentType.includes("application/json")
        ? await response.json()
        : await response.text();

      if (!response.ok) {
        throw new Error(payload?.message || payload || `HTTP ${response.status}`);
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
  };

  const LABEL_MAPS = {
    contractStatus: {
      CON_HIEU_LUC: "Còn hiệu lực",
      HET_HIEU_LUC: "Hết hiệu lực",
      HUY: "Đã hủy",
    },
    roomStatus: {
      TRONG: "Trống",
      DA_CHO_THUE: "Đã cho thuê",
    },
    roomType: {
      CO_GAC: "Có gác",
      KHONG_GAC: "Không gác",
    },
    invoiceStatus: {
      CHUA_THANH_TOAN: "Chưa thanh toán",
      DA_THANH_TOAN: "Đã thanh toán",
      PAID: "Đã thanh toán",
      UNPAID: "Chưa thanh toán",
    },
    paymentStatus: {
      THANH_CONG: "Thành công",
      THAT_BAI: "Thất bại",
      DA_THANH_TOAN: "Đã thanh toán",
      CHUA_THANH_TOAN: "Chưa thanh toán",
      PAID: "Đã thanh toán",
      UNPAID: "Chưa thanh toán",
      FAILED: "Thất bại",
    },
    meterType: {
      DIEN: "Điện",
      NUOC: "Nước",
    },
    memberRole: {
      DAI_DIEN: "Đại diện",
      O_CUNG: "Ở cùng",
    },
    paymentMethod: {
      ADMIN_XAC_NHAN: "Xác nhận thủ công",
      MOMO: "MoMo",
      CHUYEN_KHOAN: "Chuyển khoản",
      TIEN_MAT: "Tiền mặt",
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

  window.fetchAllPages = async function fetchAllPages(apiFn, path, options = {}) {
    const { pageSize = 100, sortBy, direction, params = {} } = options;
    const [rawPath, rawQuery = ""] = path.split("?");
    const baseParams = new URLSearchParams(rawQuery);
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") {
        baseParams.set(key, String(value));
      }
    });
    if (sortBy) baseParams.set("sortBy", sortBy);
    if (direction) baseParams.set("direction", direction);

    let page = 0;
    let totalPages = 1;
    const items = [];

    do {
      const query = new URLSearchParams(baseParams);
      query.set("page", String(page));
      query.set("size", String(pageSize));
      const data = await apiFn(`${rawPath}?${query.toString()}`);
      const content = Array.isArray(data) ? data : (data?.content || []);
      items.push(...content);
      if (Array.isArray(data)) break;
      totalPages = Number(data?.totalPages ?? 1);
      page += 1;
    } while (page < totalPages);

    return items;
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
