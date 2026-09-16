package com.yugabyte.app.yugastore.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DomainObjectsTest {

    @Nested
    class ProductMetadataTest {

        private ProductMetadata withId(String id) {
            ProductMetadata p = new ProductMetadata();
            p.setId(id);
            return p;
        }

        @Test
        void gettersReturnWhatSettersStore() {
            ProductMetadata p = withId("A1");
            p.setBrand("brand");
            p.setCategories(new HashSet<>(Collections.singleton("books")));
            p.setImUrl("http://img");
            p.setPrice(9.99);
            p.setTitle("title");
            p.setDescription("desc");
            p.setAlso_bought(Arrays.asList("B1"));
            p.setAlso_viewed(Arrays.asList("C1"));
            p.setBought_together(Arrays.asList("D1"));
            p.setBuy_after_viewing(Arrays.asList("E1"));
            p.setNum_reviews(3);
            p.setNum_stars(12.0);
            p.setAvg_stars(4.0);

            assertEquals("A1", p.getId());
            assertEquals("brand", p.getBrand());
            assertEquals(Collections.singleton("books"), p.getCategories());
            assertEquals("http://img", p.getImUrl());
            assertEquals(9.99, p.getPrice());
            assertEquals("title", p.getTitle());
            assertEquals("desc", p.getDescription());
            assertEquals(Arrays.asList("B1"), p.getAlso_bought());
            assertEquals(Arrays.asList("C1"), p.getAlso_viewed());
            assertEquals(Arrays.asList("D1"), p.getBought_together());
            assertEquals(Arrays.asList("E1"), p.getBuy_after_viewing());
            assertEquals(3, p.getNum_reviews());
            assertEquals(12.0, p.getNum_stars());
            assertEquals(4.0, p.getAvg_stars());
        }

        @Test
        void equalityIsBasedOnIdOnly() {
            ProductMetadata a = withId("A1");
            a.setTitle("one");
            ProductMetadata b = withId("A1");
            b.setTitle("two");
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
            assertNotEquals(a, withId("A2"));
            assertNotEquals(a, null);
            assertNotEquals(a, "A1");
            assertEquals(a, a);
        }

        @Test
        void equalsAndHashCodeThrowWhenIdIsNull() {
            ProductMetadata noId = new ProductMetadata();
            assertThrows(NullPointerException.class, () -> noId.equals(withId("A1")));
            assertThrows(NullPointerException.class, noId::hashCode);
        }
    }

    @Nested
    class ProductInventoryTest {

        @Test
        void equalityIsBasedOnIdOnly() {
            ProductInventory a = new ProductInventory();
            a.setId("A1");
            a.setQuantity(1);
            ProductInventory b = new ProductInventory();
            b.setId("A1");
            b.setQuantity(99);
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
            assertEquals(1, a.getQuantity());
            assertEquals("A1", a.getId());

            ProductInventory c = new ProductInventory();
            c.setId("A2");
            assertNotEquals(a, c);
            assertNotEquals(a, null);
        }

        @Test
        void hashCodeThrowsWhenIdIsNull() {
            assertThrows(NullPointerException.class, () -> new ProductInventory().hashCode());
        }
    }

    @Nested
    class OrderTest {

        @Test
        void gettersSettersAndIdEquality() {
            Order o = new Order();
            o.setId("o1");
            o.setUser_id(7);
            o.setOrder_details("details");
            o.setOrder_time("now");
            o.setOrder_total(12.5);
            assertEquals("o1", o.getId());
            assertEquals(7, o.getUser_id());
            assertEquals("details", o.getOrder_details());
            assertEquals("now", o.getOrder_time());
            assertEquals(12.5, o.getOrder_total());

            Order same = new Order();
            same.setId("o1");
            assertEquals(o, same);
            assertEquals(o.hashCode(), same.hashCode());
            Order other = new Order();
            other.setId("o2");
            assertNotEquals(o, other);
        }
    }

    @Nested
    class CheckoutStatusTest {

        @Test
        void constantsAndAccessors() {
            assertEquals("SUCCESS", CheckoutStatus.SUCCESS);
            assertEquals("FAILURE", CheckoutStatus.FAILURE);
            CheckoutStatus s = new CheckoutStatus();
            s.setStatus(CheckoutStatus.SUCCESS);
            s.setOrderNumber("42");
            s.setOrderDetails("d");
            assertEquals("SUCCESS", s.getStatus());
            assertEquals("42", s.getOrderNumber());
            assertEquals("d", s.getOrderDetails());
        }
    }

    @Nested
    class ImageInfoTest {

        @Test
        void constructorInitialisesAlsoBoughtAndAlsoViewedButNotBoughtTogether() {
            ImageInfo info = new ImageInfo();
            assertNotNull(info.getAlsoBought());
            assertTrue(info.getAlsoBought().isEmpty());
            assertNotNull(info.getAlsoViewed());
            assertTrue(info.getAlsoViewed().isEmpty());
            assertNull(info.getBoughtTogether());
        }

        @Test
        void settersReplaceLists() {
            ImageInfo info = new ImageInfo();
            info.setAlsoBought(Arrays.asList("a"));
            info.setAlsoViewed(Arrays.asList("b"));
            info.setBoughtTogether(Arrays.asList("c"));
            assertEquals(Arrays.asList("a"), info.getAlsoBought());
            assertEquals(Arrays.asList("b"), info.getAlsoViewed());
            assertEquals(Arrays.asList("c"), info.getBoughtTogether());
        }
    }

    @Nested
    class ProductRankingTest {

        @Test
        void gettersReturnWhatSettersStore() {
            ProductRankingKey key = new ProductRankingKey();
            key.setId("A1");
            key.setCategory("books");

            ProductRanking r = new ProductRanking();
            r.setId(key);
            r.setSalesRank(5);
            r.setTitle("t");
            r.setPrice(1.5);
            r.setImUrl("u");
            r.setNum_reviews(2);
            r.setNum_stars(8.0);
            r.setAvg_stars(4.0);

            assertEquals(key, r.getId());
            assertEquals("A1", r.getId().getAsin());
            assertEquals("books", r.getId().getCategory());
            assertEquals(5, r.getSalesRank());
            assertEquals("t", r.getTitle());
            assertEquals(1.5, r.getPrice());
            assertEquals("u", r.getImUrl());
            assertEquals(2, r.getNum_reviews());
            assertEquals(8.0, r.getNum_stars());
            assertEquals(4.0, r.getAvg_stars());
        }
    }

    @Nested
    class ProductRankingKeyTest {

        private ProductRankingKey key(String asin, String category) {
            ProductRankingKey k = new ProductRankingKey();
            k.setId(asin);
            k.setCategory(category);
            return k;
        }

        @Test
        void setIdPopulatesAsin() {
            assertEquals("A1", key("A1", null).getAsin());
        }

        @Test
        void anyTwoKeysWithNonNullAsinAreEqualRegardlessOfValues() {
            assertEquals(key("A1", "books"), key("A1", "toys"));
            assertEquals(key("A1", "books"), key("ZZZ", "books"));
        }

        @Test
        void keyWithNullAsinIsNotEqualToKeyWithAsin() {
            assertNotEquals(key(null, "books"), key("A1", "books"));
        }

        @Test
        void keyWithNonNullAsinIsEqualToKeyWithNullAsin() {
            assertEquals(key("A1", "books"), key(null, "books"));
        }

        @Test
        void comparingTwoKeysWithNullAsinThrows() {
            assertThrows(NullPointerException.class, () -> key(null, "a").equals(key(null, "b")));
        }

        @Test
        void equalsHandlesSameInstanceNullAndOtherTypes() {
            ProductRankingKey k = key("A1", "books");
            assertTrue(k.equals(k));
            assertFalse(k.equals(null));
            assertFalse(k.equals("A1"));
        }

        @Test
        void hashCodeIgnoresCategoryAndCountsAsinTwice() {
            assertEquals(key("A1", "books").hashCode(), key("A1", "toys").hashCode());
            int h = "A1".hashCode();
            assertEquals(31 * (31 * 1 + h) + h, key("A1", "x").hashCode());
            assertEquals(31 * 31, key(null, "x").hashCode());
        }
    }
}
