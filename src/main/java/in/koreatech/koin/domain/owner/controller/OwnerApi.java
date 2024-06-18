package in.koreatech.koin.domain.owner.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.domain.owner.dto.CompanyNumberCheckRequest;
import in.koreatech.koin.domain.owner.dto.OwnerEmailVerifyRequest;
import in.koreatech.koin.domain.owner.dto.OwnerLoginRequest;
import in.koreatech.koin.domain.owner.dto.OwnerLoginResponse;
import in.koreatech.koin.domain.owner.dto.OwnerPasswordResetVerifyEmailRequest;
import in.koreatech.koin.domain.owner.dto.OwnerPasswordResetVerifySmsRequest;
import in.koreatech.koin.domain.owner.dto.OwnerPasswordUpdateEmailRequest;
import in.koreatech.koin.domain.owner.dto.OwnerPasswordUpdateSmsRequest;
import in.koreatech.koin.domain.owner.dto.OwnerRegisterByPhoneRequest;
import in.koreatech.koin.domain.owner.dto.OwnerRegisterRequest;
import in.koreatech.koin.domain.owner.dto.OwnerResponse;
import in.koreatech.koin.domain.owner.dto.OwnerSendEmailRequest;
import in.koreatech.koin.domain.owner.dto.OwnerSendSmsRequest;
import in.koreatech.koin.domain.owner.dto.OwnerSmsVerifyRequest;
import in.koreatech.koin.domain.owner.dto.OwnerVerifyResponse;
import in.koreatech.koin.domain.owner.dto.VerifyEmailRequest;
import in.koreatech.koin.domain.owner.dto.VerifySmsRequest;
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
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "사장님 로그인")
    @PostMapping("/owner/login")
    ResponseEntity<OwnerLoginResponse> ownerLogin(
        @RequestBody @Valid OwnerLoginRequest request
    );

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
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(
        summary = "회원가입 문자 인증번호 발송",
        description = """
            ### 프로덕션
            - 하루 최대 5회 인증번호를 발송 가능.
            - 문자로 인증번호 발송.
            ### 스테이지
            - 하루 최대 발송 횟수 제한 없음.
            - 슬랙으로 인증번호 발송.(발송채널: 코인_이벤트알림_stage)
            - 배포 전 QA할 때 스테이지 환경에서도 최대 5회 인증번호를 발송할 수 있게 설정.
            ### 클라이언트 사용 설명
            - 해당 api를 사용하면 위의 내용들이 자동으로 적용된다.
            - 클라이언트는 해당 api를 사용하기만 하면 된다.
            """
    )
    @PostMapping("/owners/verification/sms")
    ResponseEntity<Void> requestVerificationToRegisterBySms(
        @RequestBody @Valid VerifySmsRequest verifySmsRequest
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
        }
    )
    @Operation(summary = "전화번호를 이용한 사장님 회원가입")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/owners/register/phone")
    ResponseEntity<Void> registerByPhone(
        @Valid @RequestBody OwnerRegisterByPhoneRequest request
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
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "사장님 회원가입 문자 인증번호 입력")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/owners/verification/code/sms")
    ResponseEntity<OwnerVerifyResponse> codeVerification(
        @Valid @RequestBody OwnerSmsVerifyRequest request
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
        }
    )
    @Operation(
        summary = "사장님 비밀번호 변경 인증번호 문자 발송",
        description = """
            ### 프로덕션
            - 하루 최대 5회 인증번호를 발송 가능.
            - 문자로 인증번호 발송.
            ### 스테이지
            - 하루 최대 발송 횟수 제한 없음.
            - 슬랙으로 인증번호 발송.(발송채널: 코인_이벤트알림_stage)
            - 배포 전 QA할 때 스테이지 환경에서도 최대 5회 인증번호를 발송할 수 있게 설정.
            ### 클라이언트 사용 설명
            - 해당 api를 사용하면 위의 내용들이 자동으로 적용된다.
            - 클라이언트는 해당 api를 사용하기만 하면 된다.
            """
    )
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/owners/password/reset/verification/sms")
    ResponseEntity<Void> sendResetPasswordBySms(
        @Valid @RequestBody OwnerSendSmsRequest request
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
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "사장님 비밀번호 변경 문자 인증번호 입력")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/owners/password/reset/send/sms")
    ResponseEntity<Void> sendVerifyCodeBySms(
        @Valid @RequestBody OwnerPasswordResetVerifySmsRequest request
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

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "문자 인증을 이용한 사장님 비밀번호 변경")
    @SecurityRequirement(name = "Jwt Authentication")
    @PutMapping("/owners/password/reset/sms")
    ResponseEntity<Void> updatePasswordBySms(
        @Valid @RequestBody OwnerPasswordUpdateSmsRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "사업자 등록번호 중복 검증")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/owners/check/company-number")
    ResponseEntity<Void> checkCompanyNumber(
        @ModelAttribute("company_number")
        @Valid CompanyNumberCheckRequest request
    );
}
