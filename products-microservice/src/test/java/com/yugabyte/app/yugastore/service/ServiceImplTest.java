package com.yugabyte.app.yugastore.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yugabyte.app.yugastore.domain.ProductInventory;
import com.yugabyte.app.yugastore.domain.ProductMetadata;
import com.yugabyte.app.yugastore.domain.ProductRanking;
import com.yugabyte.app.yugastore.repo.ProductInventoryRepository;
import com.yugabyte.app.yugastore.repo.ProductMetadataRepo;
import com.yugabyte.app.yugastore.repo.ProductRankingRepository;
import com.yugabyte.app.yugastore.service.impl.ProductInventoryServiceImpl;
import com.yugabyte.app.yugastore.service.impl.ProductRankingServiceImpl;
import com.yugabyte.app.yugastore.service.impl.ProductServiceImpl;

@ExtendWith(MockitoExtension.class)
class ServiceImplTest {

    @Nested
    @ExtendWith(MockitoExtension.class)
    class ProductServiceImplTest {

        @Mock
        ProductMetadataRepo repo;

        @InjectMocks
        ProductServiceImpl service;

        @Test
        void findByIdDelegatesToRepository() {
            ProductMetadata p = new ProductMetadata();
            p.setId("A1");
            when(repo.findById("A1")).thenReturn(Optional.of(p));

            Optional<ProductMetadata> result = service.findById("A1");

            assertTrue(result.isPresent());
            assertSame(p, result.get());
            verify(repo).findById("A1");
            verifyNoMoreInteractions(repo);
        }

        @Test
        void findByIdReturnsEmptyWhenRepositoryHasNoMatch() {
            when(repo.findById("missing")).thenReturn(Optional.empty());
            assertFalse(service.findById("missing").isPresent());
        }

        @Test
        void findAllProductsPageablePassesLimitAndOffsetInOrder() {
            List<ProductMetadata> products = Arrays.asList(new ProductMetadata(), new ProductMetadata());
            when(repo.getProducts(10, 20)).thenReturn(products);

            assertSame(products, service.findAllProductsPageable(10, 20));
            verify(repo).getProducts(10, 20);
        }

        @Test
        void findAllProductsPageableDoesNotValidateArguments() {
            when(repo.getProducts(-1, -5)).thenReturn(Collections.emptyList());
            assertEquals(Collections.emptyList(), service.findAllProductsPageable(-1, -5));
            verify(repo).getProducts(-1, -5);
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class ProductRankingServiceImplTest {

        @Mock
        ProductRankingRepository repo;

        @InjectMocks
        ProductRankingServiceImpl service;

        @Test
        void findProductRankingByIdDelegatesToRepository() {
            ProductRanking r = new ProductRanking();
            when(repo.findProductRankingById("A1")).thenReturn(Optional.of(r));

            assertSame(r, service.findProductRankingById("A1").get());
            verify(repo).findProductRankingById("A1");
        }

        @Test
        void findProductRankingByIdReturnsEmptyWhenMissing() {
            when(repo.findProductRankingById("x")).thenReturn(Optional.empty());
            assertFalse(service.findProductRankingById("x").isPresent());
        }

        @Test
        void getProductsByCategoryDelegatesWithSameArguments() {
            List<ProductRanking> rankings = Collections.singletonList(new ProductRanking());
            when(repo.getProductsByCategory("books", 5, 15)).thenReturn(rankings);

            assertSame(rankings, service.getProductsByCategory("books", 5, 15));
            verify(repo).getProductsByCategory("books", 5, 15);
            verifyNoMoreInteractions(repo);
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class ProductInventoryServiceImplTest {

        @Mock
        ProductInventoryRepository repo;

        @InjectMocks
        ProductInventoryServiceImpl service;

        @Test
        void findByIdDelegatesToRepository() {
            ProductInventory inv = new ProductInventory();
            inv.setId("A1");
            inv.setQuantity(3);
            when(repo.findById("A1")).thenReturn(Optional.of(inv));

            Optional<ProductInventory> result = service.findById("A1");

            assertSame(inv, result.get());
            assertEquals(3, result.get().getQuantity());
            verify(repo).findById("A1");
        }

        @Test
        void findByIdReturnsEmptyWhenMissing() {
            when(repo.findById("nope")).thenReturn(Optional.empty());
            assertFalse(service.findById("nope").isPresent());
        }
    }
}
