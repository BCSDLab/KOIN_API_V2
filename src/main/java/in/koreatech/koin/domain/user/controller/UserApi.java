package in.koreatech.koin.domain.user.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.domain.user.dto.AuthResponse;
import in.koreatech.koin.domain.user.dto.EmailCheckExistsRequest;
import in.koreatech.koin.domain.user.dto.GeneralUserRegisterRequest;
import in.koreatech.koin.domain.user.dto.NicknameCheckExistsRequest;
import in.koreatech.koin.domain.user.dto.PhoneCheckExistsRequest;
import in.koreatech.koin.domain.user.dto.UserAccessTokenRequest;
import in.koreatech.koin.domain.user.dto.UserLoginRequest;
import in.koreatech.koin.domain.user.dto.UserLoginResponse;
import in.koreatech.koin.domain.user.dto.UserPasswordCheckRequest;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshRequest;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshResponse;
import in.koreatech.koin.domain.user.dto.verification.CheckEmailRequest;
import in.koreatech.koin.domain.user.dto.verification.CheckPhoneNumberRequest;
import in.koreatech.koin.domain.user.dto.verification.CheckUserIdRequest;
import in.koreatech.koin.domain.user.dto.verification.CheckUserIdWithEmailRequest;
import in.koreatech.koin.domain.user.dto.verification.CheckUserIdWithPhoneNumberRequest;
import in.koreatech.koin.domain.user.dto.verification.EmailFindIdRequest;
import in.koreatech.koin.domain.user.dto.verification.EmailResetPasswordRequest;
import in.koreatech.koin.domain.user.dto.verification.FindIdResponse;
import in.koreatech.koin.domain.user.dto.verification.SmsFindIdRequest;
import in.koreatech.koin.domain.user.dto.verification.SmsResetPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) User: 회원", description = "회원 관련 API")
public interface UserApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/v2/user/general/register")
    ResponseEntity<Void> generalUserRegisterV2(
        @RequestBody @Valid GeneralUserRegisterRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "로그인")
    @PostMapping("/user/login")
    ResponseEntity<UserLoginResponse> login(
        @RequestBody @Valid UserLoginRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "로그아웃")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/user/logout")
    ResponseEntity<Void> logout(
        @Auth(permit = {GENERAL, STUDENT, OWNER, COOP, COUNCIL}) Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "토큰 갱신")
    @PostMapping("/user/refresh")
    ResponseEntity<UserTokenRefreshResponse> refresh(
        @RequestBody @Valid UserTokenRefreshRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "사용자 권한 조회")
    @GetMapping("/user/auth")
    ResponseEntity<AuthResponse> getAuth(
        @Auth(permit = {GENERAL, STUDENT, OWNER, COOP, COUNCIL}) Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "회원 탈퇴")
    @SecurityRequirement(name = "Jwt Authentication")
    @DeleteMapping("/user")
    ResponseEntity<Void> withdraw(
        @Auth(permit = {GENERAL, STUDENT, OWNER, COOP, COUNCIL}) Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "이메일 중복 체크")
    @GetMapping("/user/check/email")
    ResponseEntity<Void> checkUserEmailExist(
        @ModelAttribute("address")
        @Valid EmailCheckExistsRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "전화번호 중복 체크")
    @GetMapping("/user/check/phone")
    ResponseEntity<Void> checkPhoneNumberExist(
        @ModelAttribute("phone")
        @Valid PhoneCheckExistsRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "닉네임 중복 체크")
    @GetMapping("/user/check/nickname")
    ResponseEntity<Void> checkDuplicationOfNickname(
        @ModelAttribute("nickname")
        @Valid NicknameCheckExistsRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "비밀번호 검증")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/user/check/password")
    ResponseEntity<Void> checkPassword(
        @Valid @RequestBody UserPasswordCheckRequest request,
        @Auth(permit = {GENERAL, STUDENT, OWNER, COOP, COUNCIL}) Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "로그인 여부 확인")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/user/check/login")
    ResponseEntity<Void> checkLogin(
        @ParameterObject @ModelAttribute(value = "access_token")
        @Valid UserAccessTokenRequest request
    );

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 ID 존재"),
        @ApiResponse(responseCode = "404", description = "사용자 ID 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(
        summary = "사용자 ID 존재 여부 확인",
        description = "입력한 사용자 ID가 존재하는지 확인합니다."
    )
    @PostMapping("/user/id/exists")
    ResponseEntity<Void> existsByUserId(@Valid @RequestBody CheckUserIdRequest request);

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "전화번호 존재"),
        @ApiResponse(responseCode = "404", description = "전화번호 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(
        summary = "전화번호 존재 여부 확인",
        description = "입력한 전화번호로 가입된 계정이 존재하는지 확인합니다."
    )
    @PostMapping("/user/phone/exists")
    ResponseEntity<Void> existsByPhoneNumber(@Valid @RequestBody CheckPhoneNumberRequest request);

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "이메일 존재"),
        @ApiResponse(responseCode = "404", description = "이메일 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(
        summary = "이메일 존재 여부 확인",
        description = "입력한 이메일 주소로 가입된 계정이 존재하는지 확인합니다."
    )
    @PostMapping("/user/email/exists")
    ResponseEntity<Void> existsByEmail(@Valid @RequestBody CheckEmailRequest request);

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "ID와 전화번호 일치"),
        @ApiResponse(responseCode = "400", description = "ID와 전화번호 불일치", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "ID 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(
        summary = "사용자 ID와 전화번호 일치 여부 확인",
        description = "입력한 사용자 ID와 전화번호가 일치하는지 확인합니다."
    )
    @PostMapping("/user/id/match/phone")
    ResponseEntity<Void> matchUserIdWithPhoneNumber(@Valid @RequestBody CheckUserIdWithPhoneNumberRequest request);

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "ID와 이메일 일치"),
        @ApiResponse(responseCode = "400", description = "ID와 이메일 불일치", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "ID 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(
        summary = "사용자 ID와 이메일 일치 여부 확인",
        description = "입력한 사용자 ID와 이메일 주소가 일치하는지 확인합니다."
    )
    @PostMapping("/user/id/match/email")
    ResponseEntity<Void> matchUserIdWithEmail(@Valid @RequestBody CheckUserIdWithEmailRequest request);

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
    @PostMapping("/user/id/find/sms")
    ResponseEntity<FindIdResponse> findIdBySmsVerification(@Valid @RequestBody SmsFindIdRequest request);

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
    @PostMapping("/user/id/find/email")
    ResponseEntity<FindIdResponse> findIdByEmailVerification(@Valid @RequestBody EmailFindIdRequest request);

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
    @PostMapping("/user/password/reset/sms")
    ResponseEntity<Void> resetPasswordBySmsVerification(@Valid @RequestBody SmsResetPasswordRequest request);

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
    @PostMapping("/user/password/reset/email")
    ResponseEntity<Void> resetPasswordByEmailVerification(@Valid @RequestBody EmailResetPasswordRequest request);
}
