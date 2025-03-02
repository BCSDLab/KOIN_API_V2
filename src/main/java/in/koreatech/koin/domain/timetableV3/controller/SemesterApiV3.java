package in.koreatech.koin.domain.timetableV3.controller;

import static in.koreatech.koin.domain.user.model.UserType.COUNCIL;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import in.koreatech.koin.domain.timetableV3.dto.response.SemesterCheckResponseV3;
import in.koreatech.koin.domain.timetableV3.dto.response.SemesterResponseV3;
import in.koreatech.koin._common.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) V3-Semester", description = "학기 정보를 관리한다")
public interface SemesterApiV3 {
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "학기 정보 조회")
    @GetMapping("/v3/semesters")
    ResponseEntity<List<SemesterResponseV3>> getSemesters();

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "학생 학기 정보 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/v3/semesters/check")
    ResponseEntity<SemesterCheckResponseV3> getStudentSemesters(
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    );
}
