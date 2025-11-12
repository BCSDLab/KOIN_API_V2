package in.koreatech.koin.admin.bus.commuting.extractor;

import static in.koreatech.koin.admin.bus.commuting.util.ExcelCellValueExtractor.getCellValueAsString;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Component;

import in.koreatech.koin.admin.bus.commuting.enums.BusDirection;
import in.koreatech.koin.admin.bus.commuting.model.ArrivalTime;
import in.koreatech.koin.admin.bus.commuting.model.CommutingBusData;
import in.koreatech.koin.admin.bus.commuting.model.CommutingBusNodeInfoRowIndex;
import in.koreatech.koin.admin.bus.commuting.model.NodeInfos;
import in.koreatech.koin.admin.bus.commuting.model.RouteInfo;
import in.koreatech.koin.admin.bus.commuting.model.RouteInfos;

@Component
public class AdminCommutingBusDateExtractor {

    private static final Integer NORTH_CELL_NUMBER = 1;
    private static final Integer SOUTH_CELL_NUMBER = 2;

    private static final Integer NODE_INFO_NAME_CELL_NUMBER = 0;
    private static final String NODE_INFO_END_POINT = "대학(본교)";

    public CommutingBusData extract(
        Sheet sheet,
        RouteInfos routeInfos,
        CommutingBusNodeInfoRowIndex nodeInfoRowIndex,
        BusDirection busDirection
    ) {
        NodeInfos nodeInfos = new NodeInfos();

        extractNodeInfosAndArrivalTimes(
            sheet,
            nodeInfoRowIndex.startRowIndex(),
            nodeInfoRowIndex.endRowIndex(),
            busDirection,
            nodeInfos,
            routeInfos.northRouteInfo(),
            routeInfos.southRouteInfo()
        );

        List<RouteInfo> filteredRouteInfos = filterRouteInfosByDirection(
            busDirection,
            routeInfos
        );

        return CommutingBusData.of(nodeInfos, filteredRouteInfos);
    }

    private void extractNodeInfosAndArrivalTimes(
        Sheet sheet,
        int startRowIndex,
        int endRowIndex,
        BusDirection busDirection,
        NodeInfos nodeInfos,
        RouteInfo northRouteInfo,
        RouteInfo southRouteInfo
    ) {
        // 하교의 경우 엑셀을 역순으로 읽어야하기 때문에, 조건문 추가
        List<Integer> rowRange = busDirection.isSouth()
            ? getDescendingRowRange(endRowIndex, startRowIndex)
            : getAscendingRowRange(startRowIndex + 1, endRowIndex);

        for (int rowIndex : rowRange) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            String nodeInfoName = getCellValueAsString(row, NODE_INFO_NAME_CELL_NUMBER);
            if (StringUtils.isBlank(nodeInfoName)) {
                continue;
            }

            nodeInfos.addNodeInfo(nodeInfoName);

            String northTime = getCellValueAsString(row, NORTH_CELL_NUMBER);
            String southTime = getCellValueAsString(row, SOUTH_CELL_NUMBER);
            northRouteInfo.addArrivalTime(ArrivalTime.from(northTime));
            southRouteInfo.addArrivalTime(ArrivalTime.from(southTime));

            if (busDirection.isNotSouth() && nodeInfoName.contains(NODE_INFO_END_POINT)) {
                break;
            }
        }
    }

    // 하교의 경우 하교 데이터만 들어가야하기 때문에 분기처리
    private List<RouteInfo> filterRouteInfosByDirection(
        BusDirection busDirection,
        RouteInfos routeInfos
    ) {
        List<RouteInfo> filteredRouteInfos = new ArrayList<>();

        if (busDirection.isNotSouth()) {
            filteredRouteInfos.add(routeInfos.northRouteInfo());
        }
        if (busDirection.isNotNorth()) {
            filteredRouteInfos.add(routeInfos.southRouteInfo());
        }

        return filteredRouteInfos;
    }

    private List<Integer> getDescendingRowRange(int start, int end) {
        List<Integer> range = new ArrayList<>();
        for (int i = start; i > end; i--) {
            range.add(i);
        }
        return range;
    }

    private List<Integer> getAscendingRowRange(int start, int end) {
        List<Integer> range = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            range.add(i);
        }
        return range;
    }
}

