package com.yugabyte.app.yugastore.rest.clients;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

class FeignClientContractTest {

  @Test
  void feignClientNames() {
    assertThat(CheckoutRestClient.class.getAnnotation(FeignClient.class).value())
        .isEqualTo("checkout-microservice");
    assertThat(ProductCatalogRestClient.class.getAnnotation(FeignClient.class).value())
        .isEqualTo("products-microservice");
    assertThat(ShoppingCartRestClient.class.getAnnotation(FeignClient.class).value())
        .isEqualTo("cart-microservice");
  }

  @Test
  void checkoutClientCheckoutMapping() throws Exception {
    Method method = CheckoutRestClient.class.getMethod("checkout");
    RequestMapping mapping = method.getAnnotation(RequestMapping.class);

    assertThat(mapping.value()).containsExactly("/checkout-microservice/shoppingCart/checkout");
    assertThat(mapping.method()).containsExactly(RequestMethod.POST);
  }

  @Test
  void productCatalogClientMappings() throws Exception {
    Method getProductDetails = ProductCatalogRestClient.class.getMethod("getProductDetails", String.class);
    assertThat(getProductDetails.getAnnotation(RequestMapping.class).value())
        .containsExactly("/products-microservice/product/{asin}");
    // Quirk: no HTTP method is declared on these mappings.
    assertThat(getProductDetails.getAnnotation(RequestMapping.class).method()).isEmpty();
    assertThat(getProductDetails.getParameters()[0].getAnnotation(PathVariable.class).value())
        .isEqualTo("asin");

    Method getProducts = ProductCatalogRestClient.class.getMethod("getProducts", int.class, int.class);
    assertThat(getProducts.getAnnotation(RequestMapping.class).value())
        .containsExactly("/products-microservice/products");
    assertThat(getProducts.getParameters()[0].getAnnotation(RequestParam.class).value())
        .isEqualTo("limit");
    assertThat(getProducts.getParameters()[1].getAnnotation(RequestParam.class).value())
        .isEqualTo("offset");

    Method getByCategory = ProductCatalogRestClient.class
        .getMethod("getProductsByCategory", String.class, int.class, int.class);
    assertThat(getByCategory.getAnnotation(RequestMapping.class).value())
        .containsExactly("/products-microservice/products/category/{category}");
    Parameter[] params = getByCategory.getParameters();
    assertThat(params[0].getAnnotation(PathVariable.class).value()).isEqualTo("category");
    assertThat(params[1].getAnnotation(RequestParam.class).value()).isEqualTo("limit");
    assertThat(params[2].getAnnotation(RequestParam.class).value()).isEqualTo("offset");
  }

  @Test
  void shoppingCartClientMappingsDeclareNoHttpMethod() throws Exception {
    // Quirk: the cart endpoints mutate state but the @RequestMapping annotations
    // declare no HTTP method (empty method array).
    Method addProduct = ShoppingCartRestClient.class
        .getMethod("addProductToCart", String.class, String.class);
    assertThat(addProduct.getAnnotation(RequestMapping.class).value())
        .containsExactly("/cart-microservice/shoppingCart/addProduct");
    assertThat(addProduct.getAnnotation(RequestMapping.class).method()).isEmpty();
    assertThat(addProduct.getParameters()[0].getAnnotation(RequestParam.class).value())
        .isEqualTo("userid");
    assertThat(addProduct.getParameters()[1].getAnnotation(RequestParam.class).value())
        .isEqualTo("asin");

    Method productsInCart = ShoppingCartRestClient.class.getMethod("getProductsInCart", String.class);
    assertThat(productsInCart.getAnnotation(RequestMapping.class).value())
        .containsExactly("/cart-microservice/shoppingCart/productsInCart");
    assertThat(productsInCart.getAnnotation(RequestMapping.class).method()).isEmpty();
    assertThat(productsInCart.getParameters()[0].getAnnotation(RequestParam.class).value())
        .isEqualTo("userid");

    Method removeProduct = ShoppingCartRestClient.class
        .getMethod("removeProductFromCart", String.class, String.class);
    assertThat(removeProduct.getAnnotation(RequestMapping.class).value())
        .containsExactly("/cart-microservice/shoppingCart/removeProduct");
    assertThat(removeProduct.getAnnotation(RequestMapping.class).method()).isEmpty();
    assertThat(removeProduct.getParameters()[0].getAnnotation(RequestParam.class).value())
        .isEqualTo("userid");
    assertThat(removeProduct.getParameters()[1].getAnnotation(RequestParam.class).value())
        .isEqualTo("asin");

    Method clearCart = ShoppingCartRestClient.class.getMethod("clearCart", String.class);
    assertThat(clearCart.getAnnotation(RequestMapping.class).value())
        .containsExactly("/cart-microservice/shoppingCart/clearCart");
    assertThat(clearCart.getAnnotation(RequestMapping.class).method()).isEmpty();
    assertThat(clearCart.getParameters()[0].getAnnotation(RequestParam.class).value())
        .isEqualTo("userid");
  }
}
