package com.yugabyte.app.yugastore.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import com.yugabyte.app.yugastore.domain.ProductMetadata;
import com.yugabyte.app.yugastore.domain.ProductRanking;
import com.yugabyte.app.yugastore.service.ProductCatalogServiceRest;

@ExtendWith(MockitoExtension.class)
class ProductCatalogControllerTest {

  @Mock
  ProductCatalogServiceRest productCatalogServiceRest;

  @InjectMocks
  ProductCatalogController controller;

  @Test
  void getProductDetailsReturnsOkWithServiceBody() {
    ProductMetadata metadata = new ProductMetadata();
    metadata.setId("B001");
    when(productCatalogServiceRest.getProductDetails("B001")).thenReturn(metadata);

    ResponseEntity<ProductMetadata> response = controller.getProductDetails("B001");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isSameAs(metadata);
    verify(productCatalogServiceRest).getProductDetails("B001");
  }

  @Test
  void getProductDetailsReturnsOkWithNullBodyWhenServiceReturnsNull() {
    when(productCatalogServiceRest.getProductDetails("NOPE")).thenReturn(null);

    ResponseEntity<ProductMetadata> response = controller.getProductDetails("NOPE");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNull();
  }

  @Test
  void getProductsReturnsOkWithListAndPassesArgsThrough() {
    List<ProductMetadata> products = Arrays.asList(new ProductMetadata(), new ProductMetadata());
    when(productCatalogServiceRest.getProducts(5, 10)).thenReturn(products);

    ResponseEntity<List<ProductMetadata>> response = controller.getProducts(5, 10);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isSameAs(products);
    verify(productCatalogServiceRest).getProducts(5, 10);
  }

  @Test
  void getProductsByCategoryReturnsOkWithListAndPassesArgsThrough() {
    List<ProductRanking> rankings = Arrays.asList(new ProductRanking());
    when(productCatalogServiceRest.getProductsByCategory("books", 2, 0)).thenReturn(rankings);

    ResponseEntity<List<ProductRanking>> response = controller.getProductsByCategory("books", 2, 0);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isSameAs(rankings);
    verify(productCatalogServiceRest).getProductsByCategory("books", 2, 0);
  }

  @Test
  void getProductsParametersAreAnnotatedWithParamNotRequestParam() throws Exception {
    Method method = ProductCatalogController.class.getMethod("getProducts", int.class, int.class);

    for (Parameter parameter : method.getParameters()) {
      assertThat(parameter.isAnnotationPresent(RequestParam.class)).isFalse();
      assertThat(parameter.isAnnotationPresent(Param.class)).isTrue();
    }
  }
}
