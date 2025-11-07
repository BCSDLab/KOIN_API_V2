package in.koreatech.koin.admin.bus.commuting.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.admin.bus.commuting.dto.AdminCommutingBusResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Admin) Commuting Bus: 등하교 버스", description = "어드민 등하교 버스 정보를 관리한다.")
public interface AdminCommutingBusApi {

    @PostMapping("/admin/bus/commuting/excel")
    ResponseEntity<AdminCommutingBusResponse> parseCommutingBusExcel(
        @RequestParam(name = "commuting_bus_excel_file") MultipartFile commutingBusExcelFile
        // @Auth(permit = {ADMIN}) Integer adminId
    ) throws IOException;
}
