package in.koreatech.koin.admin.history.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static in.koreatech.koin.common.model.Criteria.Sort;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.admin.history.dto.AdminHistoryResponse;
import in.koreatech.koin.admin.history.dto.AdminHistoriesResponse;
import in.koreatech.koin.admin.history.enums.DomainType;
import in.koreatech.koin.admin.history.enums.HttpMethodType;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Admin) History: 기록", description = "관리자 기록 관련 API")
public interface HistoryApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "히스토리 리스트 조회")
    @GetMapping("/admin/histories")
    ResponseEntity<AdminHistoriesResponse> getHistories(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) HttpMethodType requestMethod,
        @RequestParam(required = false) DomainType domainName,
        @RequestParam(required = false) Integer domainId,
        @RequestParam(required = false) Sort sort,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "히스토리 단건 조회")
    @GetMapping("/admin/history/{id}")
    ResponseEntity<AdminHistoryResponse> getHistory(
        @PathVariable(name = "id") Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
