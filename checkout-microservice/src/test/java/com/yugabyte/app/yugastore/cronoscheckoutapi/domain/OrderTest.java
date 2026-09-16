package com.yugabyte.app.yugastore.cronoscheckoutapi.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

class OrderTest {

	private static Order order(String id) {
		Order order = new Order();
		order.setId(id);
		return order;
	}

	@Test
	void settersRoundTrip() {
		Order order = new Order();
		order.setId("order-1");
		order.setUser_id(1);
		order.setOrder_details("details");
		order.setOrder_time("2024-01-01T00:00");
		order.setOrder_total(12.5);

		assertEquals("order-1", order.getId());
		assertEquals(1, order.getUser_id());
		assertEquals("details", order.getOrder_details());
		assertEquals("2024-01-01T00:00", order.getOrder_time());
		assertEquals(12.5, order.getOrder_total());
	}

	@Test
	void equalityIsBasedOnIdOnly() {
		Order a = order("order-1");
		a.setOrder_total(1.0);
		Order b = order("order-1");
		b.setOrder_total(99.0);

		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
		assertNotEquals(order("order-1"), order("order-2"));
	}

	@Test
	void equalsHandlesSelfNullAndForeignTypes() {
		Order order = order("order-1");

		assertTrue(order.equals(order));
		assertFalse(order.equals(null));
		assertFalse(order.equals("order-1"));
	}

	@Test
	void equalsAndHashCodeFailOnAnUnsetId() {
		Order unset = new Order();

		assertThrows(NullPointerException.class, unset::hashCode);
		assertThrows(NullPointerException.class, () -> unset.equals(order("order-1")));
	}

	@Test
	void isMappedToTheOrdersTable() throws Exception {
		assertEquals("orders", Order.class.getAnnotation(Table.class).value());
		assertEquals("order_id", Order.class.getDeclaredField("id").getAnnotation(PrimaryKey.class).value());
		assertEquals("user_id", Order.class.getDeclaredField("user_id").getAnnotation(Column.class).value());
		assertEquals("order_details",
				Order.class.getDeclaredField("order_details").getAnnotation(Column.class).value());
		assertEquals("order_time", Order.class.getDeclaredField("order_time").getAnnotation(Column.class).value());
		assertEquals("order_total",
				Order.class.getDeclaredField("order_total").getAnnotation(Column.class).value());
	}
}
