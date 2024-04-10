package in.koreatech.koin.domain.owner.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.domain.owner.dto.OwnerPasswordResetVerifyRequest;
import in.koreatech.koin.domain.owner.dto.OwnerPasswordUpdateRequest;
import in.koreatech.koin.domain.owner.dto.OwnerRegisterRequest;
import in.koreatech.koin.domain.owner.dto.OwnerResponse;
import in.koreatech.koin.domain.owner.dto.OwnerSendEmailRequest;
import in.koreatech.koin.domain.owner.dto.OwnerVerifyRequest;
import in.koreatech.koin.domain.owner.dto.OwnerVerifyResponse;
import in.koreatech.koin.domain.owner.dto.VerifyEmailRequest;
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
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "회원가입 인증번호 전송 요청")
    @PostMapping("/owners/verification/email")
    ResponseEntity<Void> requestVerificationToRegister(
        @RequestBody @Valid VerifyEmailRequest request
    );

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
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "사장님 회원가입")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/owners/register")
    ResponseEntity<Void> register(
        @Valid @RequestBody OwnerRegisterRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "사장님 인증번호 입력")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/owners/verification/code")
    ResponseEntity<OwnerVerifyResponse> codeVerification(
        @Valid @RequestBody OwnerVerifyRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "사장님 비밀번호 변경 인증번호 이메일 발송")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/owners/password/reset/verification")
    ResponseEntity<Void> sendResetPasswordEmail(
        @Valid @RequestBody OwnerSendEmailRequest request
    ) ;

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "사장님 비밀번호 변경 인증번호 인증")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/owners/password/reset/send")
    ResponseEntity<Void> sendVerifyCode(
        @Valid @RequestBody OwnerPasswordResetVerifyRequest request
    ) ;

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "사장님 비밀번호 변경")
    @SecurityRequirement(name = "Jwt Authentication")
    @PutMapping("/owners/password/reset")
    ResponseEntity<Void> updatePassword(
        @Valid @RequestBody OwnerPasswordUpdateRequest request
    ) ;
}
