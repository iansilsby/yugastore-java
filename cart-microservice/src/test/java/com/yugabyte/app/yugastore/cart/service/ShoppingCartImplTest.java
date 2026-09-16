package com.yugabyte.app.yugastore.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yugabyte.app.yugastore.cart.domain.ShoppingCart;
import com.yugabyte.app.yugastore.cart.repositories.ShoppingCartRepository;

@ExtendWith(MockitoExtension.class)
class ShoppingCartImplTest {

	private static final String USER = "u1";
	private static final String ASIN = "B000123";
	private static final String KEY = USER + "-" + ASIN;

	@Mock
	private ShoppingCartRepository repository;

	private ShoppingCartImpl service;

	@BeforeEach
	void setUp() {
		service = new ShoppingCartImpl(repository);
	}

	private static ShoppingCart cart(String userId, String asin, int qty) {
		ShoppingCart c = new ShoppingCart();
		c.setCartKey(userId + "-" + asin);
		c.setUserId(userId);
		c.setAsin(asin);
		c.setQuantity(qty);
		return c;
	}

	@Nested
	class AddProductToShoppingCart {

		@Test
		@DisplayName("existing row: increments quantity via update query, does not save")
		void existingProductIncrementsQuantity() {
			when(repository.findById(KEY)).thenReturn(Optional.of(cart(USER, ASIN, 2)));

			service.addProductToShoppingCart(USER, ASIN);

			verify(repository).findById(KEY);
			verify(repository).updateQuantityForShoppingCart(USER, ASIN);
			verify(repository, never()).save(any());
			verifyNoMoreInteractions(repository);
		}

		@Test
		@DisplayName("missing row: saves a new cart row with quantity 1 and derived key")
		void newProductIsSavedWithDefaultQuantity() {
			when(repository.findById(KEY)).thenReturn(Optional.empty());

			service.addProductToShoppingCart(USER, ASIN);

			ArgumentCaptor<ShoppingCart> captor = ArgumentCaptor.forClass(ShoppingCart.class);
			verify(repository).save(captor.capture());
			verify(repository, never()).updateQuantityForShoppingCart(anyString(), anyString());

			ShoppingCart saved = captor.getValue();
			assertThat(saved.getCartKey()).isEqualTo(KEY);
			assertThat(saved.getUserId()).isEqualTo(USER);
			assertThat(saved.getAsin()).isEqualTo(ASIN);
			assertThat(saved.getQuantity()).isEqualTo(1);
		}

		@Test
		@DisplayName("new row: time_added is populated with a LocalDateTime.toString() value")
		void newProductHasTimeAddedSet() {
			when(repository.findById(KEY)).thenReturn(Optional.empty());

			service.addProductToShoppingCart(USER, ASIN);

			ArgumentCaptor<ShoppingCart> captor = ArgumentCaptor.forClass(ShoppingCart.class);
			verify(repository).save(captor.capture());
			// LocalDateTime.toString() -> ISO-8601 without zone, e.g. 2026-09-16T23:04:11.123
			assertThat(captor.getValue().getTime_added()).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}.*");
		}

		@Test
		@DisplayName("key is built by plain string concatenation 'userId-asin' (ambiguous when ids contain '-')")
		void keyIsPlainConcatenation() {
			when(repository.findById("a-b-c")).thenReturn(Optional.empty());

			// user "a-b", asin "c" and user "a", asin "b-c" collide on the same key
			service.addProductToShoppingCart("a-b", "c");
			service.addProductToShoppingCart("a", "b-c");

			verify(repository, times(2)).findById("a-b-c");
			verify(repository, times(2)).save(any(ShoppingCart.class));
		}

		@Test
		@DisplayName("null userId/asin are concatenated into the literal string 'null-null'")
		void nullArgumentsProduceLiteralNullKey() {
			when(repository.findById("null-null")).thenReturn(Optional.empty());

			service.addProductToShoppingCart(null, null);

			ArgumentCaptor<ShoppingCart> captor = ArgumentCaptor.forClass(ShoppingCart.class);
			verify(repository).save(captor.capture());
			assertThat(captor.getValue().getCartKey()).isEqualTo("null-null");
			assertThat(captor.getValue().getUserId()).isNull();
			assertThat(captor.getValue().getAsin()).isNull();
		}

		@Test
		@DisplayName("repository exceptions propagate unchanged")
		void repositoryExceptionPropagates() {
			when(repository.findById(KEY)).thenThrow(new IllegalStateException("db down"));

			assertThatThrownBy(() -> service.addProductToShoppingCart(USER, ASIN))
					.isInstanceOf(IllegalStateException.class)
					.hasMessage("db down");
		}
	}

	@Nested
	class GetProductsInCart {

		@Test
		@DisplayName("maps asin -> quantity for each row")
		void mapsAsinToQuantity() {
			when(repository.findProductsInCartByUserId(USER)).thenReturn(
					Optional.of(Arrays.asList(cart(USER, "A1", 2), cart(USER, "A2", 5))));

			Map<String, Integer> result = service.getProductsInCart(USER);

			assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of("A1", 2, "A2", 5));
		}

		@Test
		@DisplayName("queries the repository twice (isPresent check + get)")
		void queriesRepositoryTwice() {
			when(repository.findProductsInCartByUserId(USER)).thenReturn(Optional.of(Collections.emptyList()));

			service.getProductsInCart(USER);

			verify(repository, times(2)).findProductsInCartByUserId(USER);
		}

		@Test
		@DisplayName("Optional.empty() from repository yields an empty map")
		void emptyOptionalYieldsEmptyMap() {
			when(repository.findProductsInCartByUserId(USER)).thenReturn(Optional.empty());

			assertThat(service.getProductsInCart(USER)).isEmpty();
			verify(repository, times(1)).findProductsInCartByUserId(USER);
		}

		@Test
		@DisplayName("empty list from repository yields an empty map")
		void emptyListYieldsEmptyMap() {
			when(repository.findProductsInCartByUserId(USER)).thenReturn(Optional.of(Collections.emptyList()));

			assertThat(service.getProductsInCart(USER)).isEmpty();
		}

		@Test
		@DisplayName("duplicate asins collapse: last row wins")
		void duplicateAsinLastWins() {
			when(repository.findProductsInCartByUserId(USER)).thenReturn(
					Optional.of(Arrays.asList(cart(USER, "A1", 2), cart(USER, "A1", 7))));

			assertThat(service.getProductsInCart(USER)).containsExactly(Map.entry("A1", 7));
		}

		@Test
		@DisplayName("a row with null asin becomes a null key (HashMap permits it)")
		void nullAsinBecomesNullKey() {
			when(repository.findProductsInCartByUserId(USER)).thenReturn(
					Optional.of(Collections.singletonList(cart(USER, null, 3))));

			Map<String, Integer> result = service.getProductsInCart(USER);

			assertThat(result).hasSize(1);
			assertThat(result.get(null)).isEqualTo(3);
		}

		@Test
		@DisplayName("returned map is a mutable HashMap")
		void returnsMutableMap() {
			when(repository.findProductsInCartByUserId(USER)).thenReturn(Optional.empty());

			Map<String, Integer> result = service.getProductsInCart(USER);
			result.put("x", 1);

			assertThat(result).containsEntry("x", 1);
		}
	}

	@Nested
	class RemoveProductFromCart {

		@Test
		@DisplayName("quantity > 1: decrements via update query")
		void quantityGreaterThanOneDecrements() {
			when(repository.findById(KEY)).thenReturn(Optional.of(cart(USER, ASIN, 3)));

			service.removeProductFromCart(USER, ASIN);

			verify(repository).decrementQuantityForShoppingCart(USER, ASIN);
			verify(repository, never()).deleteById(anyString());
			// isPresent + get: findById called twice
			verify(repository, times(2)).findById(KEY);
		}

		@Test
		@DisplayName("quantity == 1: deletes row by key")
		void quantityOneDeletes() {
			when(repository.findById(KEY)).thenReturn(Optional.of(cart(USER, ASIN, 1)));

			service.removeProductFromCart(USER, ASIN);

			verify(repository).deleteById(KEY);
			verify(repository, never()).decrementQuantityForShoppingCart(anyString(), anyString());
			// isPresent + get (>1 check) + get (==1 check): findById called three times
			verify(repository, times(3)).findById(KEY);
		}

		@Test
		@DisplayName("quantity == 0: neither decrements nor deletes (row is left in place)")
		void quantityZeroIsNoOp() {
			when(repository.findById(KEY)).thenReturn(Optional.of(cart(USER, ASIN, 0)));

			service.removeProductFromCart(USER, ASIN);

			verify(repository, times(3)).findById(KEY);
			verifyNoMoreInteractions(repository);
		}

		@Test
		@DisplayName("negative quantity: neither decrements nor deletes")
		void negativeQuantityIsNoOp() {
			when(repository.findById(KEY)).thenReturn(Optional.of(cart(USER, ASIN, -4)));

			service.removeProductFromCart(USER, ASIN);

			verify(repository, times(3)).findById(KEY);
			verifyNoMoreInteractions(repository);
		}

		@Test
		@DisplayName("missing row: no-op, single findById call")
		void missingRowIsNoOp() {
			when(repository.findById(KEY)).thenReturn(Optional.empty());

			service.removeProductFromCart(USER, ASIN);

			verify(repository, times(1)).findById(KEY);
			verifyNoMoreInteractions(repository);
		}

		@Test
		@DisplayName("non-atomic re-read: row vanishing between isPresent and get throws NoSuchElementException")
		void rowDisappearingBetweenReadsThrows() {
			when(repository.findById(KEY))
					.thenReturn(Optional.of(cart(USER, ASIN, 2)))
					.thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.removeProductFromCart(USER, ASIN))
					.isInstanceOf(NoSuchElementException.class);
		}
	}

	@Nested
	class ClearCart {

		@Test
		@DisplayName("Optional present (even with empty list): deletes all rows for user")
		void presentOptionalDeletes() {
			when(repository.findProductsInCartByUserId(USER)).thenReturn(Optional.of(Collections.emptyList()));

			service.clearCart(USER);

			verify(repository).findProductsInCartByUserId(USER);
			verify(repository).deleteProductsInCartByUserId(USER);
			verifyNoMoreInteractions(repository);
		}

		@Test
		@DisplayName("Optional.empty(): does not delete")
		void emptyOptionalDoesNotDelete() {
			when(repository.findProductsInCartByUserId(USER)).thenReturn(Optional.empty());

			service.clearCart(USER);

			verify(repository, never()).deleteProductsInCartByUserId(anyString());
		}

		@Test
		@DisplayName("return value of delete query is ignored")
		void deleteCountIgnored() {
			when(repository.findProductsInCartByUserId(USER))
					.thenReturn(Optional.of(Collections.singletonList(cart(USER, ASIN, 1))));
			when(repository.deleteProductsInCartByUserId(USER)).thenReturn(0);

			service.clearCart(USER);

			verify(repository).deleteProductsInCartByUserId(USER);
		}
	}
}
