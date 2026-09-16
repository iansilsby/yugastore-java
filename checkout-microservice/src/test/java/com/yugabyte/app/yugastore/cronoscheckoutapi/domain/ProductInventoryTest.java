package com.yugabyte.app.yugastore.cronoscheckoutapi.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

class ProductInventoryTest {

	private static ProductInventory inventory(String asin, Integer quantity) {
		ProductInventory inventory = new ProductInventory();
		inventory.setId(asin);
		inventory.setQuantity(quantity);
		return inventory;
	}

	@Test
	void settersRoundTrip() {
		ProductInventory inventory = inventory("B00001", 7);

		assertEquals("B00001", inventory.getId());
		assertEquals(7, inventory.getQuantity());
	}

	@Test
	void equalityIgnoresQuantity() {
		assertEquals(inventory("B00001", 1), inventory("B00001", 500));
		assertEquals(inventory("B00001", 1).hashCode(), inventory("B00001", 500).hashCode());
		assertNotEquals(inventory("B00001", 1), inventory("B00002", 1));
		assertFalse(inventory("B00001", 1).equals(null));
		assertFalse(inventory("B00001", 1).equals("B00001"));
	}

	@Test
	void equalsAndHashCodeFailOnAnUnsetId() {
		ProductInventory unset = new ProductInventory();

		assertThrows(NullPointerException.class, unset::hashCode);
		assertThrows(NullPointerException.class, () -> unset.equals(inventory("B00001", 1)));
	}

	@Test
	void isMappedToTheProductInventoryTableKeyedByAsin() throws Exception {
		assertEquals("product_inventory", ProductInventory.class.getAnnotation(Table.class).value());
		assertEquals("asin", ProductInventory.class.getDeclaredField("id").getAnnotation(PrimaryKey.class).value());
		assertEquals("quantity",
				ProductInventory.class.getDeclaredField("quantity").getAnnotation(Column.class).value());
	}
}
