package com.yugabyte.app.yugastore.cronoscheckoutapi.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class CheckoutStatusTest {

	@Test
	void statusConstantsAreTheStringsSentToClients() {
		assertEquals("SUCCESS", CheckoutStatus.SUCCESS);
		assertEquals("FAILURE", CheckoutStatus.FAILURE);
	}

	@Test
	void startsCompletelyUnset() {
		CheckoutStatus status = new CheckoutStatus();

		assertNull(status.getStatus());
		assertNull(status.getOrderNumber());
		assertNull(status.getOrderDetails());
	}

	@Test
	void settersRoundTrip() {
		CheckoutStatus status = new CheckoutStatus();
		status.setStatus(CheckoutStatus.SUCCESS);
		status.setOrderNumber("order-1");
		status.setOrderDetails("details");

		assertEquals("SUCCESS", status.getStatus());
		assertEquals("order-1", status.getOrderNumber());
		assertEquals("details", status.getOrderDetails());
	}
}
