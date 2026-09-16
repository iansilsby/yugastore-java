package com.yugabyte.yugastore.ui.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonSyntaxException;

@ExtendWith(MockitoExtension.class)
class DashboardRestConsumerTest {

	private static final String BASE = "http://host/api/v1/";

	@Mock
	RestTemplate restTemplate;

	DashboardRestConsumer consumer;

	@BeforeEach
	void setUp() {
		consumer = new DashboardRestConsumer();
		consumer.restTemplate = restTemplate;
		consumer.restUrlBase = BASE;
	}

	@SuppressWarnings("unchecked")
	private void stubExchange(String url, HttpMethod method, Object body) {
		when(restTemplate.exchange(eq(url), eq(method), any(),
				any(ParameterizedTypeReference.class)))
				.thenReturn(ResponseEntity.ok((String) body));
	}

	private void stubExchangeNullEntity(String url, HttpMethod method, String body) {
		when(restTemplate.exchange(eq(url), eq(method), isNull(),
				any(ParameterizedTypeReference.class)))
				.thenReturn(ResponseEntity.ok(body));
	}

	@Test
	void getHomePageProductsCompactsJsonArray() {
		stubExchangeNullEntity(BASE + "products?limit=10&offset=0", HttpMethod.GET,
				"[ {\"a\" : 1} ]");
		assertEquals("[{\"a\":1}]", consumer.getHomePageProducts(10, 0));
	}

	@Test
	void getHomePageProductsNullBodyThrowsNpe() {
		stubExchangeNullEntity(BASE + "products?limit=10&offset=0", HttpMethod.GET, null);
		assertThrows(NullPointerException.class, () -> consumer.getHomePageProducts(10, 0));
	}

	@Test
	void getHomePageProductsObjectBodyThrowsJsonSyntaxException() {
		stubExchangeNullEntity(BASE + "products?limit=10&offset=0", HttpMethod.GET, "{\"a\":1}");
		assertThrows(JsonSyntaxException.class, () -> consumer.getHomePageProducts(10, 0));
	}

	@Test
	void getHomePageProductsEmptyArray() {
		stubExchangeNullEntity(BASE + "products?limit=10&offset=0", HttpMethod.GET, "[]");
		assertEquals("[]", consumer.getHomePageProducts(10, 0));
	}

	@Test
	void getProductsByCategoryEncodesSpaceAmpersandComma() {
		stubExchangeNullEntity(
				BASE + "products/category/Books%20%26%20Media%2C%20Kids?limit=5&offset=10",
				HttpMethod.GET, "[ {\"x\" : 2} ]");
		assertEquals("[{\"x\":2}]",
				consumer.getProductsByCategory("Books & Media, Kids", 5, 10));
	}

	@Test
	void getProductsByCategoryDoesNotEncodeOtherSpecialChars() {
		stubExchangeNullEntity(
				BASE + "products/category/a/b?c?limit=1&offset=2",
				HttpMethod.GET, "[]");
		assertEquals("[]", consumer.getProductsByCategory("a/b?c", 1, 2));
	}

	@Test
	void getProductDetailsReturnsBodyVerbatim() {
		String body = "{ \"a\" : 1 }\n  ";
		stubExchangeNullEntity(BASE + "product/B01", HttpMethod.GET, body);
		assertEquals(body, consumer.getProductDetails("B01"));
	}

	@Test
	void getProductDetailsNullBodyReturnsNull() {
		stubExchangeNullEntity(BASE + "product/B01", HttpMethod.GET, null);
		assertNull(consumer.getProductDetails("B01"));
	}

	@Test
	void addProductToCartPostsAsinForm() {
		stubExchange(BASE + "shoppingCart/addProduct", HttpMethod.POST, "added");
		assertEquals("added", consumer.addProductToCart("B01"));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> captor =
				ArgumentCaptor.forClass(HttpEntity.class);
		verify(restTemplate).exchange(eq(BASE + "shoppingCart/addProduct"),
				eq(HttpMethod.POST), captor.capture(),
				any(ParameterizedTypeReference.class));
		HttpEntity<MultiValueMap<String, String>> entity = captor.getValue();
		assertEquals("B01", entity.getBody().getFirst("asin"));
		assertTrue(entity.getHeaders().isEmpty());
	}

	@Test
	void removeProductFromCartPostsAsinForm() {
		stubExchange(BASE + "shoppingCart/removeProduct", HttpMethod.POST, "removed");
		assertEquals("removed", consumer.removeProductFromCart("B01"));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> captor =
				ArgumentCaptor.forClass(HttpEntity.class);
		verify(restTemplate).exchange(eq(BASE + "shoppingCart/removeProduct"),
				eq(HttpMethod.POST), captor.capture(),
				any(ParameterizedTypeReference.class));
		HttpEntity<MultiValueMap<String, String>> entity = captor.getValue();
		assertEquals("B01", entity.getBody().getFirst("asin"));
		assertTrue(entity.getHeaders().isEmpty());
	}

	@Test
	void getCartPostsNullEntity() {
		stubExchangeNullEntity(BASE + "shoppingCart", HttpMethod.POST, "cart");
		assertEquals("cart", consumer.getCart());
		verify(restTemplate).exchange(eq(BASE + "shoppingCart"), eq(HttpMethod.POST),
				isNull(), any(ParameterizedTypeReference.class));
	}

	@Test
	void showCartPostsHardcodedUserId() {
		stubExchange(BASE + "shoppingCart", HttpMethod.POST, "cart");
		assertEquals("cart", consumer.showCart());

		@SuppressWarnings("unchecked")
		ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> captor =
				ArgumentCaptor.forClass(HttpEntity.class);
		verify(restTemplate).exchange(eq(BASE + "shoppingCart"), eq(HttpMethod.POST),
				captor.capture(), any(ParameterizedTypeReference.class));
		assertEquals("1", captor.getValue().getBody().getFirst("userId"));
	}

	@Test
	void checkoutPostsHardcodedUserId() {
		stubExchange(BASE + "shoppingCart/checkout", HttpMethod.POST, "done");
		assertEquals("done", consumer.checkout());

		@SuppressWarnings("unchecked")
		ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> captor =
				ArgumentCaptor.forClass(HttpEntity.class);
		verify(restTemplate).exchange(eq(BASE + "shoppingCart/checkout"),
				eq(HttpMethod.POST), captor.capture(),
				any(ParameterizedTypeReference.class));
		assertEquals("1", captor.getValue().getBody().getFirst("userId"));
	}

	@Test
	void missingTrailingSlashConcatenatesUrlsWithoutSeparator() {
		consumer.restUrlBase = "http://localhost:8081/api/v1";
		stubExchangeNullEntity(
				"http://localhost:8081/api/v1products?limit=10&offset=0",
				HttpMethod.GET, "[]");
		assertEquals("[]", consumer.getHomePageProducts(10, 0));
	}

	@Test
	void restClientExceptionPropagates() {
		when(restTemplate.exchange(eq(BASE + "product/B01"), eq(HttpMethod.GET),
				isNull(), any(ParameterizedTypeReference.class)))
				.thenThrow(new RestClientException("boom"));
		RestClientException ex = assertThrows(RestClientException.class,
				() -> consumer.getProductDetails("B01"));
		assertEquals("boom", ex.getMessage());
	}
}
