package in.koreatech.koin.admin.bus.shuttle.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.util.StringUtils;

import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

public class ExcelRangeUtil {

    private static final DataFormatter FORMATTER = new DataFormatter();

    private static String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        return FORMATTER.formatCellValue(cell).trim();
    }

    public static List<Integer> findContiguousRouteColumns(Sheet sheet, int headerRowNum, int startCol) {
        Row headerRow = sheet.getRow(headerRowNum);
        if (headerRow == null) {
            throw invalidTemplate("필수 헤더 행이 없습니다: " + headerRowNum);
        }

        short lastCellNum = headerRow.getLastCellNum();
        if (lastCellNum <= startCol) {
            throw invalidTemplate("회차 헤더가 없습니다.");
        }

        List<Integer> columns = new ArrayList<>();
        for (int col = startCol; col < lastCellNum; col++) {
            if (StringUtils.hasText(getCellStringValue(headerRow.getCell(col)))) {
                columns.add(col);
                continue;
            }

            if (hasTextAfter(headerRow, col + 1, lastCellNum)) {
                throw invalidTemplate("회차 헤더 사이에 빈 열이 있습니다: " + col);
            }
            break;
        }

        if (columns.isEmpty()) {
            throw invalidTemplate("회차 헤더가 없습니다.");
        }
        return List.copyOf(columns);
    }

    public static List<Integer> findContiguousStopRows(
        Sheet sheet,
        int startRow,
        int stopNameCol,
        List<Integer> routeColumns
    ) {
        List<Integer> rows = new ArrayList<>();
        boolean ended = false;
        int lastRouteColumn = routeColumns.get(routeColumns.size() - 1);

        for (int rowNum = startRow; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            String stopName = row == null ? "" : getCellStringValue(row.getCell(stopNameCol));

            if (!StringUtils.hasText(stopName)) {
                if (row != null && hasTextAfter(row, 0, row.getLastCellNum())) {
                    throw invalidTemplate("정류소 이름이 없는 행에 값이 있습니다: " + rowNum);
                }
                ended = true;
                continue;
            }

            if (ended) {
                throw invalidTemplate("정류소 행 사이에 빈 행이 있습니다: " + rowNum);
            }

            rejectValuesAfterRouteColumns(row, lastRouteColumn, rowNum);
            rows.add(rowNum);
        }

        if (rows.isEmpty()) {
            throw invalidTemplate("정류소 행이 없습니다.");
        }
        return List.copyOf(rows);
    }

    public static void requireRow(Sheet sheet, int rowNum, String description) {
        if (sheet.getRow(rowNum) == null) {
            throw invalidTemplate("필수 행이 없습니다 (" + description + "): " + rowNum);
        }
    }

    public static void rejectValuesAfterRouteColumns(Row row, int lastRouteColumn, int rowNum) {
        if (row == null || row.getLastCellNum() <= lastRouteColumn + 1) {
            return;
        }

        for (int col = lastRouteColumn + 1; col < row.getLastCellNum(); col++) {
            if (StringUtils.hasText(getCellStringValue(row.getCell(col)))) {
                throw invalidTemplate("회차 헤더가 없는 열에 값이 있습니다 (행/열): " + rowNum + "/" + col);
            }
        }
    }

    public static CustomException invalidTemplate(String detail) {
        return CustomException.of(ApiResponseCode.INVALID_EXCEL_FILE_FORMAT, detail);
    }

    private static boolean hasTextAfter(Row row, int startCol, int lastCellNum) {
        for (int col = startCol; col < lastCellNum; col++) {
            if (StringUtils.hasText(getCellStringValue(row.getCell(col)))) {
                return true;
            }
        }
        return false;
    }
}
