package in.koreatech.koin.domain.user.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.domain.user.dto.FindIdRequest;
import in.koreatech.koin.domain.user.dto.FindIdResponse;
import in.koreatech.koin.domain.user.dto.ResetPasswordRequest;
import in.koreatech.koin.domain.user.dto.SendVerificationCodeRequest;
import in.koreatech.koin.domain.user.dto.VerificationCountRequest;
import in.koreatech.koin.domain.user.dto.VerificationCountResponse;
import in.koreatech.koin.domain.user.dto.VerifyVerificationCodeRequest;
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
        summary = "문자 또는 코리아텍 이메일 인증번호 발송",
        description = """
            ### 프로덕션
            - 같은 번호 기준 하루 최대 5회 인증번호 발송 가능하다.
            - 문자 또는 이메일로 인증번호를 발송한다.
            ### 스테이지
            - 같은 번호 또는 이메일 기준 하루 최대 5회 인증번호를 발송 가능하다.
            - 문자의 경우 슬랙으로 인증번호 발송한다.(발송채널: 코인_이벤트알림_stage)
            - 이메일의 경우 이메일로 인증번호를 발송한다.
            ### 클라이언트 사용 설명
            - target 값의 형식에 따라 인증 코드 전송 목적지가 달라진다.
            - 휴대폰 번호 형식의 경우: 문자로 인증 코드를 전송한다.
            - 코리아텍 이메일 형식의 경우: 코리아텍 이메일로 인증 코드를 전송한다.
            """
    )
    @PostMapping("/user/verification/send")
    ResponseEntity<Void> sendVerificationCode(
        @Valid @RequestBody SendVerificationCodeRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "문자 또는 이메일 인증번호 검증")
    @PostMapping("/user/verification/verify")
    ResponseEntity<Void> verifyVerificationCode(
        @Valid @RequestBody VerifyVerificationCodeRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "인증 횟수 조회", description = "총 인증 횟수, 남은 인증 횟수, 현재 인증 횟수를 조회한다.")
    @GetMapping("/user/verification/count")
    ResponseEntity<VerificationCountResponse> getVerificationCount(
        @Valid @ParameterObject VerificationCountRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "ID 찾기", description = "휴대폰 또는 이메일 인증이 완료된 후 1시간 이내로 사용이 가능하다.")
    @PostMapping("/user/id/find")
    ResponseEntity<FindIdResponse> findIdByVerification(
        @Valid @RequestBody FindIdRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "패스워드 리셋", description = "휴대폰 또는 이메일 인증이 완료된 후 1시간 이내로 사용이 가능하다.")
    @PostMapping("/user/password/reset")
    ResponseEntity<Void> resetPassword(
        @Valid @RequestBody ResetPasswordRequest request
    );
}
