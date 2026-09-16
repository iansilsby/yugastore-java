package com.yugabyte.yugastore.ui.config;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class CronosConfigTest {

	@Mock
	RestTemplateBuilder builder;

	@Test
	void createRestTemplateReturnsBuiltInstance() {
		RestTemplate restTemplate = new RestTemplate();
		when(builder.build()).thenReturn(restTemplate);
		RestTemplate result = new CronosConfig().createRestTemplate(builder);
		assertSame(restTemplate, result);
		verify(builder, times(1)).build();
	}
}
