package com.yugabyte.app.yugastore.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;

import com.yugabyte.app.yugastore.domain.ProductMetadata;

class PagerTest {

    @SuppressWarnings("unchecked")
    private Page<ProductMetadata> page(int number, int size, boolean hasNext, boolean hasPrevious,
                                       int totalPages, long totalElements) {
        Page<ProductMetadata> p = mock(Page.class);
        when(p.getNumber()).thenReturn(number);
        when(p.getSize()).thenReturn(size);
        when(p.hasNext()).thenReturn(hasNext);
        when(p.hasPrevious()).thenReturn(hasPrevious);
        when(p.getTotalPages()).thenReturn(totalPages);
        when(p.getTotalElements()).thenReturn(totalElements);
        return p;
    }

    @Test
    void pageIndexIsOneBased() {
        Pager pager = new Pager(page(0, 10, true, false, 3, 25));
        assertEquals(1, pager.getPageIndex());
    }

    @Test
    void delegatesSimpleAccessorsToPage() {
        Pager pager = new Pager(page(2, 10, false, true, 3, 25));
        assertEquals(3, pager.getPageIndex());
        assertEquals(10, pager.getPageSize());
        assertFalse(pager.hasNext());
        assertTrue(pager.hasPrevious());
        assertEquals(3, pager.getTotalPages());
        assertEquals(25L, pager.getTotalElements());
    }

    @Test
    void indexOutOfBoundsIsFalseWhenIndexWithinTotalElements() {
        Pager pager = new Pager(page(0, 10, true, false, 3, 25));
        assertFalse(pager.indexOutOfBounds());
    }

    @Test
    void indexOutOfBoundsComparesAgainstTotalElementsNotTotalPages() {
        // page index 5 (number 4) with 3 total pages but 25 elements -> NOT considered out of bounds
        Pager pager = new Pager(page(4, 10, false, true, 3, 25));
        assertFalse(pager.indexOutOfBounds());
    }

    @Test
    void indexOutOfBoundsIsTrueWhenIndexExceedsTotalElements() {
        Pager pager = new Pager(page(30, 10, false, true, 3, 25));
        assertTrue(pager.indexOutOfBounds());
    }

    @Test
    void firstPageOfEmptyResultIsOutOfBounds() {
        // page index 1 > 0 total elements
        Pager pager = new Pager(page(0, 10, false, false, 0, 0));
        assertTrue(pager.indexOutOfBounds());
    }
}
