package in.koreatech.koin.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.domain.user.dto.verification.CheckEmailRequest;
import in.koreatech.koin.domain.user.dto.verification.CheckPhoneNumberRequest;
import in.koreatech.koin.domain.user.dto.verification.CheckUserIdRequest;
import in.koreatech.koin.domain.user.dto.verification.CheckUserIdWithEmailRequest;
import in.koreatech.koin.domain.user.dto.verification.CheckUserIdWithPhoneNumberRequest;
import in.koreatech.koin.domain.user.dto.verification.EmailFindIdRequest;
import in.koreatech.koin.domain.user.dto.verification.EmailResetPasswordRequest;
import in.koreatech.koin.domain.user.dto.verification.EmailSendVerificationCodeRequest;
import in.koreatech.koin.domain.user.dto.verification.EmailVerifyVerificationCodeRequest;
import in.koreatech.koin.domain.user.dto.verification.FindIdResponse;
import in.koreatech.koin.domain.user.dto.verification.SmsFindIdRequest;
import in.koreatech.koin.domain.user.dto.verification.SmsResetPasswordRequest;
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
    @PostMapping("/user/sms/send")
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
    @PostMapping("/user/sms/verify")
    ResponseEntity<Void> verifySmsVerificationCode(@Valid @RequestBody SmsVerifyVerificationCodeRequest request);

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "ID 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "인증되지 않음 또는 유효시간 초과", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "해당 전화번호로 계정 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(
        summary = "SMS 인증으로 ID 찾기",
        description = "SMS 인증 완료 후 1시간 이내에 ID를 찾을 수 있습니다."
    )
    @PostMapping("/user/sms/id/find")
    ResponseEntity<FindIdResponse> findIdBySmsVerification(@Valid @RequestBody SmsFindIdRequest request);

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공"),
        @ApiResponse(responseCode = "400", description = "요청 데이터 오류", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 요청", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "계정 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(
        summary = "SMS 인증으로 패스워드 리셋",
        description = "SMS 인증 완료 후 1시간 이내에 비밀번호를 재설정할 수 있습니다."
    )
    @PostMapping("/user/sms/password/reset")
    ResponseEntity<Void> resetPasswordBySmsVerification(@Valid @RequestBody SmsResetPasswordRequest request);

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
    @PostMapping("/user/email/send")
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
    @PostMapping("/user/email/verify")
    ResponseEntity<Void> verifyEmailVerificationCode(@Valid @RequestBody EmailVerifyVerificationCodeRequest request);

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "ID 조회 성공"),
        @ApiResponse(responseCode = "400", description = "요청 데이터 오류", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "인증되지 않음 또는 유효시간 초과", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "해당 이메일로 계정 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(
        summary = "이메일 인증으로 ID 찾기",
        description = "이메일 인증 완료 후 1시간 이내에 ID를 찾을 수 있습니다."
    )
    @PostMapping("/user/email/id/find")
    ResponseEntity<FindIdResponse> findIdByEmailVerification(@Valid @RequestBody EmailFindIdRequest request);

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공"),
        @ApiResponse(responseCode = "400", description = "요청 데이터 오류", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 요청", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "계정 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(
        summary = "이메일 인증으로 패스워드 리셋",
        description = "이메일 인증 완료 후 1시간 이내에 비밀번호를 재설정할 수 있습니다."
    )
    @PostMapping("/user/email/password/reset")
    ResponseEntity<Void> resetPasswordByEmailVerification(@Valid @RequestBody EmailResetPasswordRequest request);

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 ID 존재"),
        @ApiResponse(responseCode = "404", description = "사용자 ID 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(
        summary = "사용자 ID 존재 여부 확인",
        description = "입력한 사용자 ID가 존재하는지 확인합니다."
    )
    @PostMapping("/user/id/exists")
    ResponseEntity<Void> checkUserIdExists(@Valid @RequestBody CheckUserIdRequest request);

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "전화번호 존재"),
        @ApiResponse(responseCode = "404", description = "전화번호 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(
        summary = "전화번호 존재 여부 확인",
        description = "입력한 전화번호로 가입된 계정이 존재하는지 확인합니다."
    )
    @PostMapping("/user/phone/exists")
    ResponseEntity<Void> checkPhoneNumberExists(@Valid @RequestBody CheckPhoneNumberRequest request);

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "이메일 존재"),
        @ApiResponse(responseCode = "404", description = "이메일 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(
        summary = "이메일 존재 여부 확인",
        description = "입력한 이메일 주소로 가입된 계정이 존재하는지 확인합니다."
    )
    @PostMapping("/user/email/exists")
    ResponseEntity<Void> checkEmailExists(@Valid @RequestBody CheckEmailRequest request);

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "ID와 전화번호 일치"),
        @ApiResponse(responseCode = "400", description = "ID와 전화번호 불일치", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "ID 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(
        summary = "사용자 ID와 전화번호 일치 여부 확인",
        description = "입력한 사용자 ID와 전화번호가 일치하는지 확인합니다."
    )
    @PostMapping("/user/id-with-phone/exists")
    ResponseEntity<Void> checkUserIdWithPhoneNumber(@Valid @RequestBody CheckUserIdWithPhoneNumberRequest request);

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "ID와 이메일 일치"),
        @ApiResponse(responseCode = "400", description = "ID와 이메일 불일치", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "ID 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(
        summary = "사용자 ID와 이메일 일치 여부 확인",
        description = "입력한 사용자 ID와 이메일 주소가 일치하는지 확인합니다."
    )
    @PostMapping("/user/id-with-email/exists")
    ResponseEntity<Void> checkUserIdWithEmail(@Valid @RequestBody CheckUserIdWithEmailRequest request);
}
