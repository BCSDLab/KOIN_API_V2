package in.koreatech.koin.domain.coop.dto;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class ExcelResponseBuilder {

    public static ResponseEntity<InputStreamResource> buildExcelResponse(
        ByteArrayInputStream excelFile, LocalDate startDate, LocalDate endDate
    ) {
        String fileName = startDate.toString() + " ~ " + endDate.toString() + " menu.xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + fileName);

        return ResponseEntity.ok()
            .headers(headers)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(new InputStreamResource(excelFile));
    }
}
