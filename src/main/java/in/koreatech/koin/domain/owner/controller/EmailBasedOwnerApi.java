package in.koreatech.koin.domain.owner.controller;

import in.koreatech.koin.domain.owner.dto.email.OwnerEmailVerifyRequest;
import in.koreatech.koin.domain.owner.dto.email.OwnerPasswordResetVerifyEmailRequest;
import in.koreatech.koin.domain.owner.dto.email.OwnerPasswordUpdateEmailRequest;
import in.koreatech.koin.domain.owner.dto.email.OwnerRegisterRequest;
import in.koreatech.koin.domain.owner.dto.email.OwnerSendEmailRequest;
import in.koreatech.koin.domain.owner.dto.OwnerVerifyResponse;
import in.koreatech.koin.domain.owner.dto.email.VerifyEmailRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "(Normal) Owner(email): 사장님", description = "이메일 기반으로 사장님 정보를 관리한다.")
public interface EmailBasedOwnerApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(
        summary = "회원가입 이메일 인증번호 발송",
        description = """
            ### 프로덕션
            - 하루 최대 5회 인증번호 발송 가능.
            - 메일로 인증번호 발송.
            ### 스테이지
            - 하루 최대 발송 횟수 제한 없음.
            - 메일로 인증번호 발송.
            """
    )
    @PostMapping("/owners/verification/email")
    ResponseEntity<Void> requestVerificationToRegisterByEmail(
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
    @Operation(summary = "이메일을 이용한 사장님 회원가입")
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
    @Operation(summary = "사장님 회원가입 이메일 인증번호 입력")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/owners/verification/code")
    ResponseEntity<OwnerVerifyResponse> codeVerification(
        @Valid @RequestBody OwnerEmailVerifyRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(
        summary = "사장님 비밀번호 변경 인증번호 이메일 발송",
        description = """
            ### 프로덕션
            - 하루 최대 5회 인증번호 발송 가능.
            - 메일로 인증번호 발송.
            ### 스테이지
            - 하루 최대 발송 횟수 제한 없음.
            - 메일로 인증번호 발송.
            """
    )
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/owners/password/reset/verification")
    ResponseEntity<Void> sendResetPasswordByEmail(
        @Valid @RequestBody OwnerSendEmailRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "사장님 비밀번호 변경 이메일 인증번호 입력")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/owners/password/reset/send")
    ResponseEntity<Void> sendVerifyCode(
        @Valid @RequestBody OwnerPasswordResetVerifyEmailRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "이메일 인증을 이용한 사장님 비밀번호 변경")
    @SecurityRequirement(name = "Jwt Authentication")
    @PutMapping("/owners/password/reset")
    ResponseEntity<Void> updatePasswordByEmail(
        @Valid @RequestBody OwnerPasswordUpdateEmailRequest request
    );
}
