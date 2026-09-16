package com.yugabyte.app.yugastore.cronoscheckoutapi.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.yugabyte.app.yugastore.cronoscheckoutapi.domain.Order;
import com.yugabyte.app.yugastore.cronoscheckoutapi.exception.NotEnoughProductsInStockException;
import com.yugabyte.app.yugastore.cronoscheckoutapi.service.CheckoutServiceImpl;

/**
 * Web-layer slice of {@link CheckoutController}: request mapping, security
 * configuration and JSON rendering, with the service mocked out.
 */
@WebMvcTest(controllers = CheckoutController.class, properties = {
		"spring.cloud.discovery.enabled=false",
		"eureka.client.enabled=false" })
class CheckoutControllerWebMvcTest {

	private static final String CHECKOUT_URL = "/checkout-microservice/shoppingCart/checkout";

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CheckoutServiceImpl checkoutService;

	@Test
	void postReturnsSuccessJsonWithoutAuthenticationOrCsrfToken() throws Exception {
		Order order = new Order();
		order.setId("order-1");
		order.setOrder_details("Customer bought these Items:  Product: Widget, Quantity: 1; Order Total is : 4.5");
		when(checkoutService.checkout("u1001")).thenReturn(order);

		mockMvc.perform(post(CHECKOUT_URL))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andExpect(jsonPath("$.orderNumber").value("order-1"))
				.andExpect(jsonPath("$.orderDetails")
						.value("Customer bought these Items:  Product: Widget, Quantity: 1; Order Total is : 4.5"));
	}

	@Test
	void postReturnsFailureJsonWhenNoOrderIsCreated() throws Exception {
		when(checkoutService.checkout("u1001")).thenReturn(null);

		mockMvc.perform(post(CHECKOUT_URL))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("FAILURE"))
				.andExpect(jsonPath("$.orderNumber").value(""))
				.andExpect(jsonPath("$.orderDetails").value("Product is Out of Stock!"));
	}

	@Test
	void postReturnsFailureJsonWithNullDetailsWhenStockIsInsufficient() throws Exception {
		when(checkoutService.checkout("u1001")).thenThrow(new NotEnoughProductsInStockException("Widget", 0));

		mockMvc.perform(post(CHECKOUT_URL))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("FAILURE"))
				.andExpect(jsonPath("$.orderNumber").value(""))
				.andExpect(jsonPath("$.orderDetails").isEmpty());
	}

	@Test
	void getIsNotAllowed() throws Exception {
		mockMvc.perform(get(CHECKOUT_URL)).andExpect(status().isMethodNotAllowed());
	}
}
