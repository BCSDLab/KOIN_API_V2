package in.koreatech.koin.domain.updateversion.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin.domain.updateversion.dto.UpdateVersionResponse;
import in.koreatech.koin.domain.updateversion.model.UpdateVersionType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Update Version: 업데이트 버전", description = "업데이트 버전 정보를 관리한다")
public interface UpdateVersionApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 타입의 버전 조회")
    @GetMapping("/update/version/{type}")
    ResponseEntity<UpdateVersionResponse> getVersion(
        @PathVariable(value = "type") UpdateVersionType type
    );
}
