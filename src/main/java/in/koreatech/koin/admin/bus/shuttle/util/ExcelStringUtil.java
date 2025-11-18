package in.koreatech.koin.admin.bus.shuttle.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;

public class ExcelStringUtil {

    private static final DataFormatter FORMATTER = new DataFormatter();

    public static String getCellValueToString(Cell cell) {
        String value = FORMATTER.formatCellValue(cell);

        return value != null ? value.trim() : "";
    }
}
