package in.koreatech.koin.domain.graduation.parser;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.graduation.model.GradeExcelData;

@Component
public class GradeExcelDataParser {

    public GradeExcelData fromRow(Row row) {
        return new GradeExcelData(
            getCellValueAsString(row.getCell(1)),
            getCellValueAsString(row.getCell(2)),
            getCellValueAsString(row.getCell(4)),
            getCellValueAsString(row.getCell(5)),
            getCellValueAsString(row.getCell(6)),
            getCellValueAsString(row.getCell(7)),
            getCellValueAsString(row.getCell(8)),
            getCellValueAsString(row.getCell(9)),
            getCellValueAsString(row.getCell(10)),
            getCellValueAsString(row.getCell(11))
        );
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int)cell.getNumericCellValue());
            default -> "";
        };
    }
}
