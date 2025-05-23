package in.koreatech.koin.domain.user.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.domain.user.dto.AuthResponse;
import in.koreatech.koin.domain.user.dto.FindIdByEmailRequest;
import in.koreatech.koin.domain.user.dto.FindIdBySmsRequest;
import in.koreatech.koin.domain.user.dto.FindIdResponse;
import in.koreatech.koin.domain.user.dto.RegisterUserRequest;
import in.koreatech.koin.domain.user.dto.ResetPasswordByEmailRequest;
import in.koreatech.koin.domain.user.dto.ResetPasswordBySmsRequest;
import in.koreatech.koin.domain.user.dto.UserAccessTokenRequest;
import in.koreatech.koin.domain.user.dto.UserLoginRequest;
import in.koreatech.koin.domain.user.dto.UserLoginRequestV2;
import in.koreatech.koin.domain.user.dto.UserLoginResponse;
import in.koreatech.koin.domain.user.dto.UserResponse;
import in.koreatech.koin.domain.user.dto.RefreshUserTokenRequest;
import in.koreatech.koin.domain.user.dto.RefreshUserTokenResponse;
import in.koreatech.koin.domain.user.dto.UpdateUserRequest;
import in.koreatech.koin.domain.user.dto.UpdateUserResponse;
import in.koreatech.koin.domain.user.dto.validation.CheckEmailDuplicationRequest;
import in.koreatech.koin.domain.user.dto.validation.CheckLoginIdDuplicationRequest;
import in.koreatech.koin.domain.user.dto.validation.CheckNicknameDuplicationRequest;
import in.koreatech.koin.domain.user.dto.validation.CheckPhoneDuplicationRequest;
import in.koreatech.koin.domain.user.dto.validation.CheckUserPasswordRequest;
import in.koreatech.koin.domain.user.dto.validation.ExistsByEmailRequest;
import in.koreatech.koin.domain.user.dto.validation.ExistsByPhoneRequest;
import in.koreatech.koin.domain.user.dto.validation.ExistsByUserIdRequest;
import in.koreatech.koin.domain.user.dto.validation.MatchUserIdWithEmailRequest;
import in.koreatech.koin.domain.user.dto.validation.MatchUserIdWithPhoneNumberRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) User: 유저", description = "유저 관련 API")
public interface UserApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "일반인 정보 조회 V2")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/v2/users/me")
    ResponseEntity<UserResponse> getUserV2(
        @Auth(permit = {GENERAL}) Integer userId
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
    @Operation(summary = "일반인 정보 수정 V2")
    @SecurityRequirement(name = "Jwt Authentication")
    @PutMapping("/v2/users/me")
    ResponseEntity<UpdateUserResponse> updateUserV2(
        @Auth(permit = {GENERAL}) Integer userId,
        @Valid @RequestBody UpdateUserRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "일반인 회원가입(문자 인증)")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/v2/users/register")
    ResponseEntity<Void> registerUserV2(
        @RequestBody @Valid RegisterUserRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "로그인 V2")
    @PostMapping("/v2/users/login")
    ResponseEntity<UserLoginResponse> loginV2(
        @RequestBody @Valid UserLoginRequestV2 request
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
    ResponseEntity<RefreshUserTokenResponse> refresh(
        @RequestBody @Valid RefreshUserTokenRequest request
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
            @ApiResponse(responseCode = "200", description = "로그인 확인"),
            @ApiResponse(responseCode = "401", description = "로그인 하지 않음", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(
        summary = "로그인 여부 확인",
        description = "액세스 토큰을 통해 로그인 여부를 확인합니다."
    )
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/user/check/login")
    ResponseEntity<Void> checkLogin(
        @ParameterObject @ModelAttribute(value = "access_token")
        @Valid UserAccessTokenRequest request
    );

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "ID 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "인증되지 않음 또는 유효시간 초과", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "해당 전화번호로 계정 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(
        summary = "SMS 인증으로 로그인 ID 찾기",
        description = "SMS 인증 완료 후 1시간 이내에 ID를 찾을 수 있습니다."
    )
    @PostMapping("/users/id/find/sms")
    ResponseEntity<FindIdResponse> findIdBySmsVerification(@Valid @RequestBody FindIdBySmsRequest request);

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "ID 조회 성공"),
        @ApiResponse(responseCode = "400", description = "요청 데이터 오류", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "인증되지 않음 또는 유효시간 초과", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "해당 이메일로 계정 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(
        summary = "이메일 인증으로 로그인 ID 찾기",
        description = "이메일 인증 완료 후 1시간 이내에 ID를 찾을 수 있습니다."
    )
    @PostMapping("/users/id/find/email")
    ResponseEntity<FindIdResponse> findIdByEmailVerification(@Valid @RequestBody FindIdByEmailRequest request);

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
    @PostMapping("/users/password/reset/sms")
    ResponseEntity<Void> resetPasswordBySmsVerification(@Valid @RequestBody ResetPasswordBySmsRequest request);

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
    @PostMapping("/users/password/reset/email")
    ResponseEntity<Void> resetPasswordByEmailVerification(@Valid @RequestBody ResetPasswordByEmailRequest request);
}
