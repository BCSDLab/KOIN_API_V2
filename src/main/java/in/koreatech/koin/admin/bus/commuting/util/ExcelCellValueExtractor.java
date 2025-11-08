package in.koreatech.koin.admin.bus.commuting.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExcelCellValueExtractor {

    public static String getCellValueAsString(Row row, int cellNumber) {
        if (row == null) {
            return "";
        }
        Cell cell = row.getCell(cellNumber);
        return cell != null ? cell.getStringCellValue() : "";
    }
}

