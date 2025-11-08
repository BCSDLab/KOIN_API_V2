package in.koreatech.koin.admin.bus.commuting.extractor;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Component;

import in.koreatech.koin.admin.bus.commuting.enums.BusDirection;
import in.koreatech.koin.admin.bus.commuting.model.CommutingBusExcelMetaData;
import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;

@Component
public class AdminCommutingBusExcelMetaDataExtractor {

    private static final Integer BUS_DIRECTION_ROW_NUMBER = 0;
    private static final Integer BUS_DIRECTION_CELL_NUMBER = 1;

    private static final Integer REGION_ROW_NUMBER = 1;
    private static final Integer REGION_CELL_NUMBER = 1;

    private static final Integer ROUTE_TYPE_ROW_NUMBER = 2;
    private static final Integer ROUTE_TYPE_CELL_NUMBER = 1;

    private static final Integer ROUTE_NAME_ROW_NUMBER = 3;
    private static final Integer ROUTE_NAME_CELL_NUMBER = 1;

    private static final Integer SUB_NAME_ROW_NUMBER = 4;
    private static final Integer SUB_NAME_CELL_NUMBER = 1;

    public CommutingBusExcelMetaData extract(Sheet sheet) {
        BusDirection busDirection = parseBusDirection(sheet);
        ShuttleBusRegion busRegion = parseBusRegion(sheet);
        ShuttleRouteType routeType = parseRouteType(sheet);
        String routeName = parseRouteName(sheet);
        String subName = parseSubName(sheet);
        return CommutingBusExcelMetaData.from(busDirection, busRegion, routeType, routeName, subName);
    }

    private BusDirection parseBusDirection(Sheet sheet) {
        Row subNameRow = sheet.getRow(BUS_DIRECTION_ROW_NUMBER);
        String busDirection = getCellValueAsString(subNameRow, BUS_DIRECTION_CELL_NUMBER);
        return BusDirection.of(busDirection);
    }

    private ShuttleBusRegion parseBusRegion(Sheet sheet) {
        Row regionRow = sheet.getRow(REGION_ROW_NUMBER);
        String region = getCellValueAsString(regionRow, REGION_CELL_NUMBER);
        return ShuttleBusRegion.of(region);
    }

    private ShuttleRouteType parseRouteType(Sheet sheet) {
        Row routeTypeRow = sheet.getRow(ROUTE_TYPE_ROW_NUMBER);
        String routeType = getCellValueAsString(routeTypeRow, ROUTE_TYPE_CELL_NUMBER);
        return ShuttleRouteType.of(routeType);
    }

    private String parseRouteName(Sheet sheet) {
        Row routeNameRow = sheet.getRow(ROUTE_NAME_ROW_NUMBER);
        return getCellValueAsString(routeNameRow, ROUTE_NAME_CELL_NUMBER);
    }

    private String parseSubName(Sheet sheet) {
        Row subNameRow = sheet.getRow(SUB_NAME_ROW_NUMBER);
        return getCellValueAsString(subNameRow, SUB_NAME_CELL_NUMBER);
    }

    private String getCellValueAsString(Row row, int cellNumber) {
        if (row == null) {
            return "";
        }
        Cell cell = row.getCell(cellNumber);
        return cell != null ? cell.getStringCellValue() : "";
    }
}
