package in.koreatech.koin.admin.abtest.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.admin.abtest.dto.request.AbtestAdminAssignRequest;
import in.koreatech.koin.admin.abtest.dto.request.AbtestAssignRequest;
import in.koreatech.koin.admin.abtest.dto.request.AbtestCloseRequest;
import in.koreatech.koin.admin.abtest.dto.request.AbtestRequest;
import in.koreatech.koin.admin.abtest.dto.response.AbtestAccessHistoryResponse;
import in.koreatech.koin.admin.abtest.dto.response.AbtestAssignResponse;
import in.koreatech.koin.admin.abtest.dto.response.AbtestDevicesResponse;
import in.koreatech.koin.admin.abtest.dto.response.AbtestResponse;
import in.koreatech.koin.admin.abtest.dto.response.AbtestUsersResponse;
import in.koreatech.koin.admin.abtest.dto.response.AbtestsResponse;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
import in.koreatech.koin.admin.abtest.useragent.UserAgent;
import in.koreatech.koin.admin.abtest.useragent.UserAgentInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RequestMapping("/abtest")
@Tag(name = "(NORMAL, ADMIN) Abtest : AB테스트", description = "AB테스트를 관리한다.")
public interface AbtestApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "(ADMIN) 실험 생성")
    @PostMapping
    ResponseEntity<AbtestResponse> createAbtest(
        @Auth(permit = {ADMIN}) Integer adminId,
        @RequestBody @Valid AbtestRequest request
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
    @Operation(summary = "(ADMIN) 실험 수정")
    @PutMapping("/{id}")
    ResponseEntity<AbtestResponse> putAbtest(
        @Auth(permit = {ADMIN}) Integer adminId,
        @PathVariable("id") Integer abtestId,
        @RequestBody @Valid AbtestRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "(ADMIN) 실험 삭제")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteAbtest(
        @Auth(permit = {ADMIN}) Integer adminId,
        @PathVariable("id") Integer abtestId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "(ADMIN) 실험 목록 조회")
    @PostMapping
    ResponseEntity<AbtestsResponse> getAbtests(
        @Auth(permit = {ADMIN}) Integer adminId,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "(ADMIN) 실험 단건 조회")
    @PostMapping("/{id}")
    ResponseEntity<AbtestResponse> getAbtest(
        @Auth(permit = {ADMIN}) Integer adminId,
        @Parameter(in = PATH) @PathVariable("id") Integer articleId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "(ADMIN) 실험 종료")
    @PostMapping("/close/{id}")
    ResponseEntity<Void> closeAbtest(
        @Auth(permit = {ADMIN}) Integer adminId,
        @PathVariable("id") Integer abtestId,
        @RequestBody @Valid AbtestCloseRequest abtestCloseRequest
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "(ADMIN) 이름으로 유저 목록 조회")
    @GetMapping("/user")
    ResponseEntity<AbtestUsersResponse> getUsersByName(
        @Auth(permit = {ADMIN}) Integer adminId,
        @RequestParam(value = "name") String userName
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "(ADMIN) 유저 id로 디바이스 목록 조회")
    @GetMapping("/user/{id}/device")
    ResponseEntity<AbtestDevicesResponse> getDevicesByUserId(
        @Auth(permit = {ADMIN}) Integer adminId,
        @PathVariable(value = "id") Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "(ADMIN) 실험군 수동 편입")
    @PostMapping("/{id}/move")
    ResponseEntity<Void> assignAbtestVariableByAdmin(
        @Auth(permit = {ADMIN}) Integer adminId,
        @PathVariable(value = "id") Integer abtestId,
        @RequestBody @Valid AbtestAdminAssignRequest abtestAdminAssignRequest
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "(NORMAL) AB테스트 토큰(access_history_id) 발급")
    @PostMapping("/assign/token")
    ResponseEntity<AbtestAccessHistoryResponse> issueAccessHistoryId(
        @UserAgent UserAgentInfo userAgentInfo,
        @UserId Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "(NORMAL) 실험군 편입 정보 확인")
    @PostMapping("/assign")
    ResponseEntity<AbtestAssignResponse> assignOrGetAbtestVariable(
        @RequestHeader("accessHistoryId") Integer accessHistoryId,
        @UserAgent UserAgentInfo userAgentInfo,
        @UserId Integer userId,
        @RequestBody @Valid AbtestAssignRequest abtestAssignRequest
    );
}
