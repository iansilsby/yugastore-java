package com.yugabyte.app.yugastore.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;

import com.yugabyte.app.yugastore.domain.ProductMetadata;
import com.yugabyte.app.yugastore.domain.ProductRanking;
import com.yugabyte.app.yugastore.domain.ProductRankingKey;
import com.yugabyte.app.yugastore.service.ProductRankingService;
import com.yugabyte.app.yugastore.service.ProductService;

@ExtendWith(MockitoExtension.class)
class ProductCatalogControllerTest {

    @Mock
    ProductService productService;

    @Mock
    ProductRankingService productRankingService;

    @InjectMocks
    ProductCatalogController controller;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private ProductMetadata product(String id, String title) {
        ProductMetadata p = new ProductMetadata();
        p.setId(id);
        p.setTitle(title);
        return p;
    }

    // ---- direct method calls ----

    @Test
    void getProductDetailsUnwrapsOptionalFromService() {
        ProductMetadata p = product("A1", "Widget");
        when(productService.findById("A1")).thenReturn(Optional.of(p));

        assertSame(p, controller.getProductDetails("A1"));
    }

    @Test
    void getProductDetailsThrowsNoSuchElementWhenProductMissing() {
        when(productService.findById("missing")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> controller.getProductDetails("missing"));
    }

    @Test
    void getProductsDelegatesLimitAndOffset() {
        List<ProductMetadata> products = Arrays.asList(product("A1", "a"), product("A2", "b"));
        when(productService.findAllProductsPageable(2, 4)).thenReturn(products);

        assertSame(products, controller.getProducts(2, 4));
        verify(productService).findAllProductsPageable(2, 4);
    }

    @Test
    void getProductsByCategoryDelegatesAllArguments() {
        List<ProductRanking> rankings = Collections.singletonList(new ProductRanking());
        when(productRankingService.getProductsByCategory("books", 3, 6)).thenReturn(rankings);

        assertSame(rankings, controller.getProductsByCategory("books", 3, 6));
        verify(productRankingService).getProductsByCategory("books", 3, 6);
    }

    // ---- HTTP layer (standalone MockMvc, no Spring context) ----

    @Test
    void httpGetProductReturnsJsonBody() throws Exception {
        ProductMetadata p = product("A1", "Widget");
        p.setPrice(9.5);
        when(productService.findById("A1")).thenReturn(Optional.of(p));

        mockMvc.perform(get("/products-microservice/product/A1"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value("A1"))
            .andExpect(jsonPath("$.title").value("Widget"))
            .andExpect(jsonPath("$.price").value(9.5));
    }

    @Test
    void httpGetUnknownProductPropagatesNoSuchElementException() {
        when(productService.findById("missing")).thenReturn(Optional.empty());

        NestedServletException ex = assertThrows(NestedServletException.class,
            () -> mockMvc.perform(get("/products-microservice/product/missing")));
        assertTrue(ex.getCause() instanceof NoSuchElementException);
    }

    @Test
    void httpGetProductsBindsLimitAndOffsetQueryParametersByName() throws Exception {
        when(productService.findAllProductsPageable(5, 10))
            .thenReturn(Arrays.asList(product("A1", "a"), product("A2", "b")));

        mockMvc.perform(get("/products-microservice/products").param("limit", "5").param("offset", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value("A1"))
            .andExpect(jsonPath("$[1].id").value("A2"));
    }

    @Test
    void httpGetProductsWithoutQueryParametersFailsBecausePrimitivesCannotBeNull() {
        NestedServletException ex = assertThrows(NestedServletException.class,
            () -> mockMvc.perform(get("/products-microservice/products")));
        assertTrue(ex.getCause() instanceof IllegalStateException, String.valueOf(ex.getCause()));
        verify(productService, never()).findAllProductsPageable(anyInt(), anyInt());
    }

    @Test
    void httpGetProductsWithNonNumericLimitIsBadRequest() throws Exception {
        mockMvc.perform(get("/products-microservice/products").param("limit", "abc").param("offset", "0"))
            .andExpect(status().isBadRequest());
        verify(productService, never()).findAllProductsPageable(anyInt(), anyInt());
    }

    @Test
    void httpGetProductsByCategoryReturnsRankingsAsJson() throws Exception {
        ProductRankingKey key = new ProductRankingKey();
        key.setId("A1");
        key.setCategory("books");
        ProductRanking r = new ProductRanking();
        r.setId(key);
        r.setSalesRank(7);
        r.setTitle("Ranked");
        when(productRankingService.getProductsByCategory("books", 1, 0))
            .thenReturn(Collections.singletonList(r));

        mockMvc.perform(get("/products-microservice/products/category/books")
                .param("limit", "1").param("offset", "0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id.asin").value("A1"))
            .andExpect(jsonPath("$[0].id.category").value("books"))
            .andExpect(jsonPath("$[0].salesRank").value(7))
            .andExpect(jsonPath("$[0].title").value("Ranked"));
        verify(productRankingService).getProductsByCategory("books", 1, 0);
        verify(productRankingService, never()).findProductRankingById(anyString());
    }

    @Test
    void httpGetProductsByCategoryWithEmptyResultReturnsEmptyArray() throws Exception {
        when(productRankingService.getProductsByCategory("none", 10, 0)).thenReturn(Collections.emptyList());

        String body = mockMvc.perform(get("/products-microservice/products/category/none")
                .param("limit", "10").param("offset", "0"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        assertEquals("[]", body);
    }
}
