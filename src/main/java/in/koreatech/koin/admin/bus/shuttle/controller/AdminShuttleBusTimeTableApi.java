package in.koreatech.koin.admin.bus.shuttle.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.admin.bus.shuttle.dto.responose.AdminShuttleBueTimeTableResponse;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Admin) ShuttleBusTimeTable: 셔틀 버스 시간표", description = "셔틀 버스의 시간표를 관리한다.")
@RequestMapping("/admin/bus/shuttle")
public interface AdminShuttleBusTimeTableApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "엑셀 파일을 업로드하여 파싱된 데이터를 미리보기 한다.")
    @PostMapping("/preview")
    ResponseEntity<AdminShuttleBueTimeTableResponse> previewShuttleBusTimeTable(
        @Auth(permit = {ADMIN}) Integer adminId,
        @RequestParam(value = "file")MultipartFile file
    );
}
