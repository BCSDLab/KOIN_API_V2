package in.koreatech.koin.admin.bus.commuting.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.admin.bus.commuting.dto.AdminCommutingBusResponse;
import in.koreatech.koin.admin.bus.commuting.service.AdminCommutingBusExcelService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AdminCommutingBusController implements AdminCommutingBusApi {

    private final AdminCommutingBusExcelService adminCommutingBusExcelService;

    @PostMapping("/admin/bus/commuting/excel")
    public ResponseEntity<AdminCommutingBusResponse> parseCommutingBusExcel(
        @RequestParam(name = "commuting_bus_excel_file") MultipartFile commutingBusExcelFile
        // @Auth(permit = {ADMIN}) Integer adminId
    ) throws IOException {
        AdminCommutingBusResponse response = adminCommutingBusExcelService.parseCommutingBusExcel(
            commutingBusExcelFile);
        return ResponseEntity.ok(response);
    }
}
