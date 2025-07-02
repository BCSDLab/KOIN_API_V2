package in.koreatech.koin.domain.user.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin._common.code.ApiResponseCode;
import in.koreatech.koin._common.code.ApiResponseCodes;
import in.koreatech.koin.domain.user.dto.validation.UserAccessTokenRequest;
import in.koreatech.koin.domain.user.dto.validation.UserCorrectPasswordRequest;
import in.koreatech.koin.domain.user.dto.validation.UserExistsEmailRequest;
import in.koreatech.koin.domain.user.dto.validation.UserExistsLoginIdRequest;
import in.koreatech.koin.domain.user.dto.validation.UserExistsPhoneNumberRequest;
import in.koreatech.koin.domain.user.dto.validation.UserMatchLoginIdWithEmailRequest;
import in.koreatech.koin.domain.user.dto.validation.UserMatchLoginIdWithPhoneNumberRequest;
import in.koreatech.koin.domain.user.dto.validation.UserUniqueEmailRequest;
import in.koreatech.koin.domain.user.dto.validation.UserUniqueLoginIdRequest;
import in.koreatech.koin.domain.user.dto.validation.UserUniqueNicknameRequest;
import in.koreatech.koin.domain.user.dto.validation.UserUniquePhoneNumberRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) User Validation: 유저 검증", description = "회원 검증 관련 API")
public interface UserValidationApi {

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
    ResponseEntity<Void> requireLogin(
        @ParameterObject @ModelAttribute(value = "access_token")
        @Valid UserAccessTokenRequest request
    );

    @Operation(
        summary = "로그인 아이디 중복 체크",
        description = "입력한 로그인 아이디가 중복되지 않고, 사용 가능한지 확인합니다."
    )
    @ApiResponseCodes({
        ApiResponseCode.OK,
        ApiResponseCode.INVALID_REQUEST_PAYLOAD,
        ApiResponseCode.INVALID_GENDER_INDEX,
        ApiResponseCode.DUPLICATE_LOGIN_ID
    })
    @GetMapping("/user/check/id")
    ResponseEntity<Void> requireUniqueLoginId(
        @ParameterObject @ModelAttribute("id")
        @Valid UserUniqueLoginIdRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", description = "전화번호 양식 오류", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "전화번호 중복", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(
        summary = "전화번호 사용 가능 체크",
        description = "입력한 전화번호가 중복되지 않고, 사용 가능한지 확인합니다."
    )
    @GetMapping("/user/check/phone")
    ResponseEntity<Void> requireUniquePhoneNumber(
        @ParameterObject @ModelAttribute("phone")
        @Valid UserUniquePhoneNumberRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", description = "이메일 양식 오류", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "이메일 중복", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(
        summary = "이메일 사용 가능 체크",
        description = "입력한 이메일이 중복되지 않고, 사용 가능한지 확인합니다."
    )
    @GetMapping("/user/check/email")
    ResponseEntity<Void> requireUniqueEmail(
        @ParameterObject @ModelAttribute("address")
        @Valid UserUniqueEmailRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", description = "닉네임 양식 오류", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "닉네임 중복", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(
        summary = "닉네임 사용 가능 체크",
        description = "입력한 닉네임이 중복되지 않고, 사용 가능한지 확인합니다."
    )
    @GetMapping("/user/check/nickname")
    ResponseEntity<Void> requireUniqueNickname(
        @ParameterObject @ModelAttribute("nickname")
        @Valid UserUniqueNicknameRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", description = "적절한 비밀번호가 아님", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "로그인 하지 않음", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(
        summary = "비밀번호 검증 (로그인 API로 대체 가능. 레거시)",
        description = "비밀번호가 맞는지 검증합니다."
    )
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/user/check/password")
    ResponseEntity<Void> requireCorrectPassword(
        @Valid @RequestBody UserCorrectPasswordRequest request,
        @Auth(permit = {GENERAL, STUDENT, OWNER, COOP, COUNCIL}) Integer userId
    );

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 ID 존재"),
        @ApiResponse(responseCode = "404", description = "사용자 ID 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(
        summary = "로그인 ID 존재 여부 확인",
        description = "입력한 로그인 ID가 존재하는지 확인합니다."
    )
    @PostMapping("/user/id/exists")
    ResponseEntity<Void> requireLoginIdExists(
        @Valid @RequestBody UserExistsLoginIdRequest request
    );

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "전화번호 존재"),
        @ApiResponse(responseCode = "404", description = "전화번호 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(
        summary = "전화번호 존재 여부 확인",
        description = "입력한 전화번호로 가입된 계정이 존재하는지 확인합니다."
    )
    @PostMapping("/user/phone/exists")
    ResponseEntity<Void> requirePhoneNumberExists(
        @Valid @RequestBody UserExistsPhoneNumberRequest request
    );

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "이메일 존재"),
        @ApiResponse(responseCode = "404", description = "이메일 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(
        summary = "이메일 존재 여부 확인",
        description = "입력한 이메일 주소로 가입된 계정이 존재하는지 확인합니다."
    )
    @PostMapping("/user/email/exists")
    ResponseEntity<Void> requireEmailExists(
        @Valid @RequestBody UserExistsEmailRequest request
    );

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "ID와 전화번호 일치"),
        @ApiResponse(responseCode = "400", description = "ID와 전화번호 불일치", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "ID 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(
        summary = "로그인 ID와 전화번호 일치 여부 확인",
        description = "입력한 로그인 ID와 전화번호가 일치하는지 확인합니다."
    )
    @PostMapping("/users/id/match/phone")
    ResponseEntity<Void> matchLoginIdWithPhoneNumber(
        @Valid @RequestBody UserMatchLoginIdWithPhoneNumberRequest request
    );

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "ID와 이메일 일치"),
        @ApiResponse(responseCode = "400", description = "ID와 이메일 불일치", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "ID 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(
        summary = "로그인 ID와 이메일 일치 여부 확인",
        description = "입력한 로그인 ID와 이메일 주소가 일치하는지 확인합니다."
    )
    @PostMapping("/users/id/match/email")
    ResponseEntity<Void> matchLoginIdWithEmail(
        @Valid @RequestBody UserMatchLoginIdWithEmailRequest request
    );
}
