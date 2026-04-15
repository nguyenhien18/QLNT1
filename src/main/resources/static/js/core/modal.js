(function () {
  function getElement(id) {
    return typeof id === "string" ? document.getElementById(id) : id;
  }

  function open(id) {
    const el = getElement(id);
    if (!el) return;
    el.classList.add("show");
    el.setAttribute("aria-hidden", "false");
  }

  function close(id) {
    const el = getElement(id);
    if (!el) return;
    el.classList.remove("show");
    el.setAttribute("aria-hidden", "true");
  }

  window.Modal = { open, close };
})();
