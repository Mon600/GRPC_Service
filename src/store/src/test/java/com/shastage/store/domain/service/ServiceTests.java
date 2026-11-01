package com.shastage.store.domain.service;

import com.shastage.store.domain.command.*;
import com.shastage.store.domain.model.*;
import com.shastage.store.domain.repository.*;
import com.shastage.store.exception.BadRequestException;
import com.shastage.store.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Pure domain tests: no Spring context or database is required. */
class ServiceTests {
    private final UUID id = UUID.randomUUID();
    private final UUID addressId = UUID.randomUUID();
    private final UUID supplierId = UUID.randomUUID();
    private final UUID imageId = UUID.randomUUID();
    private ProductRepository products;
    private ClientRepository clients;
    private SupplierRepository suppliers;
    private ImageRepository images;
    private AddressRepository addresses;
    private ProductService productService;
    private ClientService clientService;
    private SupplierService supplierService;
    private ImageService imageService;

    @BeforeEach
    void setUp() {
        products = mock(ProductRepository.class);
        clients = mock(ClientRepository.class);
        suppliers = mock(SupplierRepository.class);
        images = mock(ImageRepository.class);
        addresses = mock(AddressRepository.class);
        productService = new ProductService(products, suppliers, images);
        clientService = new ClientService(clients, addresses);
        supplierService = new SupplierService(suppliers, addresses);
        imageService = new ImageService(images, products);
    }

    private Product product() {
        return new Product(id, "Kettle", "Home", new BigDecimal("125.50"), 10,
                Instant.parse("2026-09-01T00:00:00Z"), supplierId, imageId);
    }

    private Client client() {
        return new Client(id, "Ivan", "Petrov", LocalDate.of(1990, 1, 1),
                Gender.MALE, LocalDate.of(2026, 1, 1), addressId);
    }

    private Supplier supplier() {
        return new Supplier(supplierId, "Supplier", addressId, "+70000000000");
    }

    private Address address() {
        return new Address(addressId, "Russia", "Kazan", "Old street");
    }

    private ProductCreateCommand productCommand() {
        return new ProductCreateCommand("Kettle", "Home", new BigDecimal("125.50"), 10, supplierId, imageId);
    }

    @Test
    void productCreationPreservesFieldsAndSetsIdAndPurchaseDate() {
        when(suppliers.findById(supplierId)).thenReturn(Optional.of(supplier()));
        when(images.findById(imageId)).thenReturn(Optional.of(new Image(imageId, new byte[]{1})));
        Instant before = Instant.now();
        Product created = productService.create(productCommand());
        assertAll(
                () -> assertNotNull(created.getId()),
                () -> assertEquals("Kettle", created.getName()),
                () -> assertEquals("Home", created.getCategory()),
                () -> assertEquals(new BigDecimal("125.50"), created.getPrice()),
                () -> assertEquals(10, created.getAvailableStock()),
                () -> assertEquals(supplierId, created.getSupplierId()),
                () -> assertEquals(imageId, created.getImageId()),
                () -> assertFalse(created.getLastUpdateDate().isBefore(before)),
                () -> assertFalse(created.getLastUpdateDate().isAfter(Instant.now())));
        verify(products).save(created);
    }

    @Test
    void productCreationRejectsMissingSupplierWithoutSaving() {
        assertThrows(BadRequestException.class, () -> productService.create(productCommand()));
        verifyNoInteractions(products, images);
    }

    @Test
    void productCreationRejectsMissingImageWithoutSaving() {
        when(suppliers.findById(supplierId)).thenReturn(Optional.of(supplier()));
        assertThrows(BadRequestException.class, () -> productService.create(productCommand()));
        verifyNoInteractions(products);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 10})
    void stockCanBeDecreasedIncludingToZero(int amount) {
        Product product = product();
        Instant purchaseDate = product.getLastUpdateDate();
        when(products.findById(id)).thenReturn(Optional.of(product));
        productService.decreaseStock(id, amount);
        assertEquals(10 - amount, product.getAvailableStock());
        assertEquals(purchaseDate, product.getLastUpdateDate());
        verify(products).save(product);
    }

    @Test
    void excessiveStockDecreaseDoesNotMutateOrSaveProduct() {
        Product product = product();
        when(products.findById(id)).thenReturn(Optional.of(product));
        assertThrows(BadRequestException.class, () -> productService.decreaseStock(id, 11));
        assertEquals(10, product.getAvailableStock());
        verify(products, never()).save(any());
    }

    @Test
    void stockDecreaseRejectsMissingProduct() {
        assertThrows(ResourceNotFoundException.class, () -> productService.decreaseStock(id, 1));
        verify(products, never()).save(any());
    }

    @Test
    void clientCreationPreservesAllFields() {
        when(addresses.existsById(addressId)).thenReturn(true);
        Client expected = client();
        Client created = clientService.create(new ClientCreateCommand(expected.getName(), expected.getSurname(),
                expected.getBirthday(), expected.getGender(), expected.getRegistrationDate(), addressId));
        assertAll(
                () -> assertNotNull(created.getId()),
                () -> assertEquals(expected.getName(), created.getName()),
                () -> assertEquals(expected.getSurname(), created.getSurname()),
                () -> assertEquals(expected.getBirthday(), created.getBirthday()),
                () -> assertEquals(expected.getGender(), created.getGender()),
                () -> assertEquals(expected.getRegistrationDate(), created.getRegistrationDate()),
                () -> assertEquals(addressId, created.getAddressId()));
        verify(clients).save(created);
    }

    @Test
    void clientCreationRejectsMissingAddress() {
        assertThrows(BadRequestException.class, () -> clientService.create(new ClientCreateCommand(
                "Ivan", "Petrov", LocalDate.of(1990, 1, 1), Gender.MALE, LocalDate.of(2026, 1, 1), addressId)));
        verifyNoInteractions(clients);
    }

    @Test
    void supplierCreationPreservesFields() {
        when(addresses.findById(addressId)).thenReturn(Optional.of(address()));
        Supplier created = supplierService.create(new SupplierCreateCommand("Supplier", addressId, "+70000000000"));
        assertAll(
                () -> assertNotNull(created.getId()),
                () -> assertEquals("Supplier", created.getName()),
                () -> assertEquals(addressId, created.getAddressId()),
                () -> assertEquals("+70000000000", created.getPhoneNumber()));
        verify(suppliers).save(created);
    }

    @Test
    void supplierCreationRejectsMissingAddress() {
        assertThrows(ResourceNotFoundException.class, () -> supplierService.create(
                new SupplierCreateCommand("Supplier", addressId, "+70000000000")));
        verifyNoInteractions(suppliers);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void addressReplacementPreservesAddressIdentity(boolean forClient) {
        if (forClient) when(clients.findById(id)).thenReturn(Optional.of(client()));
        else when(suppliers.findById(id)).thenReturn(Optional.of(supplier()));
        when(addresses.findById(addressId)).thenReturn(Optional.of(address()));
        AddressPutCommand command = new AddressPutCommand("France", "Paris", "New street");
        if (forClient) clientService.changeAddress(id, command);
        else supplierService.changeAddress(id, command);
        verify(addresses).save(argThat(saved -> addressId.equals(saved.getId())
                && "France".equals(saved.getCountry()) && "Paris".equals(saved.getCity())
                && "New street".equals(saved.getStreet())));
        verify(clients, never()).save(any());
        verify(suppliers, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void addressReplacementRejectsMissingOwner(boolean forClient) {
        assertThrows(ResourceNotFoundException.class, () -> {
            if (forClient) clientService.changeAddress(id, new AddressPutCommand("F", "P", "S"));
            else supplierService.changeAddress(id, new AddressPutCommand("F", "P", "S"));
        });
        verifyNoInteractions(addresses);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void addressReplacementRejectsMissingAddress(boolean forClient) {
        if (forClient) when(clients.findById(id)).thenReturn(Optional.of(client()));
        else when(suppliers.findById(id)).thenReturn(Optional.of(supplier()));
        assertThrows(ResourceNotFoundException.class, () -> {
            if (forClient) clientService.changeAddress(id, new AddressPutCommand("F", "P", "S"));
            else supplierService.changeAddress(id, new AddressPutCommand("F", "P", "S"));
        });
        verify(addresses, never()).save(any());
    }

    @Test
    void imageCreationLinksSavedImageToProduct() {
        Product product = product();
        when(products.findById(id)).thenReturn(Optional.of(product));
        byte[] bytes = {1, 2, 3};
        Image created = imageService.create(new ImageCreateCommand(bytes, id));
        assertNotNull(created.getId());
        assertArrayEquals(bytes, created.getImage());
        assertEquals(created.getId(), product.getImageId());
        var order = inOrder(images, products);
        order.verify(images).save(created);
        order.verify(products).save(product);
    }

    @Test
    void imageCreationRejectsMissingProductWithoutSaving() {
        assertThrows(ResourceNotFoundException.class, () -> imageService.create(new ImageCreateCommand(new byte[]{1}, id)));
        verifyNoInteractions(images);
        verify(products, never()).save(any());
    }

    @Test
    void imageUpdateReplacesBytesAndPreservesId() {
        Image image = new Image(imageId, new byte[]{1});
        when(images.findById(imageId)).thenReturn(Optional.of(image));
        imageService.update(imageId, new byte[]{4, 5, 6});
        assertEquals(imageId, image.getId());
        assertArrayEquals(new byte[]{4, 5, 6}, image.getImage());
        verify(images).save(image);
    }

    @Test
    void imageUpdateRejectsMissingImageWithoutSaving() {
        assertThrows(ResourceNotFoundException.class, () -> imageService.update(imageId, new byte[]{1}));
        verify(images, never()).save(any());
    }

    @Test
    void imageCanBeFoundThroughProduct() {
        Image image = new Image(imageId, new byte[]{1});
        when(products.findById(id)).thenReturn(Optional.of(product()));
        when(images.findById(imageId)).thenReturn(Optional.of(image));
        assertSame(image, imageService.findByProductId(id));
    }

    @Test
    void imageLookupRejectsMissingProduct() {
        assertThrows(ResourceNotFoundException.class, () -> imageService.findByProductId(id));
        verifyNoInteractions(images);
    }

    @Test
    void imageLookupRejectsProductWithoutImage() {
        Product product = product();
        product.setImageId(null);
        when(products.findById(id)).thenReturn(Optional.of(product));
        assertThrows(ResourceNotFoundException.class, () -> imageService.findByProductId(id));
        verifyNoInteractions(images);
    }

    @Test
    void imageLookupRejectsDanglingImageReference() {
        when(products.findById(id)).thenReturn(Optional.of(product()));
        assertThrows(ResourceNotFoundException.class, () -> imageService.findByProductId(id));
    }

    @ParameterizedTest
    @ValueSource(strings = {"product", "client", "supplier", "image"})
    void missingEntitiesCannotBeReadOrDeleted(String resource) {
        switch (resource) {
            case "product" -> {
                assertThrows(ResourceNotFoundException.class, () -> productService.findById(id));
                assertThrows(ResourceNotFoundException.class, () -> productService.deleteById(id));
                verify(products, never()).deleteById(any());
            }
            case "client" -> {
                assertThrows(ResourceNotFoundException.class, () -> clientService.findById(id));
                assertThrows(ResourceNotFoundException.class, () -> clientService.deleteById(id));
                verify(clients, never()).deleteById(any());
            }
            case "supplier" -> {
                assertThrows(ResourceNotFoundException.class, () -> supplierService.findById(id));
                assertThrows(ResourceNotFoundException.class, () -> supplierService.deleteById(id));
                verify(suppliers, never()).deleteById(any());
            }
            case "image" -> {
                assertThrows(ResourceNotFoundException.class, () -> imageService.findById(id));
                assertThrows(ResourceNotFoundException.class, () -> imageService.deleteById(id));
                verify(images, never()).deleteById(any());
            }
            default -> fail("Unknown resource: " + resource);
        }
    }

    @Test
    void existingEntitiesCanBeReadAndDeleted() {
        Product product = product(); Client client = client(); Supplier supplier = supplier();
        Image image = new Image(id, new byte[]{1});
        when(products.findById(id)).thenReturn(Optional.of(product));
        when(clients.findById(id)).thenReturn(Optional.of(client));
        when(suppliers.findById(id)).thenReturn(Optional.of(supplier));
        when(images.findById(id)).thenReturn(Optional.of(image));
        assertSame(product, productService.findById(id));
        assertSame(client, clientService.findById(id));
        assertSame(supplier, supplierService.findById(id));
        assertSame(image, imageService.findById(id));
        productService.deleteById(id); clientService.deleteById(id);
        supplierService.deleteById(id); imageService.deleteById(id);
        verify(products).deleteById(id); verify(clients).deleteById(id);
        verify(suppliers).deleteById(id); verify(images).deleteById(id);
    }

    @Test
    void listsAndClientSearchPreserveResultsAndPaginationArguments() {
        List<Product> productList = List.of(product());
        List<Client> clientList = List.of(client());
        List<Supplier> supplierList = List.of(supplier());
        when(products.findAll()).thenReturn(productList);
        when(suppliers.findAll()).thenReturn(supplierList);
        when(clients.findAll(5, 12)).thenReturn(clientList);
        when(clients.findByNameAndSurname("Ivan", "Petrov")).thenReturn(clientList);
        assertSame(productList, productService.findAll());
        assertSame(supplierList, supplierService.findAll());
        assertSame(clientList, clientService.findAll(5, 12));
        assertSame(clientList, clientService.findByNameAndSurname("Ivan", "Petrov"));
        verify(clients).findAll(5, 12);
        verify(clients).findByNameAndSurname("Ivan", "Petrov");
    }
}
