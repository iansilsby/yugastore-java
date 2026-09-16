package com.yugabyte.app.yugastore.cart.domain;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShoppingCartTest {

	@Test
	void defaultsAreNullAndZero() {
		ShoppingCart cart = new ShoppingCart();

		assertThat(cart.getCartKey()).isNull();
		assertThat(cart.getUserId()).isNull();
		assertThat(cart.getAsin()).isNull();
		assertThat(cart.getTime_added()).isNull();
		assertThat(cart.getQuantity()).isZero();
	}

	@Test
	void settersAndGettersRoundTrip() {
		ShoppingCart cart = new ShoppingCart();
		cart.setCartKey("u1-A1");
		cart.setUserId("u1");
		cart.setAsin("A1");
		cart.setTime_added("2026-01-01T00:00:00");
		cart.setQuantity(-3);

		assertThat(cart.getCartKey()).isEqualTo("u1-A1");
		assertThat(cart.getUserId()).isEqualTo("u1");
		assertThat(cart.getAsin()).isEqualTo("A1");
		assertThat(cart.getTime_added()).isEqualTo("2026-01-01T00:00:00");
		assertThat(cart.getQuantity()).isEqualTo(-3);
	}

	@Test
	@DisplayName("no equals/hashCode override: identity semantics")
	void usesIdentityEquality() {
		ShoppingCart a = new ShoppingCart();
		ShoppingCart b = new ShoppingCart();
		a.setCartKey("k");
		b.setCartKey("k");

		assertThat(a).isNotEqualTo(b);
	}

	@Test
	@DisplayName("JPA mapping: entity/table 'shopping_cart', String @Id on cart_key, time_added stored as String")
	void jpaMapping() throws NoSuchFieldException {
		Entity entity = ShoppingCart.class.getAnnotation(Entity.class);
		Table table = ShoppingCart.class.getAnnotation(Table.class);
		assertThat(entity.name()).isEqualTo("shopping_cart");
		assertThat(table.name()).isEqualTo("shopping_cart");

		var cartKey = ShoppingCart.class.getDeclaredField("cartKey");
		assertThat(cartKey.isAnnotationPresent(Id.class)).isTrue();
		assertThat(cartKey.getAnnotation(Column.class).name()).isEqualTo("cart_key");
		assertThat(cartKey.getType()).isEqualTo(String.class);

		assertThat(ShoppingCart.class.getDeclaredField("userId").getAnnotation(Column.class).name()).isEqualTo("user_id");
		assertThat(ShoppingCart.class.getDeclaredField("asin").getAnnotation(Column.class).name()).isEqualTo("asin");
		assertThat(ShoppingCart.class.getDeclaredField("quantity").getAnnotation(Column.class).name()).isEqualTo("quantity");

		var timeAdded = ShoppingCart.class.getDeclaredField("time_added");
		assertThat(timeAdded.getAnnotation(Column.class).name()).isEqualTo("time_added");
		assertThat(timeAdded.getType()).isEqualTo(String.class);
	}
}
