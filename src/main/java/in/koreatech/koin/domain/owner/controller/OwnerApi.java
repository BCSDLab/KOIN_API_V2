package in.koreatech.koin.domain.owner.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.domain.owner.dto.CompanyNumberCheckRequest;
import in.koreatech.koin.domain.owner.dto.OwnerRegisteredInfoResponse;
import in.koreatech.koin.domain.owner.dto.OwnerResponse;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) Owner: 사장님", description = "사장님 정보를 관리한다.")
public interface OwnerApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "사장님 정보 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/owner")
    ResponseEntity<OwnerResponse> getOwner(
        @Auth(permit = {OWNER}) Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "사업자 등록번호 중복 검증")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/owners/exists/company-number")
    ResponseEntity<Void> checkCompanyNumber(
        @Valid @RequestBody CompanyNumberCheckRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "사장님 회원가입시 입력한 가게정보 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/owner/registered-store")
    ResponseEntity<OwnerRegisteredInfoResponse> getOwnerRegisteredStoreInfo(
        @Auth(permit = {OWNER}) Integer userId
    );
}
