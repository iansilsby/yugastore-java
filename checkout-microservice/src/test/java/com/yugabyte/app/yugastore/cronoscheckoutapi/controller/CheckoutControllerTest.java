package com.yugabyte.app.yugastore.cronoscheckoutapi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yugabyte.app.yugastore.cronoscheckoutapi.domain.CheckoutStatus;
import com.yugabyte.app.yugastore.cronoscheckoutapi.domain.Order;
import com.yugabyte.app.yugastore.cronoscheckoutapi.exception.NotEnoughProductsInStockException;
import com.yugabyte.app.yugastore.cronoscheckoutapi.service.CheckoutServiceImpl;

@ExtendWith(MockitoExtension.class)
class CheckoutControllerTest {

	@Mock
	private CheckoutServiceImpl checkoutService;

	@InjectMocks
	private CheckoutController controller;

	private static Order order(String id, String details) {
		Order order = new Order();
		order.setId(id);
		order.setOrder_details(details);
		order.setUser_id(1);
		order.setOrder_total(9.0);
		return order;
	}

	@Test
	void alwaysChecksOutTheHardCodedUserU1001() throws Exception {
		when(checkoutService.checkout(anyString())).thenReturn(order("id-1", "details"));

		controller.checkout();

		verify(checkoutService).checkout("u1001");
	}

	@Test
	void reportsSuccessWithOrderNumberAndDetailsWhenAnOrderIsCreated() throws Exception {
		when(checkoutService.checkout("u1001"))
				.thenReturn(order("7c9e6679-7425-40de-944b-e07fc1f90ae7", "Customer bought these Items: ..."));

		CheckoutStatus status = controller.checkout();

		assertEquals(CheckoutStatus.SUCCESS, status.getStatus());
		assertEquals("7c9e6679-7425-40de-944b-e07fc1f90ae7", status.getOrderNumber());
		assertEquals("Customer bought these Items: ...", status.getOrderDetails());
	}

	@Test
	void reportsOutOfStockFailureWhenTheServiceReturnsNoOrder() throws Exception {
		// The service returns null for an empty cart, yet the controller labels it "Out of Stock".
		when(checkoutService.checkout("u1001")).thenReturn(null);

		CheckoutStatus status = controller.checkout();

		assertEquals(CheckoutStatus.FAILURE, status.getStatus());
		assertEquals("", status.getOrderNumber());
		assertEquals("Product is Out of Stock!", status.getOrderDetails());
	}

	@Test
	void reportsFailureWithoutDetailsWhenStockIsInsufficient() throws Exception {
		when(checkoutService.checkout("u1001"))
				.thenThrow(new NotEnoughProductsInStockException("Widget", 1));

		CheckoutStatus status = controller.checkout();

		assertEquals(CheckoutStatus.FAILURE, status.getStatus());
		assertEquals("", status.getOrderNumber());
		assertNull(status.getOrderDetails(), "the exception message is not surfaced to the client");
	}

	@Test
	void letsUnexpectedRuntimeExceptionsPropagate() throws Exception {
		RuntimeException boom = new NullPointerException("inventory row missing");
		when(checkoutService.checkout("u1001")).thenThrow(boom);

		RuntimeException thrown = assertThrows(RuntimeException.class, controller::checkout);

		assertSame(boom, thrown);
	}
}
