package in.koreatech.koin.admin.bus.shuttle.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;

public class ExcelStringUtil {

    private static final DataFormatter FORMATTER = new DataFormatter();

    public static String getCellValueToString(Cell cell) {
        String value = FORMATTER.formatCellValue(cell);

        return value != null ? value.trim() : "";
    }

    public static String extractDetailFromBrackets(String str) {
        int openIdx = str.indexOf('(');
        int closeIdx = str.indexOf(')');

        if (openIdx != -1 && closeIdx != -1 && closeIdx > openIdx) {
            return str.substring(openIdx + 1, closeIdx).trim();
        }

        return null;
    }

    public static String extractNameWithoutBrackets(String str) {
        int openIdx = str.indexOf('(');

        if (openIdx != -1) {
            return str.substring(0, openIdx).trim();
        }

        return str.trim();
    }
}
