package in.koreatech.koin.global.domain.upload.controller;


import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.domain.user.model.UserType;
import static in.koreatech.koin.domain.user.model.UserType.OWNER;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.domain.upload.dto.UploadFileResponse;
import in.koreatech.koin.global.domain.upload.dto.UploadUrlRequest;
import in.koreatech.koin.global.domain.upload.dto.UploadUrlResponse;
import in.koreatech.koin.global.domain.upload.model.ImageUploadDomain;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) Upload : 파일 업로드", description = "파일 업로드를 수행한다.")
public interface UploadApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "413", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "415", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "파일을 업로드할 수 있는 URL 생성", description = """
        {domain} 지원 목록
        - items
        - lands
        - circles
        - market
        - shops
        - members
        - owners
        """)
    @PostMapping("/{domain}/upload/url")
    ResponseEntity<UploadUrlResponse> getPresignedUrl(
        @PathVariable ImageUploadDomain domain,
        @RequestBody @Valid UploadUrlRequest request,
        @Auth(permit = {UserType.OWNER, STUDENT}) Long memberId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "413", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "415", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "단건 파일 업로드", description = """
        {domain} 지원 목록
        - items
        - lands
        - circles
        - market
        - shops
        - members
        - owners
        """)
    @PostMapping(
        value = "/{domain}/upload/file",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<UploadFileResponse> uploadFile(
        @PathVariable ImageUploadDomain domain,
        @RequestPart MultipartFile multipartFile,
        @Auth(permit = {OWNER, STUDENT}) Long memberId
    );
}
