package com.shastage.store.web.controller;

import com.shastage.store.web.security.Authorize;

import com.shastage.store.domain.model.Product;
import com.shastage.store.domain.service.ProductService;
import com.shastage.store.web.dto.request.ProductCreateRequest;
import com.shastage.store.web.dto.request.ProductStockDecreaseRequest;
import com.shastage.store.web.dto.response.ProductDetailsResponse;
import com.shastage.store.web.mapper.ProductWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Products", description = "Создание, просмотр, изменение остатков и удаление товаров")
@Authorize
@RestController
@RequestMapping("/api/v1/products")
@AllArgsConstructor
public class ProductController {

    ProductService productService;
    ProductWebMapper productWebMapper;

    @Operation(summary = "Получить товар по идентификатору")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Товар найден",
                    content = @Content(schema = @Schema(implementation = ProductDetailsResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный формат идентификатора",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Товар не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailsResponse> findById(
            @Parameter(description = "Идентификатор товара", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id
    ) {
        Product product = productService.findById(id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(productWebMapper.toProductDetailsResponse(product));
    }

    @Operation(summary = "Получить список товаров")
    @ApiResponse(
            responseCode = "200",
            description = "Список товаров",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductDetailsResponse.class)))
    )
    @GetMapping
    public ResponseEntity<List<ProductDetailsResponse>> findAll() {
        List<Product> products = productService.findAll();

        List<ProductDetailsResponse> productDetails = products.stream()
                .map(productWebMapper::toProductDetailsResponse)
                .toList();

        return ResponseEntity.status(HttpStatus.OK)
                .body(productDetails);
    }

    @Operation(
            summary = "Уменьшить остаток товара",
            description = "Вычитает указанное количество из текущего остатка товара."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Остаток изменён",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Product has been changed.")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректное количество или запрошено больше доступного остатка",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Товар не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PatchMapping("/{id}/stock")
    public ResponseEntity<String> decreaseStock(
            @Parameter(description = "Идентификатор товара", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Количество товара для списания",
                    required = true
            )
            @Valid @RequestBody ProductStockDecreaseRequest request
    ) {

        productService.decreaseStock(id, request.decreaseBy());

        return ResponseEntity.status(HttpStatus.OK).body("Product has been changed.");
    }

    @Operation(summary = "Создать товар")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Товар создан",
                    content = @Content(schema = @Schema(implementation = ProductDetailsResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Тело запроса не прошло валидацию",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping
    public ResponseEntity<ProductDetailsResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные нового товара",
                    required = true
            )
            @Valid @RequestBody ProductCreateRequest request
    ) {
        Product product = productService.create(productWebMapper.toProductCreateCommand(request));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productWebMapper.toProductDetailsResponse(product));
    }

    @Operation(summary = "Удалить товар")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Товар удалён",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Product deleted.")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный формат идентификатора",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Товар не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(
            @Parameter(description = "Идентификатор товара", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id
    ) {
        productService.deleteById(id);

        return ResponseEntity.status(HttpStatus.OK)
                .body("Product deleted.");
    }
}
