package in.koreatech.koin.admin.bus.shuttle.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExcelCellHelper {

    public static String getCellValue(Sheet sheet, int rowIndex, int colIndex) {
        Row row = sheet.getRow(rowIndex);

        if (row == null) {
            throw CustomException.of(ApiResponseCode.INVALID_EXCEL_ROW);
        }

        Cell cell = row.getCell(colIndex);

        if (cell == null) {
            throw CustomException.of(ApiResponseCode.INVALID_EXCEL_COL);
        }

        return cell.toString();
    }
}
