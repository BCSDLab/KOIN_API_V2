package in.koreatech.koin.admin.bus.shuttle.extractor;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import in.koreatech.koin.admin.bus.shuttle.model.RouteName;
import in.koreatech.koin.admin.bus.shuttle.model.RouteType;
import in.koreatech.koin.admin.bus.shuttle.model.SubName;
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
        Row row = sheet.getRow(REGION_ROW);
        Cell cell = row.getCell(REGION_COL);

        return ShuttleBusRegion.of(PoiCellExtractor.extractStringValue(cell));
    }

    public RouteType extractRouteType() {
        Row row = sheet.getRow(ROUTE_TYPE_ROW);
        Cell cell = row.getCell(ROUTE_TYPE_COL);

        return RouteType.of(PoiCellExtractor.extractStringValue(cell));
    }

    public RouteName extractRouteName() {
        String sheetName = sheet.getSheetName();

        return RouteName.of(sheetName);
    }

    public SubName extractSubName() {
        String sheetName = sheet.getSheetName();

        return SubName.of(sheetName);
    }
}
