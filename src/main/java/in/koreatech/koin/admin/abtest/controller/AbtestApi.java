package in.koreatech.koin.admin.abtest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.admin.abtest.dto.AbtestAssignRequest;
import in.koreatech.koin.global.auth.UserId;
import in.koreatech.koin.global.ipaddress.IpAddress;
import in.koreatech.koin.global.useragent.UserAgent;
import in.koreatech.koin.global.useragent.UserAgentInfo;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "(NORMAL) 실험군 자동 편입")
    @PostMapping("/assign")
    ResponseEntity<String> assignAbtestVariable(
        @UserAgent UserAgentInfo userAgentInfo,
        @IpAddress String ipAddress,
        @UserId Integer userId,
        @RequestBody @Valid AbtestAssignRequest abtestAssignRequest
    );
}
