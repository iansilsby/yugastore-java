package com.yugabyte.app.yugastore;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

class YugastoreApiGatewayTest {

  @Test
  void hasExpectedAnnotations() {
    assertThat(YugastoreApiGateway.class.isAnnotationPresent(SpringBootApplication.class)).isTrue();
    assertThat(YugastoreApiGateway.class.isAnnotationPresent(EnableFeignClients.class)).isTrue();
  }

  @Test
  void hasPublicStaticMain() throws Exception {
    Method main = YugastoreApiGateway.class.getMethod("main", String[].class);

    assertThat(Modifier.isPublic(main.getModifiers())).isTrue();
    assertThat(Modifier.isStatic(main.getModifiers())).isTrue();
    assertThat(main.getReturnType()).isEqualTo(void.class);
  }
}
