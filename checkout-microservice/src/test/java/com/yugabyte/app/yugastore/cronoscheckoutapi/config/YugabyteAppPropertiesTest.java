package com.yugabyte.app.yugastore.cronoscheckoutapi.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class YugabyteAppPropertiesTest {

	@Test
	void keyspaceIsUnsetUntilInjected() {
		assertNull(new YugabyteAppProperties().getKeyspace());
	}

	@Test
	void exposesTheInjectedKeyspace() {
		YugabyteAppProperties properties = new YugabyteAppProperties();
		ReflectionTestUtils.setField(properties, "keyspace", "cronos");

		assertEquals("cronos", properties.getKeyspace());
	}
}
