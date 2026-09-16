package com.yugabyte.app.yugastore.cart.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;

import com.yugabyte.app.yugastore.cart.service.ShoppingCartImpl;

@ExtendWith(MockitoExtension.class)
class ShoppingCartControllerTest {

	@Mock
	private ShoppingCartImpl shoppingCart;

	@InjectMocks
	private ShoppingCartController controller;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Nested
	class DirectInvocation {

		@Test
		void addProductDelegatesAndReturnsFixedString() {
			String result = controller.addProductToCart("u1", "A1");

			assertThat(result).isEqualTo("Added to Cart");
			verify(shoppingCart).addProductToShoppingCart("u1", "A1");
			verifyNoMoreInteractions(shoppingCart);
		}

		@Test
		void getProductsInCartReturnsServiceMapAsIs() {
			Map<String, Integer> map = new LinkedHashMap<>();
			map.put("A1", 2);
			when(shoppingCart.getProductsInCart("u1")).thenReturn(map);

			assertThat(controller.getProductsInCart("u1")).isSameAs(map);
		}

		@Test
		@DisplayName("removeProduct returns 'Removing from Cart' regardless of whether anything was removed")
		void removeProductDelegatesAndReturnsFixedString() {
			String result = controller.removeProductFromCart("u1", "A1");

			assertThat(result).isEqualTo("Removing from Cart");
			verify(shoppingCart).removeProductFromCart("u1", "A1");
			verifyNoMoreInteractions(shoppingCart);
		}

		@Test
		@DisplayName("clearCart returns a message claiming checkout success even though it only clears")
		void clearCartDelegatesAndReturnsFixedString() {
			String result = controller.clearCart("u1");

			assertThat(result).isEqualTo("Clearing Cart, Checkout successful");
			verify(shoppingCart).clearCart("u1");
			verifyNoMoreInteractions(shoppingCart);
		}

		@Test
		void serviceExceptionsPropagate() {
			doThrow(new IllegalStateException("boom")).when(shoppingCart).clearCart(anyString());

			org.assertj.core.api.Assertions.assertThatThrownBy(() -> controller.clearCart("u1"))
					.isInstanceOf(IllegalStateException.class)
					.hasMessage("boom");
		}
	}

	@Nested
	class HttpMapping {

		@Test
		@DisplayName("GET /cart-microservice/shoppingCart/addProduct returns plain text body 'Added to Cart' with JSON content type")
		void addProductEndpoint() throws Exception {
			mockMvc.perform(get("/cart-microservice/shoppingCart/addProduct")
							.param("userid", "u1").param("asin", "A1"))
					.andExpect(status().isOk())
					.andExpect(content().contentTypeCompatibleWith("application/json"))
					.andExpect(content().string("Added to Cart"));

			verify(shoppingCart).addProductToShoppingCart("u1", "A1");
		}

		@Test
		void productsInCartEndpointSerializesMapAsJson() throws Exception {
			when(shoppingCart.getProductsInCart("u1")).thenReturn(Map.of("A1", 2));

			mockMvc.perform(get("/cart-microservice/shoppingCart/productsInCart").param("userid", "u1"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.A1").value(2));
		}

		@Test
		void productsInCartEndpointEmptyMapIsEmptyJsonObject() throws Exception {
			when(shoppingCart.getProductsInCart("u1")).thenReturn(Collections.emptyMap());

			mockMvc.perform(get("/cart-microservice/shoppingCart/productsInCart").param("userid", "u1"))
					.andExpect(status().isOk())
					.andExpect(content().json("{}"));
		}

		@Test
		void removeProductEndpoint() throws Exception {
			mockMvc.perform(get("/cart-microservice/shoppingCart/removeProduct")
							.param("userid", "u1").param("asin", "A1"))
					.andExpect(status().isOk())
					.andExpect(content().string("Removing from Cart"));

			verify(shoppingCart).removeProductFromCart("u1", "A1");
		}

		@Test
		void clearCartEndpoint() throws Exception {
			mockMvc.perform(get("/cart-microservice/shoppingCart/clearCart").param("userid", "u1"))
					.andExpect(status().isOk())
					.andExpect(content().string("Clearing Cart, Checkout successful"));

			verify(shoppingCart).clearCart("u1");
		}

		@Test
		@DisplayName("required request params missing -> 400 Bad Request")
		void missingRequiredParamIs400() throws Exception {
			mockMvc.perform(get("/cart-microservice/shoppingCart/addProduct").param("userid", "u1"))
					.andExpect(status().isBadRequest());

			mockMvc.perform(get("/cart-microservice/shoppingCart/productsInCart"))
					.andExpect(status().isBadRequest());

			verifyNoMoreInteractions(shoppingCart);
		}

		@Test
		@DisplayName("mutating endpoints are GET-only; POST is 405")
		void postIsMethodNotAllowed() throws Exception {
			mockMvc.perform(post("/cart-microservice/shoppingCart/addProduct")
							.param("userid", "u1").param("asin", "A1"))
					.andExpect(status().isMethodNotAllowed());

			mockMvc.perform(post("/cart-microservice/shoppingCart/clearCart").param("userid", "u1"))
					.andExpect(status().isMethodNotAllowed());
		}

		@Test
		@DisplayName("param names are lowercase 'userid'; 'userId' is not recognised")
		void camelCaseUserIdIsRejected() throws Exception {
			mockMvc.perform(get("/cart-microservice/shoppingCart/clearCart").param("userId", "u1"))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("no exception handler: service errors surface as NestedServletException from MockMvc")
		void serviceErrorSurfacesAsServletException() {
			doThrow(new IllegalStateException("boom")).when(shoppingCart).clearCart("u1");

			org.assertj.core.api.Assertions.assertThatThrownBy(() -> mockMvc
							.perform(get("/cart-microservice/shoppingCart/clearCart").param("userid", "u1")))
					.isInstanceOf(NestedServletException.class)
					.hasCauseInstanceOf(IllegalStateException.class);
		}
	}
}
