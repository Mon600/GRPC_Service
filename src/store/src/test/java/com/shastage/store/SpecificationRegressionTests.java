package com.shastage.store;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "server.port=0")
@Import(TestDatabaseConfiguration.class)
@AutoConfigureMockMvc
@Transactional
class SpecificationRegressionTests {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManager entityManager;

    private final UUID addressId = UUID.randomUUID();
    private final UUID supplierId = UUID.randomUUID();
    private final UUID imageId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final Instant purchaseDate = Instant.parse("2026-09-01T00:00:00Z");

    @BeforeEach
    void createFixtures() {
        jdbc.update("INSERT INTO addresses(id, country, city, street) VALUES (?, 'Russia', 'Kazan', 'Test street')", addressId);
        jdbc.update("INSERT INTO suppliers(id, name, address_id, phone_number) VALUES (?, 'Test supplier', ?, '+70000000000')", supplierId, addressId);
        jdbc.update("INSERT INTO images(id, image) VALUES (?, ?)", imageId, new byte[]{1, 2, 3});
        insertProduct(productId);
    }

    private void insertProduct(UUID id) {
        jdbc.update("""
                INSERT INTO products(id, name, category, price, available_stock, last_update_date, supplier_id, image_id)
                VALUES (?, 'Test kettle', 'Home appliances', 100.00, 10, ?::timestamptz, ?, ?)
                """, id, purchaseDate.toString(), supplierId, imageId);
    }

    private String productBody(String category, String price, String stock, UUID supplier, UUID image) {
        return """
                {"name":"Test kettle","category":%s,"price":%s,"availableStock":%s,"supplierId":"%s","imageId":"%s"}
                """.formatted(category, price, stock, supplier, image);
    }

    @Test
    void invalidPaginationReturns400WithProblemDetail() throws Exception {
        for (String limit : new String[]{"-1", "0"}) {
            mvc.perform(get("/api/v1/clients").param("limit", limit))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.detail").isNotEmpty())
                    .andExpect(jsonPath("$.instance").value("/api/v1/clients"));
        }
        mvc.perform(get("/api/v1/clients").param("offset", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").isNotEmpty())
                .andExpect(jsonPath("$.instance").value("/api/v1/clients"));
    }

    @Test
    void invalidProductFieldsReturn400() throws Exception {
        String[][] values = {
                {"null", "100", "1"}, {"\" \"", "100", "1"},
                {"\"Home appliances\"", "null", "1"},
                {"\"Home appliances\"", "100", "null"},
                {"\"Home appliances\"", "10000000000", "1"},
                {"\"Home appliances\"", "1.001", "1"},
                {"\"Home appliances\"", "-1", "1"},
                {"\"Home appliances\"", "100", "-1"}
        };
        for (String[] value : values) {
            mvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                            .content(productBody(value[0], value[1], value[2], supplierId, imageId)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").isNotEmpty());
        }
    }

    @Test
    void unknownProductReferencesReturn400() throws Exception {
        for (boolean unknownSupplier : new boolean[]{true, false}) {
            mvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                            .content(productBody("\"Home appliances\"", "100", "1",
                                    unknownSupplier ? UUID.randomUUID() : supplierId,
                                    unknownSupplier ? imageId : UUID.randomUUID())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").isNotEmpty());
        }
    }

    @Test
    void validProductCanBeCreated() throws Exception {
        mvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                        .content(productBody("\"Home appliances\"", "9999999999.99", "1", supplierId, imageId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void deletingSharedImagePreservesProductsAndUnlinksEveryReference() throws Exception {
        UUID secondProduct = UUID.randomUUID();
        insertProduct(secondProduct);
        mvc.perform(delete("/api/v1/images/{id}", imageId)).andExpect(status().isOk());
        entityManager.flush();
        entityManager.clear();
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM images WHERE id = ?", Integer.class, imageId));
        for (UUID id : new UUID[]{productId, secondProduct}) {
            assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM products WHERE id = ? AND image_id IS NULL", Integer.class, id));
            mvc.perform(get("/api/v1/images/product/{id}", id))
                    .andExpect(status().isNotFound()).andExpect(jsonPath("$.title").isNotEmpty());
        }
    }

    @Test
    void stockDecreasePreservesPurchaseDate() throws Exception {
        mvc.perform(patch("/api/v1/products/{id}/stock", productId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"decreaseBy\":3}"))
                .andExpect(status().isOk());
        entityManager.flush();
        assertEquals(7, jdbc.queryForObject("SELECT available_stock FROM products WHERE id = ?", Integer.class, productId));
        assertEquals(purchaseDate, jdbc.queryForObject("SELECT last_update_date FROM products WHERE id = ?",
                (rs, row) -> rs.getTimestamp(1).toInstant(), productId));
    }

    @Test
    void putReplacesImageAndValidatesRequest() throws Exception {
        mvc.perform(put("/api/v1/images/{id}", imageId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"image\":\"BAUG\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/images/{id}", imageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"image-" + imageId + "\""))
                .andExpect(content().bytes(new byte[]{4, 5, 6}));
        mvc.perform(put("/api/v1/images/{id}", imageId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"image\":\"\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/v1/images/{id}", UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"image\":\"BAUG\"}"))
                .andExpect(status().isNotFound());
    }
}
