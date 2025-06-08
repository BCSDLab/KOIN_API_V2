package in.koreatech.koin.domain.user.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin._common.auth.Auth;
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

@Tag(name = "(Normal) User Validation: 유저 검증", description = "회원 검증 관련 API")
public interface UserValidationApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", description = "이메일 양식 오류", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "이메일 중복", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(
        summary = "이메일 중복 체크",
        description = "입력한 이메일이 중복인지 확인합니다."
    )
    @GetMapping("/user/check/email")
    ResponseEntity<Void> checkUserEmailExist(
        @ParameterObject @ModelAttribute("address")
        @Valid CheckEmailDuplicationRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", description = "전화번호 양식 오류", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "전화번호 중복", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(
        summary = "전화번호 중복 체크",
        description = "입력한 전화번호가 중복인지 확인합니다."
    )
    @GetMapping("/user/check/phone")
    ResponseEntity<Void> checkPhoneNumberExist(
        @ParameterObject @ModelAttribute("phone")
        @Valid CheckPhoneDuplicationRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", description = "아이디 양식 오류", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "아이디 중복", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(
        summary = "아이디 중복 체크",
        description = "입력한 아이디가 중복인지 확인합니다."
    )
    @GetMapping("/user/check/id")
    ResponseEntity<Void> checkDuplicatedLoginId(
        @ParameterObject @ModelAttribute("id")
        @Valid CheckLoginIdDuplicationRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", description = "닉네임 양식 오류", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "닉네임 중복", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(
        summary = "닉네임 중복 체크",
        description = "입력한 닉네임이 중복인지 확인합니다."
    )
    @GetMapping("/user/check/nickname")
    ResponseEntity<Void> checkDuplicationOfNickname(
        @ParameterObject @ModelAttribute("nickname")
        @Valid CheckNicknameDuplicationRequest request
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
        summary = "비밀번호 검증",
        description = "적절한 비밀번호인지 검증합니다."
    )
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/user/check/password")
    ResponseEntity<Void> checkPassword(
        @Valid @RequestBody CheckUserPasswordRequest request,
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
    ResponseEntity<Void> existsByUserId(
        @Valid @RequestBody ExistsByUserIdRequest request
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
    ResponseEntity<Void> existsByPhoneNumber(
        @Valid @RequestBody ExistsByPhoneRequest request
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
    ResponseEntity<Void> existsByEmail(
        @Valid @RequestBody ExistsByEmailRequest request
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
    ResponseEntity<Void> matchUserIdWithPhoneNumber(
        @Valid @RequestBody MatchUserIdWithPhoneNumberRequest request
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
    ResponseEntity<Void> matchUserIdWithEmail(
        @Valid @RequestBody MatchUserIdWithEmailRequest request
    );
}
