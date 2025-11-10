package in.koreatech.koin.admin.bus.commuting.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static in.koreatech.koin.global.code.ApiResponseCode.*;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.admin.bus.commuting.dto.AdminCommutingBusResponse;
import in.koreatech.koin.admin.bus.commuting.dto.AdminCommutingBusUpdateRequest;
import in.koreatech.koin.admin.bus.commuting.enums.SemesterType;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Admin) Commuting Bus: 등하교 버스", description = "어드민 등하교 버스 정보를 관리한다.")
public interface AdminCommutingBusApi {

    @ApiResponseCodes({
        OK,
        INVALID_SHUTTLE_ROUTE_TYPE,
        INVALID_NODE_INFO_START_POINT,
        INVALID_NODE_INFO_END_POINT,
    })
    @Operation(summary = "등하교 버스 시간표 엑셀 파일 업로드")
    @PostMapping(value = "/admin/bus/commuting/timetable/excel", consumes = MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<AdminCommutingBusResponse> parseCommutingBusExcel(
        @RequestParam(name = "commuting_bus_excel_file") MultipartFile commutingBusExcelFile,
        @Auth(permit = {ADMIN}) Integer adminId
    ) throws IOException;

    @ApiResponseCodes({
        OK,
        INVALID_SHUTTLE_ROUTE_TYPE,
        INVALID_REQUEST_BODY,
    })
    @Operation(summary = "등하교 버스 시간 업데이트")
    @PutMapping("/admin/bus/commuting/timetable")
    ResponseEntity<Void> updateCommutingBusTimetable(
        @RequestParam(name = "semester_type") SemesterType semesterType,
        @Valid @RequestBody AdminCommutingBusUpdateRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
