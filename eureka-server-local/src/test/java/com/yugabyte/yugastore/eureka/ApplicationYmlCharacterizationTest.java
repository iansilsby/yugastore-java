package com.yugabyte.yugastore.eureka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

/**
 * Characterizes the packaged src/main/resources/application.yml using Spring
 * Boot's own YAML loader, without starting a Spring context.
 */
class ApplicationYmlCharacterizationTest {

	private static EnumerablePropertySource<?> props;

	@BeforeAll
	static void load() throws IOException {
		List<PropertySource<?>> sources = new YamlPropertySourceLoader().load("application.yml",
				new ClassPathResource("application.yml"));
		assertEquals(1, sources.size());
		props = (EnumerablePropertySource<?>) sources.get(0);
	}

	@Test
	void listensOnPort8761() {
		assertEquals(8761, props.getProperty("server.port"));
	}

	@Test
	void doesNotRegisterItselfWithEureka() {
		assertEquals(false, props.getProperty("eureka.client.register-with-eureka"));
	}

	@Test
	void doesNotFetchRegistry() {
		assertEquals(false, props.getProperty("eureka.client.fetch-registry"));
	}

	@Test
	void rootLoggingIsInfoString() {
		assertEquals("info", props.getProperty("logging.level.root"));
	}

	/**
	 * Oddity: the unquoted YAML scalar {@code OFF} is parsed as the boolean
	 * {@code false} (YAML 1.1 boolean literal), not the string "OFF". Spring
	 * Boot's LoggingApplicationListener happens to map "false" to LogLevel.OFF,
	 * so logging is still disabled, but only by coincidence.
	 */
	@Test
	void netflixLoggingLevelsParseAsBooleanFalseNotStringOFF() {
		assertEquals(Boolean.FALSE, props.getProperty("logging.level.com.netflix.eureka"));
		assertEquals(Boolean.FALSE, props.getProperty("logging.level.com.netflix.discovery"));
	}

	@Test
	void definesExactlySixProperties() {
		assertEquals(6, props.getPropertyNames().length);
	}

	@Test
	void doesNotSetApplicationNameOrServiceUrl() {
		assertNull(props.getProperty("spring.application.name"));
		assertNull(props.getProperty("eureka.client.service-url.defaultZone"));
		assertNull(props.getProperty("eureka.client.serviceUrl.defaultZone"));
		assertFalse(props.containsProperty("eureka.instance.hostname"));
	}

	@Test
	void noCloudProfileFileOnClasspath() {
		assertFalse(new ClassPathResource("application-cloud.yml").exists());
	}
}
