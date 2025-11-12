package in.koreatech.koin.admin.coopShop.parser;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import org.apache.poi.EmptyFileException;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.admin.coopShop.model.CoopShopRow;
import in.koreatech.koin.admin.coopShop.service.ExcelParser;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import io.micrometer.common.util.StringUtils;

@Component
@RequestScope
public class POIExcelParser implements ExcelParser {

    private static final int COOP_SHOPS_SHEET_INDEX = 0;
    private static final int COOP_SHOPS_START_ROW_INDEX = 2;

    private static final int MAX_ROW_COUNT = 100;
    private static final int MAX_COLUMN_COUNT = 8;

    private static final int COOP_SHOP_NAME_COLUMN_INDEX = 0;
    private static final int DAY_OF_WEEK_COLUMN_INDEX = 1;
    private static final int TYPE_COLUMN_INDEX = 2;
    private static final int OPEN_TIME_COLUMN_INDEX = 3;
    private static final int CLOSE_TIME_COLUMN_INDEX = 4;
    private static final int PHONE_COLUMN_INDEX = 5;
    private static final int REMARK_COLUMN_INDEX = 6;
    private static final int LOCATION_COLUMN_INDEX = 7;

    private Sheet sheet;

    @Override
    public List<CoopShopRow> parse(MultipartFile excel) {
        try (Workbook workbook = WorkbookFactory.create(excel.getInputStream())) {
            this.sheet = workbook.getSheetAt(COOP_SHOPS_SHEET_INDEX);
            return parseCoopShopRow();
        } catch (IOException e) {
            throw CustomException.of(ApiResponseCode.UNREADABLE_EXCEL_FILE);
        } catch (EncryptedDocumentException e) {
            throw CustomException.of(ApiResponseCode.ENCRYPTED_EXCEL_FILE);
        } catch (EmptyFileException e) {
            throw CustomException.of(ApiResponseCode.EMPTY_EXCEL_FILE);
        } catch (IllegalStateException e) {
            throw CustomException.of(ApiResponseCode.INVALID_EXCEL_FILE_FORMAT);
        }
    }

    private List<CoopShopRow> parseCoopShopRow() {
        return IntStream.range(COOP_SHOPS_START_ROW_INDEX, MAX_ROW_COUNT)
            .mapToObj(sheet::getRow)
            .filter(Predicate.not(this::isRowEmpty))
            .map(this::createCoopShopRow)
            .toList();
    }

    private boolean isRowEmpty(Row row) {
        return Objects.isNull(row) || IntStream.range(0, MAX_COLUMN_COUNT)
            .mapToObj(row::getCell)
            .allMatch(this::isBlankCell);
    }

    private CoopShopRow createCoopShopRow(Row row) {
        return new CoopShopRow(
            getCellValueWithMerge(row.getCell(COOP_SHOP_NAME_COLUMN_INDEX)),
            getCellValueWithMerge(row.getCell(PHONE_COLUMN_INDEX)),
            getCellValueWithMerge(row.getCell(LOCATION_COLUMN_INDEX)),
            getCellValueWithMerge(row.getCell(REMARK_COLUMN_INDEX)),
            getCellValueWithMerge(row.getCell(TYPE_COLUMN_INDEX)),
            getCellValueWithMerge(row.getCell(DAY_OF_WEEK_COLUMN_INDEX)),
            getCellValueWithMerge(row.getCell(OPEN_TIME_COLUMN_INDEX)),
            getCellValueWithMerge(row.getCell(CLOSE_TIME_COLUMN_INDEX))
        );
    }

    private String getCellValueWithMerge(Cell cell) {
        String value = getCellValue(cell);
        if (value != null) {
            return value;
        }
        return sheet.getMergedRegions().stream()
            .filter(mergedRegion -> mergedRegion.isInRange(cell))
            .findFirst()
            .map(this::getFirstCell)
            .map(this::getCellValue)
            .orElse(null);
    }

    private String getCellValue(Cell cell) {
        try {
            if (isBlankCell(cell)) {
                return null;
            }
            return cell.getStringCellValue();
        } catch (Exception e) {
            throw CustomException.of(
                ApiResponseCode.INVALID_EXCEL_CELL_FORMAT,
                String.format("row index: %d, column index: %d", cell.getRowIndex(), cell.getColumnIndex())
            );
        }
    }

    private boolean isBlankCell(Cell cell) {
        return Objects.isNull(cell) || StringUtils.isBlank(cell.getStringCellValue());
    }

    private Cell getFirstCell(CellRangeAddress mergedRegion) {
        Row firstRow = sheet.getRow(mergedRegion.getFirstRow());
        return firstRow.getCell(mergedRegion.getFirstColumn());
    }
}
