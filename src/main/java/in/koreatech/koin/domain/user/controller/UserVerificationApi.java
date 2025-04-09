package in.koreatech.koin.domain.user.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.domain.user.dto.verification.FindIdResponse;
import in.koreatech.koin.domain.user.dto.verification.VerificationCountResponse;
import in.koreatech.koin.domain.user.dto.verification.EmailFindIdRequest;
import in.koreatech.koin.domain.user.dto.verification.EmailResetPasswordRequest;
import in.koreatech.koin.domain.user.dto.verification.EmailSendVerificationCodeRequest;
import in.koreatech.koin.domain.user.dto.verification.EmailVerificationCountRequest;
import in.koreatech.koin.domain.user.dto.verification.EmailVerifyVerificationCodeRequest;
import in.koreatech.koin.domain.user.dto.verification.SmsFindIdRequest;
import in.koreatech.koin.domain.user.dto.verification.SmsResetPasswordRequest;
import in.koreatech.koin.domain.user.dto.verification.SmsSendVerificationCodeRequest;
import in.koreatech.koin.domain.user.dto.verification.SmsVerificationCountRequest;
import in.koreatech.koin.domain.user.dto.verification.SmsVerifyVerificationCodeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) User(본인인증): 회원", description = "본인인증 기반으로 회원 정보를 관리한다.")
public interface UserVerificationApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(
        summary = "SMS 인증번호 발송",
        description = """
            ### 프로덕션
            - 같은 번호 기준 하루 최대 5회 인증번호 발송 가능하다.
            - 문자로 인증번호를 발송한다.
            ### 스테이지
            - 같은 번호 기준 하루 최대 5회 인증번호를 발송 가능하다.
            - 문자의 경우 슬랙으로 인증번호 발송한다.(발송채널: 코인_이벤트알림_stage)
            """
    )
    @PostMapping("/user/sms/send-code")
    ResponseEntity<Void> sendSmsVerificationCode(
        @Valid @RequestBody SmsSendVerificationCodeRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "SMS 인증번호 검증")
    @PostMapping("/user/sms/verify-code")
    ResponseEntity<Void> verifySmsVerificationCode(
        @Valid @RequestBody SmsVerifyVerificationCodeRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "24시간 SMS 인증 횟수 조회", description = "총 인증 횟수, 남은 인증 횟수, 현재 인증 횟수를 조회한다.")
    @GetMapping("/user/sms/verification-count")
    ResponseEntity<VerificationCountResponse> getSmsVerificationCount(
        @Valid @ParameterObject SmsVerificationCountRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "SMS 인증으로 ID 찾기", description = "휴대폰 인증이 완료된 후 1시간 이내로 사용이 가능하다.")
    @PostMapping("/user/sms/find-id")
    ResponseEntity<FindIdResponse> findIdBySmsVerification(
        @Valid @RequestBody SmsFindIdRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "SMS 인증으로 패스워드 리셋", description = "휴대폰 인증이 완료된 후 1시간 이내로 사용이 가능하다.")
    @PostMapping("/user/sms/reset-password")
    ResponseEntity<Void> resetPasswordBySmsVerification(
        @Valid @RequestBody SmsResetPasswordRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(
        summary = "이메일 인증번호 발송",
        description = """
            ### 프로덕션
            - 같은 이메일 기준 하루 최대 5회 인증번호 발송 가능하다.
            - 이메일로 인증번호를 발송한다.
            ### 스테이지
            - 같은 이메일 기준 하루 최대 5회 인증번호를 발송 가능하다.
            - 이메일로 인증번호를 발송한다.
            """
    )
    @PostMapping("/user/email/send-code")
    ResponseEntity<Void> sendEmailVerificationCode(
        @Valid @RequestBody EmailSendVerificationCodeRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "이메일 인증번호 검증")
    @PostMapping("/user/email/verify-code")
    ResponseEntity<Void> verifyEmailVerificationCode(
        @Valid @RequestBody EmailVerifyVerificationCodeRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "24시간 이메일 인증 횟수 조회", description = "총 인증 횟수, 남은 인증 횟수, 현재 인증 횟수를 조회한다.")
    @GetMapping("/user/email/verification-count")
    ResponseEntity<VerificationCountResponse> getEmailVerificationCount(
        @Valid @ParameterObject EmailVerificationCountRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "이메일 인증으로 ID 찾기", description = "이메일 인증이 완료된 후 1시간 이내로 사용이 가능하다.")
    @PostMapping("/user/email/find-id")
    ResponseEntity<FindIdResponse> findIdByEmailVerification(
        @Valid @RequestBody EmailFindIdRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "이메일 인증으로 패스워드 리셋", description = "이메일 인증이 완료된 후 1시간 이내로 사용이 가능하다.")
    @PostMapping("/user/email/reset-password")
    ResponseEntity<Void> resetPasswordByEmailVerification(
        @Valid @RequestBody EmailResetPasswordRequest request
    );
}
