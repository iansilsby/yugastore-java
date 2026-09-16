package com.yugabyte.app.yugastore.cart.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.Serializable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Characterizes the hand-written equals/hashCode of {@link ShoppingCartKey}. Several of these
 * behaviours are defects (documented in the PR); the tests pin the current behaviour.
 */
class ShoppingCartKeyTest {

	@Test
	void constructorAndAccessors() {
		ShoppingCartKey key = new ShoppingCartKey("u1", "A1");

		assertThat(key.getId()).isEqualTo("u1");
		assertThat(key.getAsin()).isEqualTo("A1");
		assertThat(key).isInstanceOf(Serializable.class);
	}

	@Test
	void noArgConstructorLeavesFieldsNull() {
		ShoppingCartKey key = new ShoppingCartKey();

		assertThat(key.getId()).isNull();
		assertThat(key.getAsin()).isNull();
	}

	@Test
	void settersMutateFields() {
		ShoppingCartKey key = new ShoppingCartKey();
		key.setId("u2");
		key.setAsin("A2");

		assertThat(key.getId()).isEqualTo("u2");
		assertThat(key.getAsin()).isEqualTo("A2");
	}

	@Test
	void equalsIsReflexiveAndRejectsNullAndOtherTypes() {
		ShoppingCartKey key = new ShoppingCartKey("u1", "A1");

		assertThat(key.equals(key)).isTrue();
		assertThat(key.equals(null)).isFalse();
		assertThat(key.equals("u1-A1")).isFalse();
	}

	@Test
	@DisplayName("QUIRK: equals ignores both id and asin when id is non-null -> any two keys with non-null ids are equal")
	void equalsIgnoresFieldsWhenIdNonNull() {
		assertThat(new ShoppingCartKey("u1", "A1")).isEqualTo(new ShoppingCartKey("u1", "A1"));
		assertThat(new ShoppingCartKey("u1", "A1")).isEqualTo(new ShoppingCartKey("u1", "OTHER"));
		assertThat(new ShoppingCartKey("u1", "A1")).isEqualTo(new ShoppingCartKey("someone-else", "A1"));
		assertThat(new ShoppingCartKey("u1", "A1")).isEqualTo(new ShoppingCartKey("x", null));
	}

	@Test
	@DisplayName("QUIRK: null id vs non-null id -> not equal")
	void nullIdVersusNonNullIdIsNotEqual() {
		assertThat(new ShoppingCartKey(null, "A1")).isNotEqualTo(new ShoppingCartKey("u1", "A1"));
	}

	@Test
	@DisplayName("BUG: comparing two keys with null ids throws NullPointerException (dereferences null id)")
	void equalsWithBothIdsNullThrowsNpe() {
		ShoppingCartKey a = new ShoppingCartKey(null, "A1");
		ShoppingCartKey b = new ShoppingCartKey(null, "A1");

		assertThatThrownBy(() -> a.equals(b)).isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("QUIRK: hashCode is computed from asin/id swapped (null-check on one, hash of the other)")
	void hashCodeFormula() {
		ShoppingCartKey key = new ShoppingCartKey("u1", "A1");

		int expected = 31 * (31 * 1 + "A1".hashCode()) + "u1".hashCode();
		assertThat(key.hashCode()).isEqualTo(expected);
	}

	@Test
	void hashCodeOfAllNullKeyIsStable() {
		assertThat(new ShoppingCartKey().hashCode()).isEqualTo(31 * 31);
	}

	@Test
	@DisplayName("BUG: hashCode throws NPE when exactly one of id/asin is null")
	void hashCodeThrowsWhenOnlyOneFieldNull() {
		assertThatThrownBy(() -> new ShoppingCartKey("u1", null).hashCode())
				.isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> new ShoppingCartKey(null, "A1").hashCode())
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("BUG: equals/hashCode contract broken -> equal objects can have different hash codes")
	void equalObjectsMayHaveDifferentHashCodes() {
		ShoppingCartKey a = new ShoppingCartKey("u1", "A1");
		ShoppingCartKey b = new ShoppingCartKey("u2", "A2");

		assertThat(a).isEqualTo(b);
		assertThat(a.hashCode()).isNotEqualTo(b.hashCode());
	}
}
