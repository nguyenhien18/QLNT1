package com.quanlynhatro;

import com.quanlynhatro.util.PageableUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageableUtilsTest {
    @Test
    void shouldClampAndDefaultPagingValues() {
        var pageable = PageableUtils.build(-1, 999, null, null, "id");
        assertEquals(0, pageable.getPageNumber());
        assertEquals(100, pageable.getPageSize());
        assertEquals("id: DESC", pageable.getSort().toString());
    }
}
