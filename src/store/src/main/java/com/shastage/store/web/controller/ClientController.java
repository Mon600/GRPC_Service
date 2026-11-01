package com.shastage.store.web.controller;

import com.shastage.store.web.security.Authorize;

import com.shastage.store.domain.command.AddressPutCommand;
import com.shastage.store.domain.model.Client;
import com.shastage.store.domain.service.ClientService;
import com.shastage.store.web.dto.request.AddressCreateRequest;
import com.shastage.store.web.dto.request.ClientCreateRequest;
import com.shastage.store.web.dto.response.ClientDetailsResponse;
import com.shastage.store.web.mapper.AddressWebMapper;
import com.shastage.store.web.mapper.ClientWebMapper;
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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Clients", description = "Создание, поиск, изменение и удаление клиентов")
@Authorize
@RestController
@RequestMapping("/api/v1/clients")
@AllArgsConstructor
public class ClientController {

    ClientWebMapper clientWebMapper;
    AddressWebMapper addressWebMapper;
    ClientService clientService;

    @Operation(
            summary = "Создать клиента",
            description = "Создаёт клиента и связывает его с уже существующим адресом."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Клиент создан",
                    content = @Content(schema = @Schema(implementation = ClientDetailsResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Тело запроса не прошло валидацию или адрес не существует",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping
    public ResponseEntity<ClientDetailsResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные нового клиента",
                    required = true
            )
            @Valid @RequestBody ClientCreateRequest request
    ) {
        Client client = clientService.create(clientWebMapper.toClientCreateCommand(request));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(clientWebMapper.toClientDetailsResponse(client));
    }

    @Operation(summary = "Удалить клиента")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Клиент удалён"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный формат идентификатора",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Клиент не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "Идентификатор клиента", example = "a3bb189e-8bf9-3888-9912-ace4e6543002")
            @PathVariable UUID id
    ) {
        clientService.deleteById(id);
    }

    @Operation(summary = "Получить клиента по идентификатору")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Клиент найден",
                    content = @Content(schema = @Schema(implementation = ClientDetailsResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный формат идентификатора",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Клиент не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ClientDetailsResponse> findById(
            @Parameter(description = "Идентификатор клиента", example = "a3bb189e-8bf9-3888-9912-ace4e6543002")
            @PathVariable UUID id
    ) {
        Client client = clientService.findById(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(clientWebMapper.toClientDetailsResponse(client));
    }

    @Operation(
            summary = "Найти клиентов по имени и фамилии",
            description = "Возвращает пустой список, если совпадений нет."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Результаты поиска",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClientDetailsResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Не передан обязательный параметр поиска",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @GetMapping("/search")
    public ResponseEntity<List<ClientDetailsResponse>> findByNameAndSurname(
            @Parameter(description = "Имя клиента", example = "Иван", required = true)
            @NotNull @RequestParam String name,
            @Parameter(description = "Фамилия клиента", example = "Иванов", required = true)
            @NotNull @RequestParam String surname
    ) {
        List<Client> clients = clientService.findByNameAndSurname(name, surname);
        List<ClientDetailsResponse> toResponse = clients.stream()
                .map(clientWebMapper::toClientDetailsResponse)
                .toList();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(toResponse);
    }

    @Operation(
            summary = "Получить список клиентов",
            description = "Поддерживает необязательную пагинацию через limit и offset."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список клиентов",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClientDetailsResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные параметры пагинации",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @GetMapping
    public ResponseEntity<List<ClientDetailsResponse>> findAll(
            @Parameter(description = "Максимальное количество клиентов в ответе", example = "20")
            @RequestParam(required = false)
            @Positive
            Integer limit,
            @Parameter(description = "Количество пропускаемых клиентов", example = "0")
            @RequestParam(required = false)
            @Min(0)
            Integer offset
    ) {
        List<Client> clients = clientService.findAll(limit, offset);
        List<ClientDetailsResponse> toResponse = clients.stream()
                .map(clientWebMapper::toClientDetailsResponse)
                .toList();

        return ResponseEntity.status(HttpStatus.OK)
                .body(toResponse);
    }

    @Operation(
            summary = "Изменить адрес клиента",
            description = "Полностью заменяет страну, город и улицу текущего адреса клиента."
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
                    description = "Клиент или его текущий адрес не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PatchMapping("/{id}/address")
    public ResponseEntity<String> changeAddress(
            @Parameter(description = "Идентификатор клиента", example = "a3bb189e-8bf9-3888-9912-ace4e6543002")
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новое значение адреса",
                    required = true
            )
            @RequestBody @Valid AddressCreateRequest address
    ) {

        AddressPutCommand addressPutCommand = addressWebMapper.toAddressPutCommand(address);
        clientService.changeAddress(id, addressPutCommand);

        return ResponseEntity.status(HttpStatus.OK).body("Address updated!");
    }
}
