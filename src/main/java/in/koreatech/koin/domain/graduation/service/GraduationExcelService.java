package in.koreatech.koin.domain.graduation.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.domain.graduation.model.GradeExcelData;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GraduationExcelService {

    public List<GradeExcelData> parseStudentGradeFromExcel(MultipartFile file) throws IOException {
        List<GradeExcelData> gradeExcelDatas = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)
        ) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                GradeExcelData gradeExcelData = fromRow(row);
                if (row.getRowNum() == 0 || gradeExcelData.isSkipRow()) {
                    continue;
                }
                if (gradeExcelData.isTotalRow()) {
                    break;
                }
                gradeExcelDatas.add(gradeExcelData);
            }
        }
        return gradeExcelDatas;
    }

    private GradeExcelData fromRow(Row row) {
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
        if (cell == null)
            return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int)cell.getNumericCellValue());
            default -> "";
        };
    }
}
