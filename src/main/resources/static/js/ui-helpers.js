(function () {
  function escapeHtml(value) {
    return String(value ?? "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#39;");
  }

  function textOrDash(value) {
    if (value === null || value === undefined || value === "") {
      return '<span class="text-muted">-</span>';
    }
    return escapeHtml(value);
  }

  function buildOptions(items, getValue, getLabel, placeholder = "") {
    const options = [];
    if (placeholder) {
      options.push(`<option value="">${escapeHtml(placeholder)}</option>`);
    }
    items.forEach((item) => {
      const value = escapeHtml(getValue(item));
      const label = escapeHtml(getLabel(item));
      options.push(`<option value="${value}">${label}</option>`);
    });
    return options.join("");
  }

  function badge(label, className = "") {
    return `<span class="${escapeHtml(className)}">${escapeHtml(label || "")}</span>`;
  }

  window.UiHelpers = {
    escapeHtml,
    textOrDash,
    buildOptions,
    badge,
  };
})();
