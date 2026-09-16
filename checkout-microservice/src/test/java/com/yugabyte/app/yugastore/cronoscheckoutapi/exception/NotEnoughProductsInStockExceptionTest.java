package com.yugabyte.app.yugastore.cronoscheckoutapi.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class NotEnoughProductsInStockExceptionTest {

	@Test
	void defaultConstructorUsesGenericMessage() {
		NotEnoughProductsInStockException e = new NotEnoughProductsInStockException();

		assertEquals("Not enough products in stock", e.getMessage());
		assertNull(e.getCause());
	}

	@Test
	void detailedConstructorEmbedsTitleAndRemainingQuantity() {
		NotEnoughProductsInStockException e = new NotEnoughProductsInStockException("Widget", 3);

		assertEquals("Not enough Widget products in stock. Only 3 left", e.getMessage());
	}

	@Test
	void detailedConstructorToleratesNullArguments() {
		NotEnoughProductsInStockException e = new NotEnoughProductsInStockException(null, null);

		assertEquals("Not enough null products in stock. Only null left", e.getMessage());
	}

	@Test
	void isACheckedException() {
		assertFalse(RuntimeException.class.isAssignableFrom(NotEnoughProductsInStockException.class));
	}
}
