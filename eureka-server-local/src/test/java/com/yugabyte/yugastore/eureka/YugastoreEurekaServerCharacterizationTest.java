package com.yugabyte.yugastore.eureka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.cloud.netflix.eureka.server.EurekaServerAutoConfiguration;
import org.springframework.cloud.netflix.eureka.server.EurekaServerMarkerConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Characterization tests for {@link YugastoreEurekaServer}. These document the
 * module's current behaviour without booting a Spring context.
 */
@ExtendWith(MockitoExtension.class)
class YugastoreEurekaServerCharacterizationTest {

	@Nested
	@DisplayName("class shape")
	class ClassShape {

		@Test
		void isPublicNonFinalNonAbstractClass() {
			int mods = YugastoreEurekaServer.class.getModifiers();
			assertTrue(Modifier.isPublic(mods));
			assertFalse(Modifier.isFinal(mods));
			assertFalse(Modifier.isAbstract(mods));
			assertFalse(YugastoreEurekaServer.class.isInterface());
		}

		@Test
		void livesInEurekaPackage() {
			assertEquals("com.yugabyte.yugastore.eureka", YugastoreEurekaServer.class.getPackageName());
		}

		@Test
		void hasOnlyImplicitPublicNoArgConstructor() throws Exception {
			Constructor<?>[] ctors = YugastoreEurekaServer.class.getDeclaredConstructors();
			assertEquals(1, ctors.length);
			Constructor<?> ctor = YugastoreEurekaServer.class.getDeclaredConstructor();
			assertTrue(Modifier.isPublic(ctor.getModifiers()));
			assertNotNull(ctor.newInstance());
		}

		@Test
		void declaresExactlyOneMethodNamedMain() {
			Method[] methods = YugastoreEurekaServer.class.getDeclaredMethods();
			assertEquals(1, methods.length);
			assertEquals("main", methods[0].getName());
		}

		@Test
		void mainIsPublicStaticVoidTakingStringArray() throws Exception {
			Method main = YugastoreEurekaServer.class.getDeclaredMethod("main", String[].class);
			int mods = main.getModifiers();
			assertTrue(Modifier.isPublic(mods));
			assertTrue(Modifier.isStatic(mods));
			assertEquals(void.class, main.getReturnType());
			assertEquals(0, main.getExceptionTypes().length);
		}
	}

	@Nested
	@DisplayName("annotations")
	class Annotations {

		@Test
		void isAnnotatedWithSpringBootApplication() {
			assertTrue(YugastoreEurekaServer.class.isAnnotationPresent(SpringBootApplication.class));
		}

		@Test
		void springBootApplicationUsesDefaults() {
			SpringBootApplication ann = YugastoreEurekaServer.class.getAnnotation(SpringBootApplication.class);
			assertEquals(0, ann.exclude().length);
			assertEquals(0, ann.excludeName().length);
			assertEquals(0, ann.scanBasePackages().length);
			assertEquals(0, ann.scanBasePackageClasses().length);
			assertTrue(ann.proxyBeanMethods());
		}

		@Test
		void isAnnotatedWithEnableEurekaServer() {
			assertTrue(YugastoreEurekaServer.class.isAnnotationPresent(EnableEurekaServer.class));
		}

		@Test
		void enableEurekaServerImportsMarkerConfiguration() {
			Import imp = EnableEurekaServer.class.getAnnotation(Import.class);
			assertNotNull(imp);
			assertEquals(1, imp.value().length);
			assertSame(EurekaServerMarkerConfiguration.class, imp.value()[0]);
		}

		@Test
		void hasExactlyTwoAnnotations() {
			assertEquals(2, YugastoreEurekaServer.class.getAnnotations().length);
		}

		@Test
		void eurekaServerAutoConfigurationIsOnClasspath() {
			assertNotNull(EurekaServerAutoConfiguration.class);
		}
	}

	@Nested
	@DisplayName("main")
	class Main {

		@Test
		void delegatesToSpringApplicationRunWithOwnClassAndArgs() {
			String[] args = { "--server.port=0", "--foo=bar" };
			try (MockedStatic<SpringApplication> spring = mockStatic(SpringApplication.class)) {
				YugastoreEurekaServer.main(args);

				spring.verify(() -> SpringApplication.run(YugastoreEurekaServer.class, "--server.port=0", "--foo=bar"));
				spring.verifyNoMoreInteractions();
			}
		}

		@Test
		void passesEmptyArgsThroughUnchanged() {
			try (MockedStatic<SpringApplication> spring = mockStatic(SpringApplication.class)) {
				YugastoreEurekaServer.main(new String[0]);
				spring.verify(() -> SpringApplication.run(YugastoreEurekaServer.class));
				spring.verifyNoMoreInteractions();
			}
		}

		@Test
		void passesNullArgsThroughToSpringApplication() {
			try (MockedStatic<SpringApplication> spring = mockStatic(SpringApplication.class)) {
				YugastoreEurekaServer.main(null);
				spring.verify(() -> SpringApplication.run(YugastoreEurekaServer.class, (String[]) null));
			}
		}

		@Test
		void runsOnceOnly() {
			try (MockedStatic<SpringApplication> spring = mockStatic(SpringApplication.class)) {
				YugastoreEurekaServer.main(new String[] { "a" });
				spring.verify(() -> SpringApplication.run(YugastoreEurekaServer.class, "a"), times(1));
			}
		}

		@Test
		void discardsReturnedContextAndDoesNotCloseIt() {
			GenericApplicationContext ctx = new GenericApplicationContext();
			ctx.refresh();
			try (MockedStatic<SpringApplication> spring = mockStatic(SpringApplication.class)) {
				spring.when(() -> SpringApplication.run(any(Class.class), any(String[].class))).thenReturn(ctx);
				YugastoreEurekaServer.main(new String[0]);
			}
			assertTrue(ctx.isActive());
			ctx.close();
		}

		@Test
		void propagatesExceptionsFromSpringApplicationRun() {
			IllegalStateException boom = new IllegalStateException("boom");
			try (MockedStatic<SpringApplication> spring = mockStatic(SpringApplication.class)) {
				spring.when(() -> SpringApplication.run(any(Class.class), any(String[].class))).thenThrow(boom);
				IllegalStateException thrown = assertThrows(IllegalStateException.class,
						() -> YugastoreEurekaServer.main(new String[0]));
				assertSame(boom, thrown);
			}
		}
	}
}
