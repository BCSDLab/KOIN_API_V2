package in.koreatech.koin.admin.member.controller;

import static in.koreatech.koin.admin.history.enums.DomainType.TECHSTACKS;
import static in.koreatech.koin.admin.history.enums.DomainType.TRACKS;
import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.admin.history.aop.AdminActivityLogging;
import in.koreatech.koin.admin.member.dto.AdminTechStackRequest;
import in.koreatech.koin.admin.member.dto.AdminTechStackResponse;
import in.koreatech.koin.admin.member.dto.AdminTrackRequest;
import in.koreatech.koin.admin.member.dto.AdminTrackResponse;
import in.koreatech.koin.admin.member.dto.AdminTrackSingleResponse;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

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

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "트랙 생성")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/admin/tracks")
    @AdminActivityLogging(domain = TRACKS)
    ResponseEntity<AdminTrackResponse> createTrack(
        @RequestBody @Valid AdminTrackRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "트랙 단건 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/admin/tracks/{id}")
    ResponseEntity<AdminTrackSingleResponse> getTrack(
        @PathVariable("id") Integer trackId,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "트랙 수정")
    @SecurityRequirement(name = "Jwt Authentication")
    @PutMapping("/admin/tracks/{id}")
    @AdminActivityLogging(domain = TRACKS, domainIdParam = "trackId")
    ResponseEntity<AdminTrackResponse> updateTrack(
        @PathVariable("id") Integer trackId,
        @RequestBody @Valid AdminTrackRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "204", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "트랙 삭제")
    @SecurityRequirement(name = "Jwt Authentication")
    @DeleteMapping("/admin/tracks/{id}")
    @AdminActivityLogging(domain = TRACKS, domainIdParam = "trackId")
    ResponseEntity<Void> deleteTrack(
        @PathVariable("id") Integer trackId,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "기술스택 생성")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/admin/techStacks")
    @AdminActivityLogging(domain = TECHSTACKS)
    ResponseEntity<AdminTechStackResponse> createTechStack(
        @RequestBody @Valid AdminTechStackRequest request,
        @RequestParam(value = "trackName") String trackName,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "기술스택 수정")
    @SecurityRequirement(name = "Jwt Authentication")
    @PutMapping("/admin/techStacks/{id}")
    @AdminActivityLogging(domain = TECHSTACKS, domainIdParam = "techStackId")
    ResponseEntity<AdminTechStackResponse> updateTechStack(
        @RequestBody @Valid AdminTechStackRequest request,
        @RequestParam(value = "trackName") String trackName,
        @PathVariable("id") Integer techStackId,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "204", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "기술스택 삭제")
    @SecurityRequirement(name = "Jwt Authentication")
    @DeleteMapping("/admin/techStacks/{id}")
    @AdminActivityLogging(domain = TECHSTACKS, domainIdParam = "techStackId")
    ResponseEntity<Void> deleteTechStack(
        @PathVariable("id") Integer techStackId,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
