package in.koreatech.koin.admin.bus.commuting.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.admin.bus.commuting.dto.AdminCommutingBusResponse;
import in.koreatech.koin.admin.bus.commuting.dto.AdminCommutingBusUpdateRequest;
import in.koreatech.koin.admin.bus.commuting.enums.SemesterType;
import in.koreatech.koin.admin.bus.commuting.service.AdminCommutingBusExcelService;
import in.koreatech.koin.admin.bus.commuting.service.AdminCommutingBusQueryService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminCommutingBusController implements AdminCommutingBusApi {

    private final AdminCommutingBusExcelService adminCommutingBusExcelService;
    private final AdminCommutingBusQueryService adminCommutingBusQueryService;

    @PostMapping(value = "/admin/bus/commuting/timetable/excel", consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<AdminCommutingBusResponse>> parseCommutingBusExcel(
        @RequestParam(name = "commuting_bus_excel_file") MultipartFile commutingBusExcelFile,
        @Auth(permit = {ADMIN}) Integer adminId
    ) throws IOException {
        List<AdminCommutingBusResponse> response = adminCommutingBusExcelService.parseCommutingBusExcel(
            commutingBusExcelFile);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/admin/bus/commuting/timetable")
    public ResponseEntity<Void> updateCommutingBusTimetable(
        @RequestParam(name = "semester_type") SemesterType semesterType,
        @Valid @RequestBody List<AdminCommutingBusUpdateRequest> requests,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminCommutingBusQueryService.updateCommutingBusTimetable(semesterType, requests);
        return ResponseEntity.ok().build();
    }
}
