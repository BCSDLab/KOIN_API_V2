package in.koreatech.koin.admin.bus.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.admin.bus.dto.AdminCommutingBusResponse;
import in.koreatech.koin.admin.bus.enums.SemesterType;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Admin) Bus: 버스", description = "어드민 버스 정보를 관리한다.")
public interface AdminBusApi {

    @PostMapping("/admin/bus/commuting/excel")
    ResponseEntity<AdminCommutingBusResponse> parseCommutingBusExcel(
        @RequestParam(name = "commuting_bus_excel_file") MultipartFile commutingBusExcelFile,
        @RequestParam(name = "semester_type") SemesterType semesterType,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
