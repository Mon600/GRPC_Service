package com.shastage.store.web.controller;

import com.shastage.store.web.security.Authorize;

import com.shastage.store.domain.model.Image;
import com.shastage.store.domain.service.ImageService;
import com.shastage.store.web.dto.request.ImageCreateRequest;
import com.shastage.store.web.dto.request.ImageUpdateRequest;
import com.shastage.store.web.dto.response.ImageDetailsResponse;
import com.shastage.store.web.mapper.ImageWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Images", description = "Загрузка, получение, изменение и удаление изображений товаров")
@Authorize
@RestController
@RequestMapping("/api/v1/images")
@AllArgsConstructor
public class ImageController {

    ImageService imageService;
    ImageWebMapper imageWebMapper;

    @Operation(
            summary = "Загрузить изображение",
            description = "Сохраняет изображение и связывает его с существующим товаром. Байтовый массив передаётся в JSON как Base64-строка."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Изображение загружено",
                    content = @Content(schema = @Schema(implementation = ImageDetailsResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Тело запроса не прошло валидацию",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Товар не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping
    public ResponseEntity<ImageDetailsResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Изображение в Base64 и идентификатор товара",
                    required = true
            )
            @Valid @RequestBody ImageCreateRequest request
    ) {
        Image image = imageService.create(imageWebMapper.toImageCreateCommand(request));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(imageWebMapper.toImageDetailsResponse(image));
    }

    @Operation(
            summary = "Заменить изображение",
            description = "Полностью заменяет содержимое существующего изображения."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Изображение изменено",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Image updated!")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Тело запроса не прошло валидацию или идентификатор имеет неверный формат",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Изображение не найдено",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<String> update(
            @Parameter(description = "Идентификатор изображения", example = "6ba7b810-9dad-11d1-80b4-00c04fd430c8")
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новое изображение в Base64",
                    required = true
            )
            @Valid @RequestBody ImageUpdateRequest request
    ) {
        imageService.update(id, request.image());

        return ResponseEntity.status(HttpStatus.OK).body("Image updated!");
    }

    @Operation(summary = "Удалить изображение")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Изображение удалено",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Image deleted.")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный формат идентификатора",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Изображение не найдено",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(
            @Parameter(description = "Идентификатор изображения", example = "6ba7b810-9dad-11d1-80b4-00c04fd430c8")
            @PathVariable UUID id
    ) {
        imageService.deleteById(id);

        return ResponseEntity.status(HttpStatus.OK).body("Image deleted.");
    }

    @Operation(summary = "Скачать изображение товара")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Бинарное содержимое изображения",
                    headers = @Header(
                            name = "Content-Disposition",
                            description = "Имя скачиваемого файла",
                            schema = @Schema(type = "string", example = "attachment; filename=\"image-6ba7b810-9dad-11d1-80b4-00c04fd430c8\"")
                    ),
                    content = @Content(
                            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный формат идентификатора",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Товар или его изображение не найдено",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @GetMapping("/product/{productId}")
    public ResponseEntity<byte[]> findByProductId(
            @Parameter(description = "Идентификатор товара", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID productId
    ) {
        Image image = imageService.findByProductId(productId);

        return imageResponse(image);
    }

    @Operation(summary = "Скачать изображение по идентификатору")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Бинарное содержимое изображения",
                    headers = @Header(
                            name = "Content-Disposition",
                            description = "Имя скачиваемого файла",
                            schema = @Schema(type = "string", example = "attachment; filename=\"image-6ba7b810-9dad-11d1-80b4-00c04fd430c8\"")
                    ),
                    content = @Content(
                            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный формат идентификатора",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Изображение не найдено",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> findById(
            @Parameter(description = "Идентификатор изображения", example = "6ba7b810-9dad-11d1-80b4-00c04fd430c8")
            @PathVariable UUID id
    ) {
        Image image = imageService.findById(id);

        return imageResponse(image);
    }

    private ResponseEntity<byte[]> imageResponse(Image image) {
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename("image-" + image.getId())
                .build();

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(image.getImage());
    }
}
