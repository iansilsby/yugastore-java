package com.yugabyte.app.yugastore.cronoscheckoutapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.cql.CqlOperations;
import org.springframework.test.util.ReflectionTestUtils;

import com.yugabyte.app.yugastore.cronoscheckoutapi.domain.Order;
import com.yugabyte.app.yugastore.cronoscheckoutapi.domain.ProductInventory;
import com.yugabyte.app.yugastore.cronoscheckoutapi.domain.ProductMetadata;
import com.yugabyte.app.yugastore.cronoscheckoutapi.exception.NotEnoughProductsInStockException;
import com.yugabyte.app.yugastore.cronoscheckoutapi.repositories.ProductInventoryRepository;
import com.yugabyte.app.yugastore.cronoscheckoutapi.rest.clients.ProductCatalogRestClient;
import com.yugabyte.app.yugastore.cronoscheckoutapi.rest.clients.ShoppingCartRestClient;

/**
 * Characterization tests for {@link CheckoutServiceImpl}. They pin down the
 * current behaviour of the checkout flow (including its quirks) without
 * needing a live YugabyteDB, Eureka or downstream microservices.
 */
@ExtendWith(MockitoExtension.class)
class CheckoutServiceImplTest {

	private static final String USER_ID = "u1001";
	private static final String ASIN = "B00001";
	private static final String OTHER_ASIN = "B00002";

	@Mock
	private ShoppingCartRestClient shoppingCartRestClient;

	@Mock
	private ProductCatalogRestClient productCatalogRestClient;

	@Mock
	private ProductInventoryRepository productInventoryRepository;

	@Mock
	private CassandraOperations cassandraTemplate;

	@Mock
	private CqlOperations cqlOperations;

	private CheckoutServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new CheckoutServiceImpl(productInventoryRepository, shoppingCartRestClient,
				productCatalogRestClient);
		// CassandraOperations is field-injected in production, so it has to be set by hand here.
		ReflectionTestUtils.setField(service, "cassandraTemplate", cassandraTemplate);
	}

	private void cartContains(Map<String, Integer> products) {
		when(shoppingCartRestClient.getProductsInCart(USER_ID)).thenReturn(products);
	}

	private void inventoryHas(String asin, int quantity) {
		ProductInventory inventory = new ProductInventory();
		inventory.setId(asin);
		inventory.setQuantity(quantity);
		when(productInventoryRepository.findById(asin)).thenReturn(Optional.of(inventory));
	}

	private void catalogHas(String asin, String title, double price) {
		ProductMetadata metadata = new ProductMetadata();
		metadata.setId(asin);
		metadata.setTitle(title);
		metadata.setPrice(price);
		when(productCatalogRestClient.getProductDetails(asin)).thenReturn(metadata);
	}

	private void databaseAcceptsStatements() {
		when(cassandraTemplate.getCqlOperations()).thenReturn(cqlOperations);
	}

	private String executedCql() {
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(cqlOperations).execute(captor.capture());
		return captor.getValue();
	}

	@Nested
	@DisplayName("when the cart is empty")
	class EmptyCart {

		@Test
		void returnsNullWithoutTouchingTheDatabase() throws Exception {
			cartContains(new HashMap<>());

			Order order = service.checkout(USER_ID);

			assertNull(order);
			verifyNoInteractions(cassandraTemplate, cqlOperations, productInventoryRepository,
					productCatalogRestClient);
		}

		@Test
		void stillClearsTheCart() throws Exception {
			cartContains(new HashMap<>());

			service.checkout(USER_ID);

			verify(shoppingCartRestClient).getProductsInCart(USER_ID);
			verify(shoppingCartRestClient).clearCart(USER_ID);
		}
	}

	@Nested
	@DisplayName("when every product is in stock")
	class SuccessfulCheckout {

		@BeforeEach
		void stockAndCatalog() {
			inventoryHas(ASIN, 10);
			catalogHas(ASIN, "Widget", 4.5);
			databaseAcceptsStatements();
		}

		@Test
		void returnsAPopulatedOrder() throws Exception {
			cartContains(new HashMap<>(Map.of(ASIN, 2)));

			Order order = service.checkout(USER_ID);

			assertNotNull(order);
			assertNotNull(UUID.fromString(order.getId()), "order id is a random UUID");
			assertEquals("Customer bought these Items:  Product: Widget, Quantity: 2; Order Total is : 9.0",
					order.getOrder_details());
			assertEquals(9.0, order.getOrder_total());
			assertNotNull(LocalDateTime.parse(order.getOrder_time()),
					"order time is LocalDateTime#toString() output");
		}

		@Test
		void hardCodesTheOrderUserIdToOneRegardlessOfCaller() throws Exception {
			cartContains(new HashMap<>(Map.of(ASIN, 1)));

			Order order = service.checkout(USER_ID);

			assertEquals(1, order.getUser_id());
			assertTrue(executedCql().contains("VALUES ('" + order.getId() + "', '1', "),
					"orders row is inserted for user '1', not for " + USER_ID);
		}

		@Test
		void executesASingleTransactionalCqlBatch() throws Exception {
			cartContains(new HashMap<>(Map.of(ASIN, 2)));

			Order order = service.checkout(USER_ID);

			String expected = "BEGIN TRANSACTION"
					+ " UPDATE product_inventory SET quantity = quantity - 2 where asin = '" + ASIN + "' ;"
					+ " INSERT INTO orders (order_id, user_id, order_details, order_time, order_total) VALUES ("
					+ "'" + order.getId() + "', '1', '" + order.getOrder_details() + "', '"
					+ order.getOrder_time() + "',9.0);"
					+ " END TRANSACTION;";
			assertEquals(expected, executedCql());
		}

		@Test
		void clearsTheCartOnlyAfterTheDatabaseWrite() throws Exception {
			cartContains(new HashMap<>(Map.of(ASIN, 1)));

			service.checkout(USER_ID);

			InOrder inOrder = inOrder(cqlOperations, shoppingCartRestClient);
			inOrder.verify(cqlOperations).execute(anyString());
			inOrder.verify(shoppingCartRestClient).clearCart(USER_ID);
		}

		@Test
		void allowsBuyingTheEntireRemainingStock() throws Exception {
			cartContains(new HashMap<>(Map.of(ASIN, 10)));

			Order order = service.checkout(USER_ID);

			assertNotNull(order);
			assertEquals(45.0, order.getOrder_total());
			assertTrue(executedCql().contains("quantity = quantity - 10 where asin = '" + ASIN + "'"));
		}

		@Test
		void looksUpInventoryAndCatalogTwicePerProduct() throws Exception {
			cartContains(new HashMap<>(Map.of(ASIN, 1)));

			service.checkout(USER_ID);

			// once during the stock check, once again while computing the total
			verify(productInventoryRepository, times(2)).findById(ASIN);
			verify(productCatalogRestClient, times(2)).getProductDetails(ASIN);
		}

		@Test
		void emptiesTheMapReturnedByTheCartClient() throws Exception {
			Map<String, Integer> products = new HashMap<>(Map.of(ASIN, 1));
			cartContains(products);

			service.checkout(USER_ID);

			assertTrue(products.isEmpty(), "checkout mutates the caller-visible map");
		}

		@Test
		void exposesTheLastProcessedProductThroughPackagePrivateFields() throws Exception {
			cartContains(new HashMap<>(Map.of(ASIN, 1)));

			service.checkout(USER_ID);

			assertEquals(ASIN, service.productInventory.getId());
			assertEquals("Widget", service.productDetails.getTitle());
		}

		@Test
		void rendersTheTotalWithJavaDoubleFormatting() throws Exception {
			catalogHas(ASIN, "Widget", 0.1);
			cartContains(new HashMap<>(Map.of(ASIN, 3)));

			Order order = service.checkout(USER_ID);

			assertEquals(0.1 * 3, order.getOrder_total());
			assertTrue(order.getOrder_details().endsWith("Order Total is : 0.30000000000000004"),
					"floating point total is not rounded: " + order.getOrder_details());
		}
	}

	@Nested
	@DisplayName("when the statement is built")
	class StatementBuilding {

		@Test
		void interpolatesAsinAndQuantityDirectlyIntoTheStatement() throws Exception {
			String oddAsin = "it's-odd";
			inventoryHas(oddAsin, 5);
			catalogHas(oddAsin, "Odd", 1.0);
			databaseAcceptsStatements();
			cartContains(new HashMap<>(Map.of(oddAsin, 3)));

			service.checkout(USER_ID);

			assertTrue(executedCql().contains("quantity = quantity - 3 where asin = 'it's-odd' ;"),
					"values are concatenated verbatim, not bound as parameters");
		}
	}

	@Nested
	@DisplayName("with several products in the cart")
	class MultipleProducts {

		@BeforeEach
		void stockAndCatalog() {
			inventoryHas(ASIN, 10);
			catalogHas(ASIN, "Widget", 10.0);
			inventoryHas(OTHER_ASIN, 10);
			catalogHas(OTHER_ASIN, "Gadget", 2.5);
			databaseAcceptsStatements();
		}

		@Test
		void sumsPriceTimesQuantityAcrossProducts() throws Exception {
			Map<String, Integer> products = new LinkedHashMap<>();
			products.put(ASIN, 2);
			products.put(OTHER_ASIN, 3);
			cartContains(products);

			Order order = service.checkout(USER_ID);

			assertEquals(27.5, order.getOrder_total());
			assertEquals("Customer bought these Items: "
					+ " Product: Widget, Quantity: 2;"
					+ " Product: Gadget, Quantity: 3;"
					+ " Order Total is : 27.5", order.getOrder_details());
		}

		@Test
		void emitsOneUpdatePerProductInCartIterationOrder() throws Exception {
			Map<String, Integer> products = new LinkedHashMap<>();
			products.put(ASIN, 2);
			products.put(OTHER_ASIN, 3);
			cartContains(products);

			service.checkout(USER_ID);

			String cql = executedCql();
			int firstUpdate = cql.indexOf("quantity = quantity - 2 where asin = '" + ASIN + "'");
			int secondUpdate = cql.indexOf("quantity = quantity - 3 where asin = '" + OTHER_ASIN + "'");
			int insert = cql.indexOf("INSERT INTO orders");
			assertTrue(firstUpdate > 0 && secondUpdate > firstUpdate && insert > secondUpdate,
					"expected UPDATE, UPDATE, INSERT in order but got: " + cql);
		}
	}

	@Nested
	@DisplayName("when a product is out of stock")
	class InsufficientStock {

		@Test
		void throwsWithTitleAndRemainingQuantity() {
			cartContains(new HashMap<>(Map.of(ASIN, 5)));
			inventoryHas(ASIN, 1);
			catalogHas(ASIN, "Widget", 4.5);

			NotEnoughProductsInStockException e = assertThrows(NotEnoughProductsInStockException.class,
					() -> service.checkout(USER_ID));

			assertEquals("Not enough Widget products in stock. Only 1 left", e.getMessage());
		}

		@Test
		void neitherWritesToTheDatabaseNorClearsTheCart() {
			cartContains(new HashMap<>(Map.of(ASIN, 5)));
			inventoryHas(ASIN, 1);
			catalogHas(ASIN, "Widget", 4.5);

			assertThrows(NotEnoughProductsInStockException.class, () -> service.checkout(USER_ID));

			verifyNoInteractions(cassandraTemplate, cqlOperations);
			verify(shoppingCartRestClient, never()).clearCart(anyString());
		}

		@Test
		void zeroStockRejectsAnyPositiveQuantity() {
			cartContains(new HashMap<>(Map.of(ASIN, 1)));
			inventoryHas(ASIN, 0);
			catalogHas(ASIN, "Widget", 4.5);

			NotEnoughProductsInStockException e = assertThrows(NotEnoughProductsInStockException.class,
					() -> service.checkout(USER_ID));

			assertEquals("Not enough Widget products in stock. Only 0 left", e.getMessage());
		}

		@Test
		void aLaterProductFailingAbortsTheWholeCheckout() {
			Map<String, Integer> products = new LinkedHashMap<>();
			products.put(ASIN, 1);
			products.put(OTHER_ASIN, 2);
			cartContains(products);
			inventoryHas(ASIN, 10);
			catalogHas(ASIN, "Widget", 4.5);
			inventoryHas(OTHER_ASIN, 1);
			catalogHas(OTHER_ASIN, "Gadget", 1.0);

			NotEnoughProductsInStockException e = assertThrows(NotEnoughProductsInStockException.class,
					() -> service.checkout(USER_ID));

			assertEquals("Not enough Gadget products in stock. Only 1 left", e.getMessage());
			verifyNoInteractions(cassandraTemplate, cqlOperations);
			verify(shoppingCartRestClient, never()).clearCart(anyString());
		}
	}

	@Nested
	@DisplayName("when downstream data is missing or a dependency fails")
	class Failures {

		@Test
		void missingInventoryRowCausesNullPointerException() {
			cartContains(new HashMap<>(Map.of(ASIN, 1)));
			when(productInventoryRepository.findById(ASIN)).thenReturn(Optional.empty());
			catalogHas(ASIN, "Widget", 4.5);

			assertThrows(NullPointerException.class, () -> service.checkout(USER_ID));

			verifyNoInteractions(cassandraTemplate, cqlOperations);
			verify(shoppingCartRestClient, never()).clearCart(anyString());
		}

		@Test
		void missingCatalogEntryCausesNullPointerExceptionEvenWhenInStock() {
			cartContains(new HashMap<>(Map.of(ASIN, 1)));
			inventoryHas(ASIN, 10);
			when(productCatalogRestClient.getProductDetails(ASIN)).thenReturn(null);

			assertThrows(NullPointerException.class, () -> service.checkout(USER_ID));

			verifyNoInteractions(cassandraTemplate, cqlOperations);
		}

		@Test
		void missingCatalogEntryMasksTheOutOfStockError() {
			cartContains(new HashMap<>(Map.of(ASIN, 5)));
			inventoryHas(ASIN, 1);
			when(productCatalogRestClient.getProductDetails(ASIN)).thenReturn(null);

			assertThrows(NullPointerException.class, () -> service.checkout(USER_ID));
		}

		@Test
		void cartClientFailurePropagatesBeforeAnythingElseHappens() {
			RuntimeException boom = new RuntimeException("cart down");
			when(shoppingCartRestClient.getProductsInCart(USER_ID)).thenThrow(boom);

			RuntimeException thrown = assertThrows(RuntimeException.class, () -> service.checkout(USER_ID));

			assertSame(boom, thrown);
			verifyNoInteractions(cassandraTemplate, productInventoryRepository, productCatalogRestClient);
		}

		@Test
		void databaseFailurePropagatesAndLeavesTheCartIntact() {
			cartContains(new HashMap<>(Map.of(ASIN, 1)));
			inventoryHas(ASIN, 10);
			catalogHas(ASIN, "Widget", 4.5);
			databaseAcceptsStatements();
			RuntimeException boom = new RuntimeException("db down");
			when(cqlOperations.execute(anyString())).thenThrow(boom);

			RuntimeException thrown = assertThrows(RuntimeException.class, () -> service.checkout(USER_ID));

			assertSame(boom, thrown);
			verify(shoppingCartRestClient, never()).clearCart(anyString());
		}

		@Test
		void unmodifiableCartMapFailsAfterTheDatabaseWriteButBeforeClearingTheCart() {
			cartContains(Map.of(ASIN, 1));
			inventoryHas(ASIN, 10);
			catalogHas(ASIN, "Widget", 4.5);
			databaseAcceptsStatements();

			assertThrows(UnsupportedOperationException.class, () -> service.checkout(USER_ID));

			verify(cqlOperations).execute(anyString());
			verify(shoppingCartRestClient, never()).clearCart(anyString());
		}
	}
}
