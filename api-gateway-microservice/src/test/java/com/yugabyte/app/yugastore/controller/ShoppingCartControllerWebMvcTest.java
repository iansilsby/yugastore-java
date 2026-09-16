package com.yugabyte.app.yugastore.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.yugabyte.app.yugastore.domain.CheckoutStatus;
import com.yugabyte.app.yugastore.service.CheckoutServiceRest;
import com.yugabyte.app.yugastore.service.ShoppingCartServiceRest;

class ShoppingCartControllerWebMvcTest {

  private ShoppingCartServiceRest shoppingCartServiceRest;
  private CheckoutServiceRest checkoutServiceRest;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    shoppingCartServiceRest = mock(ShoppingCartServiceRest.class);
    checkoutServiceRest = mock(CheckoutServiceRest.class);
    mockMvc = MockMvcBuilders
        .standaloneSetup(new ShoppingCartController(shoppingCartServiceRest, checkoutServiceRest))
        .build();
  }

  @Test
  void postShoppingCartReturnsJsonMap() throws Exception {
    when(shoppingCartServiceRest.getProductsInCart("u1001"))
        .thenReturn(Collections.singletonMap("B001", 2));

    mockMvc.perform(post("/api/v1/shoppingCart"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.B001").value(2));
  }

  @Test
  void postAddProductCallsServiceWithHardcodedUser() throws Exception {
    when(shoppingCartServiceRest.getProductsInCart("u1001"))
        .thenReturn(Collections.singletonMap("B001", 1));

    mockMvc.perform(post("/api/v1/shoppingCart/addProduct").param("asin", "B001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.B001").value(1));

    verify(shoppingCartServiceRest).addProduct("u1001", "B001");
  }

  @Test
  void postAddProductWithoutAsinIsBadRequest() throws Exception {
    mockMvc.perform(post("/api/v1/shoppingCart/addProduct"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void postRemoveProductCallsService() throws Exception {
    when(shoppingCartServiceRest.getProductsInCart("u1001"))
        .thenReturn(Collections.emptyMap());

    mockMvc.perform(post("/api/v1/shoppingCart/removeProduct").param("asin", "B001"))
        .andExpect(status().isOk());

    verify(shoppingCartServiceRest).removeProduct("u1001", "B001");
  }

  @Test
  void postCheckoutReturnsStatusJson() throws Exception {
    CheckoutStatus status = new CheckoutStatus();
    status.setStatus(CheckoutStatus.SUCCESS);
    status.setOrderNumber("ORDER-42");
    when(checkoutServiceRest.checkout()).thenReturn(status);

    mockMvc.perform(post("/api/v1/shoppingCart/checkout"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.orderNumber").value("ORDER-42"));
  }

  @Test
  void getShoppingCartIsMethodNotAllowed() throws Exception {
    mockMvc.perform(get("/api/v1/shoppingCart"))
        .andExpect(status().isMethodNotAllowed());
  }

  @Test
  void postShoppingCartReturns500WhenServiceReturnsNull() throws Exception {
    when(shoppingCartServiceRest.getProductsInCart("u1001")).thenReturn(null);

    mockMvc.perform(post("/api/v1/shoppingCart"))
        .andExpect(status().isInternalServerError());
  }
}
