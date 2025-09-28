package in.koreatech.koin.domain.club.manager.controller;

import in.koreatech.koin.domain.club.manager.dto.request.ClubManagerEmpowermentRequest;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

@Tag(name = "(Normal) Club Manager: 동아리 관리자", description = "동아리 관리자를 관리한다")
@RequestMapping("/clubs")
public interface ClubManagerApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "동아리 관리자 권한을 위임한다")
    @PutMapping("/empowerment")
    ResponseEntity<Void> empowermentClubManager(
        @RequestBody @Valid ClubManagerEmpowermentRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    );
}
