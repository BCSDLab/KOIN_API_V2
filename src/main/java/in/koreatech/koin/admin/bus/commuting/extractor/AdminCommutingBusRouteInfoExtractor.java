package in.koreatech.koin.admin.bus.commuting.extractor;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Component;

import in.koreatech.koin.admin.bus.commuting.model.RouteInfo;
import in.koreatech.koin.admin.bus.commuting.model.RouteInfos;

@Component
public class AdminCommutingBusRouteInfoExtractor {

    private static final Integer NORTH_CELL_NUMBER = 1;
    private static final Integer SOUTH_CELL_NUMBER = 2;

    public RouteInfos extract(Sheet sheet, Integer startRowIndex) {
        Row commutingBusNameRow = sheet.getRow(startRowIndex);
        String northRouteInfoName = getCellValueAsString(commutingBusNameRow, NORTH_CELL_NUMBER);
        String southRouteInfoName = getCellValueAsString(commutingBusNameRow, SOUTH_CELL_NUMBER);
        return RouteInfos.from(RouteInfo.of(northRouteInfoName), RouteInfo.of(southRouteInfoName));
    }

    private String getCellValueAsString(Row row, int cellNumber) {
        if (row == null) {
            return "";
        }
        Cell cell = row.getCell(cellNumber);
        return cell != null ? cell.getStringCellValue() : "";
    }
}
