package in.koreatech.koin.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin.domain.user.dto.verification.EmailSendVerificationCodeRequest;
import in.koreatech.koin.domain.user.dto.verification.EmailVerifyVerificationCodeRequest;
import in.koreatech.koin.domain.user.dto.verification.SmsSendVerificationCodeRequest;
import in.koreatech.koin.domain.user.dto.verification.SmsVerifyVerificationCodeRequest;
import in.koreatech.koin.domain.user.dto.verification.VerificationCountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) User(본인인증): 회원", description = "본인인증 기반으로 회원 정보를 관리한다.")
@RequestMapping("/user/verification")
public interface UserVerificationApi {

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "인증 번호 전송 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "429", description = "인증 번호 전송 횟수 초과", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(
        summary = "SMS 인증번호 발송",
        description = """
            요청한 전화번호로 하루 최대 5회까지 인증번호를 전송할 수 있습니다.
            
            스테이지 환경에서는 `코인_이벤트알림_STAGE` 채널로 인증번호를 전송합니다.
            """
    )
    @PostMapping("/sms/send")
    ResponseEntity<VerificationCountResponse> sendSmsVerificationCode(
        @Valid @RequestBody SmsSendVerificationCodeRequest request);

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "인증 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 인증 코드 또는 만료됨", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "인증 코드 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(
        summary = "SMS 인증번호 검증",
        description = "전화번호로 전송된 인증번호를 검증합니다."
    )
    @PostMapping("/sms/verify")
    ResponseEntity<Void> verifySmsVerificationCode(@Valid @RequestBody SmsVerifyVerificationCodeRequest request);

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "인증 번호 전송 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "429", description = "인증 번호 전송 횟수 초과", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(
        summary = "이메일 인증번호 발송",
        description = """
            요청한 이메일 주소로 하루 최대 5회까지 인증번호를 전송할 수 있습니다.
            
            스테이지 환경에서도 전송합니다.
            """
    )
    @PostMapping("/email/send")
    ResponseEntity<VerificationCountResponse> sendEmailVerificationCode(
        @Valid @RequestBody EmailSendVerificationCodeRequest request);

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "이메일 인증 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 인증 코드", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "이메일 인증 코드 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(
        summary = "이메일 인증번호 검증",
        description = "이메일로 전송된 인증번호를 검증합니다."
    )
    @PostMapping("/email/verify")
    ResponseEntity<Void> verifyEmailVerificationCode(@Valid @RequestBody EmailVerifyVerificationCodeRequest request);
}
