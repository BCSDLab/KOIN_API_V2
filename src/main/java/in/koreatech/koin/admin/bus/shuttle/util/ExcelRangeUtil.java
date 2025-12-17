package in.koreatech.koin.admin.bus.shuttle.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.util.StringUtils;

public class ExcelRangeUtil {

    private static final DataFormatter FORMATTER = new DataFormatter();

    private static String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        return FORMATTER.formatCellValue(cell).trim();
    }

    public static int countUsedRowsInColumn(Sheet sheet, int startRow, int checkColumn) {
        int cnt = 0;

        for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            if (row == null) {
                break;
            }

            Cell cell = row.getCell(checkColumn);

            if (!StringUtils.hasText(getCellStringValue(cell))) {
                break;
            }

            cnt++;
        }

        return cnt;
    }

    public static int countUsedColumnsInRow(Sheet sheet, int checkRow, int startCol) {
        Row row = sheet.getRow(checkRow);

        if (row == null) {
            return 0;
        }

        int lastCol = startCol;

        for (int col = startCol; col < row.getLastCellNum(); col++) {
            Cell cell = row.getCell(col);

            if (StringUtils.hasText(getCellStringValue(cell))) {
                lastCol = col;
            }
        }

        return lastCol - startCol + 1;
    }
}
