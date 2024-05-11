package in.koreatech.koin.admin.track.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import in.koreatech.koin.admin.track.dto.AdminTrackResponse;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Admin) Track: BCSDLab 트랙", description = "관리자 권한으로 BCSDLab 트랙 정보를 관리한다")
public interface AdminTrackApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "트랙 목록 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/admin/tracks")
    ResponseEntity<List<AdminTrackResponse>> getTracks(
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
