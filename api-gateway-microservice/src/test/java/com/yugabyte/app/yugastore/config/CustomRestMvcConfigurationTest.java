package com.yugabyte.app.yugastore.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.data.rest.core.config.EnumTranslationConfiguration;
import org.springframework.data.rest.core.config.MetadataConfiguration;
import org.springframework.data.rest.core.config.ProjectionDefinitionConfiguration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import com.yugabyte.app.yugastore.domain.Order;
import com.yugabyte.app.yugastore.domain.ProductMetadata;
import com.yugabyte.app.yugastore.domain.ProductRanking;

class CustomRestMvcConfigurationTest {

  @Test
  void configurerSetsBasePathAndExposesIds() {
    RepositoryRestConfigurer configurer = new CustomRestMvcConfiguration().repositoryRestConfigurer();
    RepositoryRestConfiguration config = new RepositoryRestConfiguration(
        new ProjectionDefinitionConfiguration(),
        new MetadataConfiguration(),
        mock(EnumTranslationConfiguration.class));

    configurer.configureRepositoryRestConfiguration(config, new CorsRegistry());

    assertThat(config.getBasePath().toString()).isEqualTo("/api/v1");
    assertThat(config.isIdExposedFor(ProductMetadata.class)).isTrue();
    assertThat(config.isIdExposedFor(ProductRanking.class)).isTrue();
    assertThat(config.isIdExposedFor(Order.class)).isFalse();
  }
}
