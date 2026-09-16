package com.yugabyte.yugastore.ui.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yugabyte.yugastore.ui.rest.DashboardRestConsumer;

@ExtendWith(MockitoExtension.class)
class CronosProductsControllerTest {

	@Mock
	DashboardRestConsumer dashboardRestConsumer;

	CronosProductsController controller;

	@BeforeEach
	void setUp() {
		controller = new CronosProductsController();
		controller.dashboardRestConsumer = dashboardRestConsumer;
	}

	@Test
	void getProductDetailsDelegatesToHomePageProductsWithHardcodedArgs() {
		when(dashboardRestConsumer.getHomePageProducts(10, 0)).thenReturn("products");
		assertEquals("products", controller.getProductDetails());
		verify(dashboardRestConsumer).getHomePageProducts(10, 0);
		verifyNoMoreInteractions(dashboardRestConsumer);
	}

	@Test
	void getProductDetailsByCategoryDelegates() {
		when(dashboardRestConsumer.getProductsByCategory("Books", 5, 10))
				.thenReturn("cat");
		assertEquals("cat", controller.getProductDetails("Books", 5, 10));
		verify(dashboardRestConsumer).getProductsByCategory("Books", 5, 10);
		verifyNoMoreInteractions(dashboardRestConsumer);
	}

	@Test
	void getProductDetailsByAsinDelegates() {
		when(dashboardRestConsumer.getProductDetails("B01")).thenReturn("details");
		assertEquals("details", controller.getProductDetails("B01"));
		verify(dashboardRestConsumer).getProductDetails("B01");
		verifyNoMoreInteractions(dashboardRestConsumer);
	}

	@Test
	void addProductToCartDelegates() {
		when(dashboardRestConsumer.addProductToCart("B01")).thenReturn("added");
		assertEquals("added", controller.addProductToCart("B01"));
		verify(dashboardRestConsumer).addProductToCart("B01");
		verifyNoMoreInteractions(dashboardRestConsumer);
	}

	@Test
	void showCartDelegates() {
		when(dashboardRestConsumer.showCart()).thenReturn("cart");
		assertEquals("cart", controller.showCart());
		verify(dashboardRestConsumer).showCart();
		verifyNoMoreInteractions(dashboardRestConsumer);
	}

	@Test
	void checkoutCartDelegatesToCheckout() {
		when(dashboardRestConsumer.checkout()).thenReturn("done");
		assertEquals("done", controller.checkoutCart());
		verify(dashboardRestConsumer).checkout();
		verifyNoMoreInteractions(dashboardRestConsumer);
	}

	@Test
	void removeProductFromCartDelegates() {
		when(dashboardRestConsumer.removeProductFromCart("B01")).thenReturn("removed");
		assertEquals("removed", controller.removeProductFromCart("B01"));
		verify(dashboardRestConsumer).removeProductFromCart("B01");
		verifyNoMoreInteractions(dashboardRestConsumer);
	}

	@Test
	void getCartDelegates() {
		when(dashboardRestConsumer.getCart()).thenReturn("cart");
		assertEquals("cart", controller.getCart());
		verify(dashboardRestConsumer).getCart();
		verifyNoMoreInteractions(dashboardRestConsumer);
	}
}
