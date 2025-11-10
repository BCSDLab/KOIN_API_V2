package in.koreatech.koin.admin.coopShop.parser;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.admin.coopShop.model.CoopShopRow;
import in.koreatech.koin.admin.coopShop.service.ExcelParser;
import io.micrometer.common.util.StringUtils;

@Component
public class POIExcelParser implements ExcelParser {

    @Override
    public List<CoopShopRow> parse(MultipartFile excelFile) {
        try (Workbook workbook = WorkbookFactory.create(excelFile.getInputStream())) {
            return parseCoopShopRow(workbook.getSheetAt(0));
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private List<CoopShopRow> parseCoopShopRow(Sheet sheet) {
        return IntStream.rangeClosed(1, sheet.getLastRowNum())
            .mapToObj(sheet::getRow)
            .filter(Predicate.not(this::isRowEmpty))
            .map(this::createCoopShopRow)
            .toList();
    }

    private boolean isRowEmpty(Row row) {
        return Objects.isNull(row) || IntStream.rangeClosed(0, row.getLastCellNum())
            .mapToObj(row::getCell)
            .allMatch(this::isBlankCell);
    }

    private boolean isBlankCell(Cell cell) {
        return Objects.isNull(cell) || StringUtils.isBlank(cell.getStringCellValue());
    }

    private CoopShopRow createCoopShopRow(Row row) {
        return new CoopShopRow(
            getCellValue(row.getCell(0)), // coopName
            getCellValue(row.getCell(1)), // phone
            getCellValue(row.getCell(2)), // location
            getCellValue(row.getCell(3)), // remark
            getCellValue(row.getCell(4)), // type
            getCellValue(row.getCell(5)), // dayOfWeek
            getCellValue(row.getCell(6)), // openTime
            getCellValue(row.getCell(7))  // closeTime
        );
    }

    private String getCellValue(Cell cell) {
        if (Objects.isNull(cell) || StringUtils.isBlank(cell.getStringCellValue())) {
            return null;
        }
        return cell.getStringCellValue();
    }
}
