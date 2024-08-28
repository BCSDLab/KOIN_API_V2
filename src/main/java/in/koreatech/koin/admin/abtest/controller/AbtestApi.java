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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.admin.abtest.dto.AbtestAdminAssignRequest;
import in.koreatech.koin.admin.abtest.dto.AbtestAssignRequest;
import in.koreatech.koin.admin.abtest.dto.AbtestCloseRequest;
import in.koreatech.koin.admin.abtest.dto.AbtestDevicesResponse;
import in.koreatech.koin.admin.abtest.dto.AbtestRequest;
import in.koreatech.koin.admin.abtest.dto.AbtestResponse;
import in.koreatech.koin.admin.abtest.dto.AbtestUsersResponse;
import in.koreatech.koin.admin.abtest.dto.AbtestsResponse;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
import in.koreatech.koin.global.ipaddress.IpAddress;
import in.koreatech.koin.global.useragent.UserAgent;
import in.koreatech.koin.global.useragent.UserAgentInfo;
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
    @Operation(summary = "(NORMAL) 자신의 실험군 조회")
    @GetMapping("/me")
    ResponseEntity<String> getMyAbtestVariable(
        @IpAddress String ipAddress,
        @RequestParam(name = "title") String title
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "(NORMAL) 실험군 최초 편입")
    @PostMapping("/assign")
    ResponseEntity<String> assignAbtestVariable(
        @UserAgent UserAgentInfo userAgentInfo,
        @IpAddress String ipAddress,
        @UserId Integer userId,
        @RequestBody @Valid AbtestAssignRequest abtestAssignRequest
    );
}
