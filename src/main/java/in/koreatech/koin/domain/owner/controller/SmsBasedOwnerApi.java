package in.koreatech.koin.domain.owner.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.admin.abtest.useragent.UserAgent;
import in.koreatech.koin.admin.abtest.useragent.UserAgentInfo;
import in.koreatech.koin.domain.owner.dto.OwnerVerifyResponse;
import in.koreatech.koin.domain.owner.dto.sms.OwnerAccountCheckExistsRequest;
import in.koreatech.koin.domain.owner.dto.sms.OwnerLoginRequest;
import in.koreatech.koin.domain.owner.dto.sms.OwnerLoginResponse;
import in.koreatech.koin.domain.owner.dto.sms.OwnerPasswordResetVerifySmsRequest;
import in.koreatech.koin.domain.owner.dto.sms.OwnerPasswordUpdateSmsRequest;
import in.koreatech.koin.domain.owner.dto.sms.OwnerRegisterByPhoneRequest;
import in.koreatech.koin.domain.owner.dto.sms.OwnerSendSmsRequest;
import in.koreatech.koin.domain.owner.dto.sms.OwnerSmsVerifyRequest;
import in.koreatech.koin.domain.owner.dto.sms.VerifySmsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) Owner(sms): 사장님", description = "전화번호를 기반으로 사장님 정보를 관리한다.")
public interface SmsBasedOwnerApi {

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
        @RequestBody @Valid OwnerLoginRequest request,
        @UserAgent UserAgentInfo userAgentInfo
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
    @Operation(summary = "문자 인증을 이용한 사장님 비밀번호 변경")
    @SecurityRequirement(name = "Jwt Authentication")
    @PutMapping("/owners/password/reset/sms")
    ResponseEntity<Void> updatePasswordBySms(
        @Valid @RequestBody OwnerPasswordUpdateSmsRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "전화번호 중복 체크")
    @GetMapping("/owners/exists/account")
    ResponseEntity<Void> checkDuplicationOfPhoneNumber(
        @ModelAttribute("account")
        @Valid OwnerAccountCheckExistsRequest request
    );
}
