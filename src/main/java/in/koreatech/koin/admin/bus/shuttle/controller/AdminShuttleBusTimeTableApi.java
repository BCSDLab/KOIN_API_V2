package in.koreatech.koin.admin.bus.shuttle.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static in.koreatech.koin.global.code.ApiResponseCode.*;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.admin.bus.shuttle.dto.response.AdminShuttleBusTimeTableResponse;
import in.koreatech.koin.admin.bus.shuttle.model.SemesterType;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Admin) ShuttleBusTimeTable: 셔틀 버스 시간표", description = "셔틀 버스의 시간표를 관리한다.")
@RequestMapping("/admin/bus/shuttle/timetable")
public interface AdminShuttleBusTimeTableApi {

    @ApiResponseCodes({
        OK,
        INVALID_EXCEL_FILE_TYPE,
        INVALID_EXCEL_ROW,
        INVALID_EXCEL_COL
    })
    @Operation(summary = "엑셀 파일을 업로드하여 파싱된 데이터를 미리보기 한다.")
    @PostMapping("/excel")
    ResponseEntity<List<AdminShuttleBusTimeTableResponse>> previewShuttleBusTimeTable(
        @Auth(permit = {ADMIN}) Integer adminId,
        @RequestParam(name = "shuttle-bus-time-table") MultipartFile file
    );

    @ApiResponseCodes({
        OK
    })
    @Operation(summary = "셔틀 버스 시간표를 업데이트한다.")
    @PutMapping
    ResponseEntity<List<AdminShuttleBusTimeTableResponse>> updateShuttleBusTimeTable(
        @RequestParam(name = "semester_type") SemesterType semesterType,
        @Valid @RequestBody AdminShuttleBusUpdateRequest requst,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
