package in.koreatech.koin.domain.timetableV3.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.domain.timetableV3.dto.request.TimetableFrameCreateRequestV3;
import in.koreatech.koin.domain.timetableV3.dto.response.TimetableFrameResponseV3;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) Timetable: V3-시간표", description = "시간표 프레임을 관리한다")
public interface TimetableFrameApiV3 {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "시간표 프레임 생성")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/v3/timetables/frame")
    ResponseEntity<List<TimetableFrameResponseV3>> createTimetablesFrame(
        @Valid @RequestBody TimetableFrameCreateRequestV3 request,
        @Auth(permit = {STUDENT}) Integer userId
    );
}
