package com.yugabyte.app.yugastore.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.yugabyte.app.yugastore.domain.ProductInventory;
import com.yugabyte.app.yugastore.domain.ProductMetadata;

class NotEnoughProductsInStockExceptionTest {

    @Test
    void defaultConstructorUsesGenericMessage() {
        NotEnoughProductsInStockException ex = new NotEnoughProductsInStockException();
        assertEquals("Not enough products in stock", ex.getMessage());
        assertTrue(ex instanceof Exception);
    }

    @Test
    void detailedConstructorFormatsTitleAndRemainingQuantity() {
        ProductMetadata product = new ProductMetadata();
        product.setTitle("Widget");
        ProductInventory inventory = new ProductInventory();
        inventory.setQuantity(2);

        NotEnoughProductsInStockException ex = new NotEnoughProductsInStockException(inventory, product);

        assertEquals("Not enough Widget products in stock. Only 2 left", ex.getMessage());
    }

    @Test
    void detailedConstructorWithNullTitleAndQuantityRendersNull() {
        NotEnoughProductsInStockException ex =
            new NotEnoughProductsInStockException(new ProductInventory(), new ProductMetadata());

        assertEquals("Not enough null products in stock. Only null left", ex.getMessage());
    }

    @Test
    void detailedConstructorThrowsWhenArgumentsAreNull() {
        assertThrows(NullPointerException.class, () -> new NotEnoughProductsInStockException(null, new ProductMetadata()));
        assertThrows(NullPointerException.class, () -> new NotEnoughProductsInStockException(new ProductInventory(), null));
    }
}
