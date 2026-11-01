package com.shastage.store.web;

import com.shastage.store.domain.command.*;
import com.shastage.store.domain.model.*;
import com.shastage.store.domain.service.*;
import com.shastage.store.exception.BadRequestException;
import com.shastage.store.exception.ResourceNotFoundException;
import com.shastage.store.web.controller.*;
import com.shastage.store.web.mapper.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** MVC slice uses real request validation, JSON serialization and generated MapStruct mappers. */
@WebMvcTest({ProductController.class, ClientController.class, SupplierController.class, ImageController.class})
@Import({ProductWebMapperImpl.class, ClientWebMapperImpl.class, SupplierWebMapperImpl.class,
        ImageWebMapperImpl.class, AddressWebMapperImpl.class})
class ApiTests {
    @Autowired MockMvc mvc;
    @MockitoBean ProductService products;
    @MockitoBean ClientService clients;
    @MockitoBean SupplierService suppliers;
    @MockitoBean ImageService images;

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ADDRESS_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID SUPPLIER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID IMAGE_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");

    private Product product() {
        return new Product(ID, "Kettle", "Home", new BigDecimal("125.50"), 10,
                Instant.parse("2026-09-01T00:00:00Z"), SUPPLIER_ID, IMAGE_ID);
    }

    private Client client() {
        return new Client(ID, "Ivan", "Petrov", LocalDate.of(1990, 1, 1),
                Gender.MALE, LocalDate.of(2026, 1, 1), ADDRESS_ID);
    }

    private Supplier supplier() {
        return new Supplier(ID, "Supplier", ADDRESS_ID, "+70000000000");
    }

    @Test
    void productCreateMapsRequestAndReturnsCreatedResource() throws Exception {
        when(products.create(any())).thenReturn(product());
        mvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Kettle","category":"Home","price":125.50,"availableStock":10,
                 "supplierId":"%s","imageId":"%s"}
                """.formatted(SUPPLIER_ID, IMAGE_ID)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.price").value(125.5))
                .andExpect(jsonPath("$.availableStock").value(10))
                .andExpect(jsonPath("$.supplierId").value(SUPPLIER_ID.toString()))
                .andExpect(jsonPath("$.imageId").value(IMAGE_ID.toString()));
        verify(products).create(new ProductCreateCommand("Kettle", "Home", new BigDecimal("125.50"),
                10, SUPPLIER_ID, IMAGE_ID));
    }

    @Test
    void productReadAndListSerializeFields() throws Exception {
        when(products.findById(ID)).thenReturn(product());
        when(products.findAll()).thenReturn(List.of(product()));
        mvc.perform(get("/api/v1/products/{id}", ID)).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Kettle"))
                .andExpect(jsonPath("$.category").value("Home"))
                .andExpect(jsonPath("$.lastUpdateDate").value("2026-09-01T00:00:00Z"));
        mvc.perform(get("/api/v1/products")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ID.toString()));
    }

    @Test
    void stockDecreaseAndProductDeletionReturnText() throws Exception {
        mvc.perform(patch("/api/v1/products/{id}/stock", ID).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decreaseBy\":3}"))
                .andExpect(status().isOk()).andExpect(content().string("Product has been changed."));
        verify(products).decreaseStock(ID, 3);
        mvc.perform(delete("/api/v1/products/{id}", ID)).andExpect(status().isOk())
                .andExpect(content().string("Product deleted."));
        verify(products).deleteById(ID);
    }

    @Test
    void clientCreateMapsDatesGenderAndAddress() throws Exception {
        when(clients.create(any())).thenReturn(client());
        mvc.perform(post("/api/v1/clients").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Ivan","surname":"Petrov","birthday":"1990-01-01","gender":"MALE",
                 "registrationDate":"2026-01-01","addressId":"%s"}
                """.formatted(ADDRESS_ID)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.birthday").value("1990-01-01"))
                .andExpect(jsonPath("$.gender").value("MALE"))
                .andExpect(jsonPath("$.registrationDate").value("2026-01-01"))
                .andExpect(jsonPath("$.addressId").value(ADDRESS_ID.toString()));
        verify(clients).create(new ClientCreateCommand("Ivan", "Petrov", LocalDate.of(1990, 1, 1),
                Gender.MALE, LocalDate.of(2026, 1, 1), ADDRESS_ID));
    }

    @Test
    void clientsSupportReadPaginationAndExactSearch() throws Exception {
        when(clients.findById(ID)).thenReturn(client());
        when(clients.findAll(5, 12)).thenReturn(List.of(client()));
        when(clients.findByNameAndSurname("Ivan", "Petrov")).thenReturn(List.of(client()));
        mvc.perform(get("/api/v1/clients/{id}", ID)).andExpect(status().isOk())
                .andExpect(jsonPath("$.surname").value("Petrov"));
        mvc.perform(get("/api/v1/clients").param("limit", "5").param("offset", "12"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(ID.toString()));
        verify(clients).findAll(5, 12);
        mvc.perform(get("/api/v1/clients/search").param("name", "Ivan").param("surname", "Petrov"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Ivan"));
        verify(clients).findByNameAndSurname("Ivan", "Petrov");
    }

    @Test
    void clientDeletionReturnsNoContent() throws Exception {
        mvc.perform(delete("/api/v1/clients/{id}", ID)).andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(clients).deleteById(ID);
    }

    @Test
    void suppliersSupportCreateReadListAndDelete() throws Exception {
        mvc.perform(post("/api/v1/suppliers").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Supplier","addressId":"%s","phoneNumber":"+70000000000"}
                """.formatted(ADDRESS_ID)))
                .andExpect(status().isCreated()).andExpect(content().string("Supplier is created."));
        verify(suppliers).create(new SupplierCreateCommand("Supplier", ADDRESS_ID, "+70000000000"));
        when(suppliers.findById(ID)).thenReturn(supplier());
        when(suppliers.findAll()).thenReturn(List.of(supplier()));
        mvc.perform(get("/api/v1/suppliers/{id}", ID)).andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value("+70000000000"))
                .andExpect(jsonPath("$.addressId").value(ADDRESS_ID.toString()));
        mvc.perform(get("/api/v1/suppliers")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Supplier"));
        mvc.perform(delete("/api/v1/suppliers/{id}", ID)).andExpect(status().isOk());
        verify(suppliers).deleteById(ID);
    }

    @ParameterizedTest
    @ValueSource(strings = {"clients", "suppliers"})
    void addressPatchMapsAllFields(String resource) throws Exception {
        mvc.perform(patch("/api/v1/{resource}/{id}/address", resource, ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"country\":\"France\",\"city\":\"Paris\",\"street\":\"New street\"}"))
                .andExpect(status().isOk()).andExpect(content().string("Address updated!"));
        AddressPutCommand command = new AddressPutCommand("France", "Paris", "New street");
        if (resource.equals("clients")) verify(clients).changeAddress(ID, command);
        else verify(suppliers).changeAddress(ID, command);
    }

    @Test
    void imageCreateDecodesBase64AndReturnsBase64() throws Exception {
        when(images.create(any())).thenReturn(new Image(IMAGE_ID, new byte[]{1, 2, 3}));
        mvc.perform(post("/api/v1/images").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"image\":\"AQID\",\"productId\":\"" + ID + "\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(IMAGE_ID.toString()))
                .andExpect(jsonPath("$.image").value("AQID"));
        verify(images).create(argThat(command -> ID.equals(command.productId())
                && java.util.Arrays.equals(new byte[]{1, 2, 3}, command.image())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/v1/images/{id}", "/api/v1/images/product/{id}"})
    void imageDownloadsReturnExactBytesAndAttachmentHeader(String path) throws Exception {
        Image image = new Image(IMAGE_ID, new byte[]{0, 1, 2, (byte) 255});
        if (path.contains("/product/")) when(images.findByProductId(ID)).thenReturn(image);
        else when(images.findById(ID)).thenReturn(image);
        mvc.perform(get(path, ID)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"image-" + IMAGE_ID + "\""))
                .andExpect(content().bytes(image.getImage()));
    }

    @Test
    void imagesSupportReplacementAndDeletion() throws Exception {
        mvc.perform(put("/api/v1/images/{id}", ID).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"image\":\"BAUG\"}"))
                .andExpect(status().isOk()).andExpect(content().string("Image updated!"));
        verify(images).update(eq(ID), aryEq(new byte[]{4, 5, 6}));
        mvc.perform(delete("/api/v1/images/{id}", ID)).andExpect(status().isOk())
                .andExpect(content().string("Image deleted."));
        verify(images).deleteById(ID);
    }

    @ParameterizedTest
    @ValueSource(strings = {"products", "clients", "suppliers", "images"})
    void unknownResourceReturnsProblemDetail(String resource) throws Exception {
        ResourceNotFoundException error = new ResourceNotFoundException("Resource is missing");
        switch (resource) {
            case "products" -> when(products.findById(ID)).thenThrow(error);
            case "clients" -> when(clients.findById(ID)).thenThrow(error);
            case "suppliers" -> when(suppliers.findById(ID)).thenThrow(error);
            case "images" -> when(images.findById(ID)).thenThrow(error);
        }
        mvc.perform(get("/api/v1/{resource}/{id}", resource, ID)).andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Resource is missing"))
                .andExpect(jsonPath("$.instance").value("/api/v1/" + resource + "/" + ID));
    }

    @Test
    void insufficientStockReturnsBadRequestProblem() throws Exception {
        doThrow(new BadRequestException("There are not so many products.")).when(products).decreaseStock(ID, 11);
        mvc.perform(patch("/api/v1/products/{id}/stock", ID).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decreaseBy\":11}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("There are not so many products."));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"decreaseBy\":null}", "{\"decreaseBy\":0}", "{\"decreaseBy\":-1}"})
    void invalidStockRequestNeverReachesService(String body) throws Exception {
        mvc.perform(patch("/api/v1/products/{id}/stock", ID).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(products);
    }

    @ParameterizedTest
    @CsvSource({"POST,/api/v1/products", "POST,/api/v1/clients", "POST,/api/v1/suppliers",
            "POST,/api/v1/images", "PUT,/api/v1/images/{id}",
            "PATCH,/api/v1/clients/{id}/address", "PATCH,/api/v1/suppliers/{id}/address"})
    void missingRequiredFieldsNeverReachServices(String method, String path) throws Exception {
        mvc.perform(request(org.springframework.http.HttpMethod.valueOf(method), path, ID)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").isNotEmpty());
        verifyNoInteractions(products, clients, suppliers, images);
    }

    @ParameterizedTest
    @ValueSource(strings = {"products", "clients", "suppliers", "images"})
    void malformedUuidReturnsBadRequestWithoutCallingService(String resource) throws Exception {
        mvc.perform(get("/api/v1/{resource}/invalid-id", resource)).andExpect(status().isBadRequest());
        verifyNoInteractions(products, clients, suppliers, images);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "{\"image\":\"%%%\"}", "{\"image\":null}", "{\"image\":\"\"}"})
    void invalidImageReplacementNeverReachesService(String body) throws Exception {
        mvc.perform(put("/api/v1/images/{id}", ID).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(images);
    }

    @Test
    void missingSearchParameterReturnsBadRequest() throws Exception {
        mvc.perform(get("/api/v1/clients/search").param("name", "Ivan"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(clients);
    }

    @ParameterizedTest
    @CsvSource({"limit,-1", "limit,0", "offset,-1", "limit,abc", "offset,abc"})
    void invalidPaginationNeverReachesService(String parameter, String value) throws Exception {
        mvc.perform(get("/api/v1/clients").param(parameter, value))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(clients);
    }

    @Test
    void emptyListsAreJsonArrays() throws Exception {
        when(products.findAll()).thenReturn(List.of());
        when(suppliers.findAll()).thenReturn(List.of());
        when(clients.findAll(null, null)).thenReturn(List.of());
        for (String resource : List.of("products", "suppliers", "clients")) {
            mvc.perform(get("/api/v1/" + resource)).andExpect(status().isOk())
                    .andExpect(content().json("[]"));
        }
    }
}
