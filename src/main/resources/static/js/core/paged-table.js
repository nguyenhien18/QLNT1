(function () {
  window.createPagedState = function createPagedState(pageSize = 10) {
    return {
      page: 1,
      totalPages: 1,
      totalItems: 0,
      pageSize,
      apply(pageData) {
        this.totalItems = Number(pageData?.totalElements || 0);
        this.totalPages = Math.max(1, Number(pageData?.totalPages || 1));
        return pageData?.content || [];
      },
      ensureValidPage() {
        if (this.page > this.totalPages) this.page = this.totalPages;
        if (this.page < 1) this.page = 1;
      },
      pageInfo() {
        const start = this.totalItems ? (this.page - 1) * this.pageSize + 1 : 0;
        const end = Math.min(this.page * this.pageSize, this.totalItems);
        return { start, end };
      },
    };
  };
})();
