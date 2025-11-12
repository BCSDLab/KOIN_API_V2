package in.koreatech.koin.admin.bus.shuttle.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import in.koreatech.koin.admin.bus.shuttle.model.Region;
import in.koreatech.koin.admin.bus.shuttle.model.RouteName;
import in.koreatech.koin.admin.bus.shuttle.model.RouteType;
import in.koreatech.koin.admin.bus.shuttle.model.SubName;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

public class ShuttleBusMetaDataParser {

    private static final int REGION_ROW = 0;
    private static final int REGION_COL = 1;

    private static final int ROUTE_TYPE_ROW = 1;
    private static final int ROUTE_TYPE_COL = 1;

    public static Region getRegionFromSheet(Sheet sheet) {
        return Region.of(getCellValue(sheet, REGION_ROW, REGION_COL));
    }

    public static RouteType getRouteTypeFromSheet(Sheet sheet) {
        return RouteType.of(getCellValue(sheet, ROUTE_TYPE_ROW, ROUTE_TYPE_COL));
    }

    public static RouteName getRouteNameFromSheet(Sheet sheet) {
        String sheetName = sheet.getSheetName();

        String routeName = ExcelStringUtil.extractNameWithoutBrackets(sheetName);

        return RouteName.of(routeName);
    }

    public static SubName getSubNameFromSheet(Sheet sheet) {
        String sheetName = sheet.getSheetName();

        String subName = ExcelStringUtil.extractDetailFromBrackets(sheetName);

        return SubName.of(subName);
    }

    private static String getCellValue(Sheet sheet, int rowIndex, int colIndex) {
        Row row = sheet.getRow(rowIndex);

        if (row == null) {
            throw CustomException.of(ApiResponseCode.INVALID_EXCEL_ROW);
        }

        Cell cell = row.getCell(colIndex);

        if (cell == null) {
            throw CustomException.of(ApiResponseCode.INVALID_EXCEL_COL);
        }

        return cell.toString();
    }
}
