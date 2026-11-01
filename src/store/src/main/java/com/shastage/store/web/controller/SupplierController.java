package com.shastage.store.web.controller;

import com.shastage.store.web.security.Authorize;

import com.shastage.store.domain.model.Supplier;
import com.shastage.store.domain.service.SupplierService;
import com.shastage.store.web.dto.request.AddressCreateRequest;
import com.shastage.store.web.dto.request.SupplierCreateRequest;
import com.shastage.store.web.dto.response.SupplierDetailsResponse;
import com.shastage.store.web.mapper.AddressWebMapper;
import com.shastage.store.web.mapper.SupplierWebMapper;
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

@Tag(name = "Suppliers", description = "Создание, просмотр, изменение и удаление поставщиков")
@Authorize
@RestController
@RequestMapping("/api/v1/suppliers")
@AllArgsConstructor
public class SupplierController {

    SupplierService supplierService;
    SupplierWebMapper supplierWebMapper;
    AddressWebMapper addressWebMapper;

    @Operation(summary = "Получить поставщика по идентификатору")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Поставщик найден",
                    content = @Content(schema = @Schema(implementation = SupplierDetailsResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный формат идентификатора",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Поставщик не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<SupplierDetailsResponse> findById(
            @Parameter(description = "Идентификатор поставщика", example = "7d444840-9dc0-11d1-b245-5ffdce74fad2")
            @PathVariable UUID id
    ) {
        Supplier supplier = supplierService.findById(id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(supplierWebMapper.toSupplierDetailsResponse(supplier));
    }

    @Operation(summary = "Получить список поставщиков")
    @ApiResponse(
            responseCode = "200",
            description = "Список поставщиков",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = SupplierDetailsResponse.class)))
    )
    @GetMapping
    public ResponseEntity<List<SupplierDetailsResponse>> findAll() {
        List<Supplier> suppliers = supplierService.findAll();
        List<SupplierDetailsResponse> response = suppliers.stream()
                .map(supplierWebMapper::toSupplierDetailsResponse)
                .toList();

        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @Operation(summary = "Удалить поставщика")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Поставщик удалён"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный формат идентификатора",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Поставщик не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @DeleteMapping("/{id}")
    public void deleteById(
            @Parameter(description = "Идентификатор поставщика", example = "7d444840-9dc0-11d1-b245-5ffdce74fad2")
            @PathVariable UUID id
    ) {
        supplierService.deleteById(id);
    }

    @Operation(
            summary = "Создать поставщика",
            description = "Создаёт поставщика и связывает его с уже существующим адресом."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Поставщик создан",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Supplier is created.")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Тело запроса не прошло валидацию",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Адрес не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping
    public ResponseEntity<String> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные нового поставщика",
                    required = true
            )
            @Valid @RequestBody SupplierCreateRequest request
    ) {
        supplierService.create(supplierWebMapper.toSupplierCreateCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body("Supplier is created.");
    }

    @Operation(
            summary = "Изменить адрес поставщика",
            description = "Полностью заменяет страну, город и улицу текущего адреса поставщика."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Адрес изменён",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Address updated!")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Тело запроса не прошло валидацию или идентификатор имеет неверный формат",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Поставщик или его текущий адрес не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PatchMapping("/{id}/address")
    public ResponseEntity<String> changeAddress(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новое значение адреса",
                    required = true
            )
            @Valid @RequestBody AddressCreateRequest request,
            @Parameter(description = "Идентификатор поставщика", example = "7d444840-9dc0-11d1-b245-5ffdce74fad2")
            @PathVariable UUID id
    ) {
        supplierService.changeAddress(id, addressWebMapper.toAddressPutCommand(request));

        return ResponseEntity.status(HttpStatus.OK).body("Address updated!");
    }
}
