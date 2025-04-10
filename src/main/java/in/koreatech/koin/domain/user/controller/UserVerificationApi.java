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

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "429", content = @Content(schema = @Schema(hidden = true))),
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
            
            하루 인증 횟수 초과 시 `429 TooManyRequests` 반환한다.
            """
    )
    @PostMapping("/user/sms/send")
    ResponseEntity<VerificationCountResponse> sendSmsVerificationCode(
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
    @PostMapping("/user/sms/verify")
    ResponseEntity<Void> verifySmsVerificationCode(
        @Valid @RequestBody SmsVerifyVerificationCodeRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "SMS 인증으로 ID 찾기",
        description = """
            휴대폰 인증이 완료된 후 1시간 이내로 사용이 가능하다.
            
            인증을 하지 않고 사용하거나, 1시간이 지난 후 사용하면 `401 UnAuthorized` 반환한다.
            해당 휴대폰으로 생성된 계정이 없다면 `404 NotFound` 반환한다.
            """)
    @PostMapping("/user/sms/id/find")
    ResponseEntity<FindIdResponse> findIdBySmsVerification(
        @Valid @RequestBody SmsFindIdRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "SMS 인증으로 패스워드 리셋",
        description = """
            휴대폰 인증이 완료된 후 1시간 이내로 사용이 가능하다.
            
            인증을 하지 않고 사용하거나, 1시간이 지난 후 사용하면 `401 UnAuthorized` 반환한다.
            해당 휴대폰으로 생성된 계정이 없다면 `404 NotFound` 반환한다.
            """)
    @PostMapping("/user/sms/password/reset")
    ResponseEntity<Void> resetPasswordBySmsVerification(
        @Valid @RequestBody SmsResetPasswordRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "429", content = @Content(schema = @Schema(hidden = true))),
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
            
            하루 인증 횟수 초과 시 `429 TooManyRequests` 반환한다.
            """
    )
    @PostMapping("/user/email/send")
    ResponseEntity<VerificationCountResponse> sendEmailVerificationCode(
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
    @PostMapping("/user/email/verify")
    ResponseEntity<Void> verifyEmailVerificationCode(
        @Valid @RequestBody EmailVerifyVerificationCodeRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "이메일 인증으로 ID 찾기",
        description = """
            이메일 인증이 완료된 후 1시간 이내로 사용이 가능하다.
            
            인증을 하지 않고 사용하거나, 1시간이 지난 후 사용하면 `401 UnAuthorized` 반환한다.
            해당 이메일로 생성된 계정이 없다면 `404 NotFound` 반환한다.
            """)
    @PostMapping("/user/email/id/find")
    ResponseEntity<FindIdResponse> findIdByEmailVerification(
        @Valid @RequestBody EmailFindIdRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "이메일 인증으로 패스워드 리셋",
        description = """
            이메일 인증이 완료된 후 1시간 이내로 사용이 가능하다.
            
            인증을 하지 않고 사용하거나, 1시간이 지난 후 사용하면 `401 UnAuthorized` 반환한다.
            해당 이메일로 생성된 계정이 없다면 `404 NotFound` 반환한다.
            """)
    @PostMapping("/user/email/password/reset")
    ResponseEntity<Void> resetPasswordByEmailVerification(
        @Valid @RequestBody EmailResetPasswordRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "사용자 ID 존재 여부 확인",
        description = """
            비밀번호 찾기 전 사용자 ID가 존재하는지 확인한다.
            
            사용자 ID가 존재할 경우 `200 OK` 반환한다.
            사용자 ID가 존재하지 않을 경우 `404 NotFound` 반환한다.
            """)
    @PostMapping("/user/check/user-id")
    ResponseEntity<Void> checkUserIdExists(@Valid @RequestBody CheckUserIdRequest request);

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "전화번호 존재 여부 확인",
        description = """
            인증번호 전송 전 전화번호가 존재하는지 확인한다.
            
            전화번호가 존재할 경우 `200 OK` 반환한다.
            전화번호가 존재하지 않을 경우 `404 NotFound` 반환한다.
            """)
    @PostMapping("/user/id/exists")
    ResponseEntity<Void> checkPhoneNumberExists(@Valid @RequestBody CheckPhoneNumberRequest request);

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "이메일 존재 여부 확인",
        description = """
            인증번호 전송 전 이메일이 존재하는지 확인한다.
            
            이메일이 존재할 경우 `200 OK` 반환한다.
            이메일이 존재하지 않을 경우 `404 NotFound` 반환한다.
            """)
    @PostMapping("/user/phone/exists")
    ResponseEntity<Void> checkEmailExists(@Valid @RequestBody CheckEmailRequest request);

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "사용자 ID와 전화번호 일치 여부 확인",
        description = """
            인증번호 전송 전 사용자 ID와 전화번호 일치 여부를 확인한다.
            
            일치할 경우 `200 OK` 반환한다.
            일치하지 않을 경우 `400 Bad Request` 반환한다.
            입력한 ID가 존재하지 않을 경우 `404 NotFound` 반환한다.
            """)
    @PostMapping("/user/id-with-phone/exists")
    ResponseEntity<Void> checkUserIdWithPhoneNumber(@Valid @RequestBody CheckUserIdWithPhoneNumberRequest request);

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "사용자 ID와 이메일 일치 여부 확인",
        description = """
            인증번호 전송 전 사용자 ID와 이메일 일치 여부를 확인한다.
            
            일치할 경우 `200 OK` 반환한다.
            일치하지 않을 경우 `400 Bad Request` 반환한다.
            입력한 ID가 존재하지 않을 경우 `404 NotFound` 반환한다.
            """)
    @PostMapping("/user/id-with-email/exists")
    ResponseEntity<Void> checkUserIdWithEmail(@Valid @RequestBody CheckUserIdWithEmailRequest request);
}
