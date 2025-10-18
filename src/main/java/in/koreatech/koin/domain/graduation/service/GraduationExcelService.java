package in.koreatech.koin.domain.graduation.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.domain.graduation.model.GradeExcelData;
import in.koreatech.koin.domain.graduation.parser.GradeExcelDataParser;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GraduationExcelService {

    private final GradeExcelDataParser gradeExcelDataParser;

    public List<GradeExcelData> parseStudentGradeFromExcel(MultipartFile file) throws IOException {
        List<GradeExcelData> gradeExcelDatas = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)
        ) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                GradeExcelData gradeExcelData = gradeExcelDataParser.fromRow(row);
                if (row.getRowNum() == 0 || gradeExcelData.isSkipRow()) {
                    continue;
                }
                if (gradeExcelData.isTotal()) {
                    break;
                }
                gradeExcelDatas.add(gradeExcelData);
            }
        }
        return gradeExcelDatas;
    }
}
