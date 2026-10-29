package in.koreatech.koin.admin.bus.shuttle.extractor;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import in.koreatech.koin.admin.bus.shuttle.model.RouteName;
import in.koreatech.koin.admin.bus.shuttle.model.RouteType;
import in.koreatech.koin.admin.bus.shuttle.model.SubName;
import in.koreatech.koin.admin.bus.shuttle.util.ExcelRangeUtil;
import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ShuttleBusMetaDataExtractor {

    private final Sheet sheet;

    private static final int REGION_ROW = 0;
    private static final int REGION_COL = 1;

    private static final int ROUTE_TYPE_ROW = 1;
    private static final int ROUTE_TYPE_COL = 1;

    public ShuttleBusRegion extractRegion() {
        return ShuttleBusRegion.of(extractRequiredValue(REGION_ROW, "지역"));
    }

    public RouteType extractRouteType() {
        return RouteType.of(extractRequiredValue(ROUTE_TYPE_ROW, "노선 형태"));
    }

    public RouteName extractRouteName() {
        String sheetName = sheet.getSheetName();

        return RouteName.of(sheetName);
    }

    public SubName extractSubName() {
        String sheetName = sheet.getSheetName();

        return SubName.of(sheetName);
    }

    private String extractRequiredValue(int rowNum, String description) {
        ExcelRangeUtil.requireRow(sheet, rowNum, description);
        int requiredCol = rowNum == ROUTE_TYPE_ROW ? ROUTE_TYPE_COL : REGION_COL;
        Cell cell = sheet.getRow(rowNum).getCell(requiredCol);
        String value = PoiCellExtractor.extractStringValue(cell);
        if (value.isBlank()) {
            throw ExcelRangeUtil.invalidTemplate("필수 셀 값이 없습니다 (" + description + "): " + rowNum + "/" + requiredCol);
        }
        return value;
    }
}
