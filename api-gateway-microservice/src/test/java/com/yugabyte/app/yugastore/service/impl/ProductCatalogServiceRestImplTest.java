package com.yugabyte.app.yugastore.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yugabyte.app.yugastore.domain.ProductMetadata;
import com.yugabyte.app.yugastore.domain.ProductRanking;
import com.yugabyte.app.yugastore.rest.clients.ProductCatalogRestClient;

@ExtendWith(MockitoExtension.class)
class ProductCatalogServiceRestImplTest {

  @Mock
  ProductCatalogRestClient productCatalogRestClient;

  @InjectMocks
  ProductCatalogServiceRestImpl service;

  @Test
  void getProductDetailsDelegatesAndReturnsClientResult() {
    ProductMetadata metadata = new ProductMetadata();
    when(productCatalogRestClient.getProductDetails("B001")).thenReturn(metadata);

    assertThat(service.getProductDetails("B001")).isSameAs(metadata);
    verify(productCatalogRestClient).getProductDetails("B001");
  }

  @Test
  void getProductDetailsPassesNullThrough() {
    when(productCatalogRestClient.getProductDetails("NOPE")).thenReturn(null);

    assertThat(service.getProductDetails("NOPE")).isNull();
  }

  @Test
  void getProductsDelegatesAndReturnsClientResult() {
    List<ProductMetadata> products = Arrays.asList(new ProductMetadata());
    when(productCatalogRestClient.getProducts(5, 10)).thenReturn(products);

    assertThat(service.getProducts(5, 10)).isSameAs(products);
    verify(productCatalogRestClient).getProducts(5, 10);
  }

  @Test
  void getProductsByCategoryDelegatesAndReturnsClientResult() {
    List<ProductRanking> rankings = Arrays.asList(new ProductRanking());
    when(productCatalogRestClient.getProductsByCategory("books", 2, 0)).thenReturn(rankings);

    assertThat(service.getProductsByCategory("books", 2, 0)).isSameAs(rankings);
    verify(productCatalogRestClient).getProductsByCategory("books", 2, 0);
  }

  @Test
  void clientExceptionPropagatesUnchanged() {
    RuntimeException boom = new RuntimeException("feign blew up");
    when(productCatalogRestClient.getProductDetails("B001")).thenThrow(boom);

    assertThatThrownBy(() -> service.getProductDetails("B001")).isSameAs(boom);
  }
}
