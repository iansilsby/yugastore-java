package com.yugabyte.app.yugastore.repo.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.yugabyte.app.yugastore.domain.ImageInfo;
import com.yugabyte.app.yugastore.domain.ProductMetadata;
import com.yugabyte.app.yugastore.repo.ProductMetadataRepo;

@ExtendWith(MockitoExtension.class)
class ProductMetadataRestRepoTest {

    @Mock
    ProductMetadataRepo repo;

    @InjectMocks
    ProductMetadataRestRepo controller;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/productmetadata/relatedproducts");
        request.setServerName("localhost");
        request.setServerPort(8080);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    private ProductMetadata product(String id, String imUrl) {
        ProductMetadata p = new ProductMetadata();
        p.setId(id);
        p.setImUrl(imUrl);
        return p;
    }

    private ProductMetadata rootProduct(List<String> alsoBought, List<String> boughtTogether) {
        ProductMetadata root = product("ROOT", "http://root");
        root.setAlso_bought(alsoBought);
        root.setBought_together(boughtTogether);
        root.setAlso_viewed(Arrays.asList("V1", "V2"));
        return root;
    }

    @SuppressWarnings("unchecked")
    private ImageInfo body(ResponseEntity<?> response) {
        return (ImageInfo) response.getBody();
    }

    @Test
    void returnsImageUrlsOfAlsoBoughtAndBoughtTogetherProducts() {
        when(repo.findById("ROOT")).thenReturn(Optional.of(rootProduct(Arrays.asList("B1", "B2"), Arrays.asList("T1"))));
        when(repo.findById("B1")).thenReturn(Optional.of(product("B1", "http://b1")));
        when(repo.findById("B2")).thenReturn(Optional.of(product("B2", "http://b2")));
        when(repo.findById("T1")).thenReturn(Optional.of(product("T1", "http://t1")));

        ResponseEntity<?> response = controller.getRelatedProducts("ROOT");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ImageInfo info = body(response);
        assertEquals(Arrays.asList("http://b1", "http://b2"), info.getAlsoBought());
        assertEquals(Arrays.asList("http://t1"), info.getBoughtTogether());
    }

    @Test
    void alsoViewedIsNeverPopulatedAndItsAsinsAreNeverLookedUp() {
        when(repo.findById("ROOT")).thenReturn(Optional.of(rootProduct(null, null)));

        ImageInfo info = body(controller.getRelatedProducts("ROOT"));

        assertNotNull(info.getAlsoViewed());
        assertTrue(info.getAlsoViewed().isEmpty());
        verify(repo, never()).findById("V1");
        verify(repo, never()).findById("V2");
    }

    @Test
    void nullOrEmptyRelatedListsYieldEmptyResultLists() {
        when(repo.findById("ROOT")).thenReturn(Optional.of(rootProduct(null, Collections.emptyList())));

        ImageInfo info = body(controller.getRelatedProducts("ROOT"));

        assertEquals(Collections.emptyList(), info.getAlsoBought());
        assertEquals(Collections.emptyList(), info.getBoughtTogether());
        verify(repo, times(1)).findById(anyString());
    }

    @Test
    void skipsRelatedAsinsThatAreMissingOrHaveNoImageUrl() {
        when(repo.findById("ROOT")).thenReturn(Optional.of(rootProduct(Arrays.asList("MISSING", "NOIMG", "EMPTYIMG", "OK"), null)));
        when(repo.findById("MISSING")).thenReturn(Optional.empty());
        when(repo.findById("NOIMG")).thenReturn(Optional.of(product("NOIMG", null)));
        when(repo.findById("EMPTYIMG")).thenReturn(Optional.of(product("EMPTYIMG", "")));
        when(repo.findById("OK")).thenReturn(Optional.of(product("OK", "http://ok")));

        ImageInfo info = body(controller.getRelatedProducts("ROOT"));

        assertEquals(Collections.singletonList("http://ok"), info.getAlsoBought());
    }

    @Test
    void capsResultAtElevenImagesAndStopsLookingUpFurtherAsins() {
        List<String> asins = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            asins.add("B" + i);
        }
        when(repo.findById("ROOT")).thenReturn(Optional.of(rootProduct(asins, null)));
        for (int i = 0; i < 15; i++) {
            lenient().when(repo.findById("B" + i)).thenReturn(Optional.of(product("B" + i, "http://b" + i)));
        }

        ImageInfo info = body(controller.getRelatedProducts("ROOT"));

        assertEquals(11, info.getAlsoBought().size());
        assertEquals("http://b0", info.getAlsoBought().get(0));
        assertEquals("http://b10", info.getAlsoBought().get(10));
        verify(repo).findById("B10");
        verify(repo, never()).findById("B11");
        verify(repo, never()).findById("B14");
    }

    @Test
    void asinsWithoutImagesDoNotCountTowardsTheCap() {
        List<String> asins = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            asins.add("N" + i);
        }
        asins.add("LAST");
        when(repo.findById("ROOT")).thenReturn(Optional.of(rootProduct(asins, null)));
        for (int i = 0; i < 12; i++) {
            when(repo.findById("N" + i)).thenReturn(Optional.empty());
        }
        when(repo.findById("LAST")).thenReturn(Optional.of(product("LAST", "http://last")));

        ImageInfo info = body(controller.getRelatedProducts("ROOT"));

        assertEquals(Collections.singletonList("http://last"), info.getAlsoBought());
    }

    @Test
    void repositoryFailureWhileResolvingRelatedProductsIsSwallowed() {
        when(repo.findById("ROOT")).thenReturn(Optional.of(rootProduct(Arrays.asList("BOOM"), Arrays.asList("T1"))));
        when(repo.findById("BOOM")).thenThrow(new RuntimeException("db down"));

        ResponseEntity<?> response = controller.getRelatedProducts("ROOT");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ImageInfo info = body(response);
        assertTrue(info.getAlsoBought().isEmpty());
        assertNull(info.getBoughtTogether());
        verify(repo, never()).findById("T1");
    }

    @Test
    void addsSelfLinkPointingBackToTheEndpoint() {
        when(repo.findById("ROOT")).thenReturn(Optional.of(rootProduct(null, null)));

        ImageInfo info = body(controller.getRelatedProducts("ROOT"));

        Optional<Link> self = info.getLink(IanaLinkRelations.SELF);
        assertTrue(self.isPresent());
        assertEquals("http://localhost:8080/productmetadata/relatedproducts?asin=ROOT", self.get().getHref());
    }

    @Test
    void unknownAsinThrowsNoSuchElementException() {
        when(repo.findById("missing")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> controller.getRelatedProducts("missing"));
    }

    @Test
    void endpointIsNotMappedByPlainSpringMvcOnlyBySpringDataRest() throws Exception {
        // @RepositoryRestController handlers are served by Spring Data REST's own handler mapping,
        // so a standalone MVC setup does not route to them.
        mockMvc.perform(get("/productmetadata/relatedproducts").param("asin", "ROOT"))
            .andExpect(status().isNotFound());
        verify(repo, never()).findById(anyString());
    }
}
