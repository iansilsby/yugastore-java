package com.yugabyte.yugastore.ui.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.yugabyte.yugastore.ui.rest.DashboardRestConsumer;

@WebMvcTest(CronosProductsController.class)
class CronosProductsControllerWebMvcTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	DashboardRestConsumer dashboardRestConsumer;

	@Test
	void getProductsReturnsBody() throws Exception {
		when(dashboardRestConsumer.getHomePageProducts(10, 0)).thenReturn("[{}]");
		mockMvc.perform(get("/products"))
				.andExpect(status().isOk())
				.andExpect(content().string("[{}]"));
		verify(dashboardRestConsumer).getHomePageProducts(10, 0);
	}

	@Test
	void getProductsByCategoryDelegatesParams() throws Exception {
		when(dashboardRestConsumer.getProductsByCategory("Books", 2, 1))
				.thenReturn("cat");
		mockMvc.perform(get("/products/category/Books?limit=2&offset=1"))
				.andExpect(status().isOk())
				.andExpect(content().string("cat"));
		verify(dashboardRestConsumer).getProductsByCategory("Books", 2, 1);
	}

	@Test
	void getProductsByCategoryMissingParamsIs400() throws Exception {
		mockMvc.perform(get("/products/category/Books"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void getProductDetailsReturnsBody() throws Exception {
		when(dashboardRestConsumer.getProductDetails("X")).thenReturn("details");
		mockMvc.perform(get("/products/details?asin=X"))
				.andExpect(status().isOk())
				.andExpect(content().string("details"));
		verify(dashboardRestConsumer).getProductDetails("X");
	}

	@Test
	void getProductDetailsMissingAsinIs400() throws Exception {
		mockMvc.perform(get("/products/details"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void postCartAddReturnsBody() throws Exception {
		when(dashboardRestConsumer.addProductToCart("X")).thenReturn("added");
		mockMvc.perform(post("/cart/add?asin=X"))
				.andExpect(status().isOk())
				.andExpect(content().string("added"));
		verify(dashboardRestConsumer).addProductToCart("X");
	}

	@Test
	void postCartAddMissingAsinIs400() throws Exception {
		mockMvc.perform(post("/cart/add"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void getCartAddIs405() throws Exception {
		mockMvc.perform(get("/cart/add"))
				.andExpect(status().isMethodNotAllowed());
	}

	@Test
	void postCartGetReturnsBody() throws Exception {
		when(dashboardRestConsumer.showCart()).thenReturn("cart");
		mockMvc.perform(post("/cart/get"))
				.andExpect(status().isOk())
				.andExpect(content().string("cart"));
		verify(dashboardRestConsumer).showCart();
	}

	@Test
	void getCartGetIs405() throws Exception {
		mockMvc.perform(get("/cart/get"))
				.andExpect(status().isMethodNotAllowed());
	}

	@Test
	void postCartCheckoutReturnsBody() throws Exception {
		when(dashboardRestConsumer.checkout()).thenReturn("done");
		mockMvc.perform(post("/cart/checkout"))
				.andExpect(status().isOk())
				.andExpect(content().string("done"));
		verify(dashboardRestConsumer).checkout();
	}

	@Test
	void postCartRemoveReturnsBody() throws Exception {
		when(dashboardRestConsumer.removeProductFromCart("X")).thenReturn("removed");
		mockMvc.perform(post("/cart/remove?asin=X"))
				.andExpect(status().isOk())
				.andExpect(content().string("removed"));
		verify(dashboardRestConsumer).removeProductFromCart("X");
	}

	@Test
	void postCartGetCartReturnsBody() throws Exception {
		when(dashboardRestConsumer.getCart()).thenReturn("cart");
		mockMvc.perform(post("/cart/getCart"))
				.andExpect(status().isOk())
				.andExpect(content().string("cart"));
		verify(dashboardRestConsumer).getCart();
	}
}
