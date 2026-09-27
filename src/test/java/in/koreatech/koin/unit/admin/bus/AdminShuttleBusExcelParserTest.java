package in.koreatech.koin.unit.admin.bus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import in.koreatech.koin.admin.bus.shuttle.service.AdminShuttleBusExcelService;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

class AdminShuttleBusExcelParserTest {

    private final AdminShuttleBusExcelService excelService = new AdminShuttleBusExcelService();

    @Test
    void skipsHiddenAndVeryHiddenSheetsAndReadsNumericExcelTime() {
        byte[] workbook = workbookWith(xlsx -> {
            xlsx.createSheet("숨김");
            xlsx.setSheetVisibility(0, SheetVisibility.HIDDEN);
            xlsx.createSheet("매우숨김");
            xlsx.setSheetVisibility(1, SheetVisibility.VERY_HIDDEN);
            xlsx.createSheet("공개");
            xlsx.setSheetVisibility(2, SheetVisibility.VISIBLE);
            writeValidSheet(xlsx.getSheetAt(2), xlsx);
        });

        var response = excelService.getShuttleBusTimetablePreview(file(workbook));

        assertThat(response.shuttleBusTimetables()).hasSize(1);
        assertThat(response.shuttleBusTimetables().get(0).routeInfo().get(0).arrivalTime())
            .containsExactly("08:00");
    }

    @Test
    void rejectsWorkbookWithNoVisibleSheets() {
        byte[] workbook = workbookWith(xlsx -> {
            xlsx.createSheet("숨김");
            xlsx.setSheetVisibility(0, SheetVisibility.HIDDEN);
        });

        assertInvalidTemplate(workbook);
    }

    @Test
    void rejectsHeaderGapInsteadOfShiftingRouteTimes() {
        byte[] workbook = workbookWith(xlsx -> {
            var sheet = xlsx.createSheet("공개");
            writeMetadataAndStops(sheet, xlsx);
            sheet.getRow(3).createCell(3).setCellValue("2회");
            sheet.getRow(5).createCell(3).setCellValue("08:10");
        });

        assertInvalidTemplate(workbook);
    }

    @Test
    void rejectsUnnamedStopRowContainingTime() {
        byte[] workbook = workbookWith(xlsx -> {
            var sheet = xlsx.createSheet("공개");
            writeMetadataAndStops(sheet, xlsx);
            sheet.createRow(6).createCell(1).setCellValue("08:10");
        });

        assertInvalidTemplate(workbook);
    }

    @Test
    void rejectsMissingRequiredMetadataRow() {
        byte[] workbook = workbookWith(xlsx -> {
            var sheet = xlsx.createSheet("공개");
            writeMetadataAndStops(sheet, xlsx);
            sheet.removeRow(sheet.getRow(1));
        });

        assertInvalidTemplate(workbook);
    }

    private static void writeValidSheet(Sheet sheet, XSSFWorkbook workbook) {
        writeMetadataAndStops(sheet, workbook);
    }

    private static void writeMetadataAndStops(org.apache.poi.ss.usermodel.Sheet sheet, XSSFWorkbook workbook) {
        sheet.createRow(0).createCell(1).setCellValue("CHEONAN_ASAN");
        sheet.createRow(1).createCell(1).setCellValue("SHUTTLE");
        sheet.createRow(3).createCell(1).setCellValue("1회");
        sheet.createRow(4).createCell(1).setCellValue("등교");
        sheet.createRow(5).createCell(0).setCellValue("한기대");
        Cell time = sheet.getRow(5).createCell(1);
        time.setCellValue(8d / 24d);
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("HH:mm"));
        time.setCellStyle(style);
    }

    private static MockMultipartFile file(byte[] workbook) {
        return new MockMultipartFile("shuttle-bus-timetable", "timetable.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", workbook);
    }

    private static byte[] workbookWith(Consumer<XSSFWorkbook> writer) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            writer.accept(workbook);
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private void assertInvalidTemplate(byte[] workbook) {
        assertThatThrownBy(() -> excelService.getShuttleBusTimetablePreview(file(workbook)))
            .isInstanceOf(CustomException.class)
            .extracting("errorCode")
            .isEqualTo(ApiResponseCode.INVALID_EXCEL_FILE_FORMAT);
    }
}
