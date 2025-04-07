package in.koreatech.koin.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin.domain.user.dto.SendVerificationCodeRequest;
import in.koreatech.koin.domain.user.dto.VerifyVerificationCodeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) User: 회원", description = "회원 관련 API")
@RequestMapping("/user/verification")
public interface UserVerificationApi {
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
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
            - 슬랙으로 인증번호 발송한다.(발송채널: 코인_이벤트알림_stage)
            - 실제 문자 또는 이메일로 인증번호를 발송하지 않는다.
            ### 클라이언트 사용 설명
            - target 값의 형식에 따라 인증 코드 전송 목적지가 달라진다.
            - 휴대폰 번호 형식의 경우: 문자로 인증 코드를 전송한다.
            - 코리아텍 이메일 형식의 경우: 코리아텍 이메일로 인증 코드를 전송한다.
            """
    )
    @PostMapping("/send")
    ResponseEntity<Void> sendVerificationCode(
        @Valid @RequestBody SendVerificationCodeRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "문자 또는 이메일 인증번호 검증")
    @PostMapping("/verify")
    ResponseEntity<Void> verifyVerificationCode(
        @Valid @RequestBody VerifyVerificationCodeRequest request
    );
}
