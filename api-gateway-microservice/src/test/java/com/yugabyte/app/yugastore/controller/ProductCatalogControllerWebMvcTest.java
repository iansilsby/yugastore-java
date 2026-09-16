package com.yugabyte.app.yugastore.controller;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.yugabyte.app.yugastore.domain.ProductMetadata;
import com.yugabyte.app.yugastore.domain.ProductRanking;
import com.yugabyte.app.yugastore.service.ProductCatalogServiceRest;

class ProductCatalogControllerWebMvcTest {

  private ProductCatalogServiceRest productCatalogServiceRest;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    productCatalogServiceRest = mock(ProductCatalogServiceRest.class);
    mockMvc = MockMvcBuilders.standaloneSetup(new ProductCatalogController(productCatalogServiceRest)).build();
  }

  @Test
  void getProductDetailsReturnsJsonBody() throws Exception {
    ProductMetadata metadata = new ProductMetadata();
    metadata.setId("B001");
    metadata.setTitle("Some Book");
    when(productCatalogServiceRest.getProductDetails("B001")).thenReturn(metadata);

    mockMvc.perform(get("/api/v1/product/B001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("B001"))
        .andExpect(jsonPath("$.title").value("Some Book"));
  }

  @Test
  void getProductsBindsLimitAndOffsetByParameterName() throws Exception {
    when(productCatalogServiceRest.getProducts(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList(new ProductMetadata()));

    // Quirk: parameters are annotated with Spring Data's @Param, not @RequestParam.
    // Spring MVC still resolves unannotated simple-type parameters as request
    // parameters by name (the project is compiled with -parameters via
    // spring-boot-starter-parent), so the values are bound normally.
    mockMvc.perform(get("/api/v1/products").param("limit", "5").param("offset", "10"))
        .andExpect(status().isOk());

    verify(productCatalogServiceRest).getProducts(5, 10);
  }

  @Test
  void getProductsWithoutParamsFails() {
    // Same quirk: the parameters resolve as OPTIONAL request parameters, so the
    // request reaches argument resolution, which throws
    // "Optional int parameter 'limit' is present but cannot be translated into a
    // null value due to being declared as a primitive type". In standalone
    // MockMvc this propagates as a NestedServletException rather than a 500.
    assertThatThrownBy(() -> mockMvc.perform(get("/api/v1/products")))
        .isInstanceOf(NestedServletException.class)
        .hasRootCauseInstanceOf(IllegalStateException.class);
  }

  @Test
  void getProductsByCategoryPassesArgumentsThrough() throws Exception {
    when(productCatalogServiceRest.getProductsByCategory(eq("books"), anyInt(), anyInt()))
        .thenReturn(Collections.singletonList(new ProductRanking()));

    mockMvc.perform(get("/api/v1/products/category/books").param("limit", "2").param("offset", "0"))
        .andExpect(status().isOk());

    verify(productCatalogServiceRest).getProductsByCategory("books", 2, 0);
  }
}
