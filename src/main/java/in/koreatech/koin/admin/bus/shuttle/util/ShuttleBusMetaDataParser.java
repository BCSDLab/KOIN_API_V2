    package in.koreatech.koin.admin.bus.shuttle.util;

import org.apache.poi.ss.usermodel.Sheet;

import in.koreatech.koin.admin.bus.shuttle.model.Region;
import in.koreatech.koin.admin.bus.shuttle.model.RouteName;
import in.koreatech.koin.admin.bus.shuttle.model.RouteType;

public class ShuttleBusMetaDataParser {

    private static final int REGION_ROW = 0;
    private static final int REGION_COL = 1;

    private static final int ROUTE_TYPE_ROW = 1;
    private static final int ROUTE_TYPE_COL = 1;

    public static Region getRegionFromSheet(Sheet sheet) {
        return Region.of(ExcelCellHelper.getCellValue(sheet, REGION_ROW, REGION_COL));
    }

    public static RouteType getRouteTypeFromSheet(Sheet sheet) {
        return RouteType.of(ExcelCellHelper.getCellValue(sheet, ROUTE_TYPE_ROW, ROUTE_TYPE_COL));
    }

    public static RouteName getRouteNameFromSheet(Sheet sheet) {
        String sheetName = sheet.getSheetName();

        String routeName = ExcelStringUtil.extractNameWithoutBrackets(sheetName);
        String subName = ExcelStringUtil.extractDetailFromBrackets(sheetName);

        return RouteName.of(routeName, subName);
    }
}

