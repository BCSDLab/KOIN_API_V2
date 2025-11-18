package in.koreatech.koin.admin.bus.shuttle.extractor;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;

public class PoiCellExtractor {

    private static final DataFormatter FORMATTER = new DataFormatter();

    public static String extractStringValue(Cell cell) {
        String value = FORMATTER.formatCellValue(cell);

        return value != null ? value.trim() : "";
    }
}
