(function () {
  function bindChanges(ids, handler, eventName = "change") {
    ids.forEach((id) => {
      const el = document.getElementById(id);
      if (!el) return;
      el.addEventListener(eventName, handler);
    });
  }

  function bindInputs(ids, handler) {
    bindChanges(ids, handler, "input");
  }

  function bindPagination(prevId, nextId, state, reload) {
    const prev = document.getElementById(prevId);
    const next = document.getElementById(nextId);

    prev?.addEventListener("click", () => {
      if (state.page > 1) {
        state.page -= 1;
        reload();
      }
    });

    next?.addEventListener("click", () => {
      if (state.page < state.totalPages) {
        state.page += 1;
        reload();
      }
    });
  }

  window.PageFilters = {
    bindChanges,
    bindInputs,
    bindPagination,
  };
})();
