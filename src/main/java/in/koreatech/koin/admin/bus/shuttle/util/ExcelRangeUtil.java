package in.koreatech.koin.admin.bus.shuttle.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.util.StringUtils;

public class ExcelRangeUtil {

    public static int getNumberOfRows(Sheet sheet, int startRow, int checkColumn) {
        int cnt = 0;

        for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            if (row == null) {
                break;
            }

            Cell cell = row.getCell(checkColumn);

            if (cell == null || !StringUtils.hasText(cell.getStringCellValue())) {
                break;
            }

            cnt++;
        }

        return cnt;
    }

    public static int getNumberOfCols(Sheet sheet, int checkRow, int startCol) {
        Row row = sheet.getRow(checkRow);

        if (row == null) {
            return 0;
        }

        int lastCol = startCol;

        for (int col = startCol; col < row.getLastCellNum(); col++) {
            Cell cell = row.getCell(col);

            if (cell != null && StringUtils.hasText(cell.getStringCellValue())) {
                lastCol = col;
            }
        }

        return lastCol - startCol + 1;
    }
}
