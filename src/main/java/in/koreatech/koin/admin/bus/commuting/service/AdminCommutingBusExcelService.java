package in.koreatech.koin.admin.bus.commuting.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.admin.bus.commuting.dto.AdminCommutingBusResponse;
import in.koreatech.koin.admin.bus.commuting.enums.BusDirection;
import in.koreatech.koin.admin.bus.commuting.model.ArrivalTime;
import in.koreatech.koin.admin.bus.commuting.model.NodeInfos;
import in.koreatech.koin.admin.bus.commuting.model.RouteInfo;
import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;

@Service
public class AdminCommutingBusExcelService {

    private static final Integer MAX_EXCEL_ROW_NUMBER = 30;

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

    private static final Integer NORTH_CELL_NUMBER = 1;
    private static final Integer SOUTH_CELL_NUMBER = 2;

    private static final Integer NODE_INFO_NAME_CELL_NUMBER = 0;

    private static final String NODE_INFO_START_POINT = "정거장";
    private static final String NODE_INFO_END_POINT = "대학(본교)";

    public List<AdminCommutingBusResponse> parseCommutingBusExcel(MultipartFile commutingBusExcelFile) throws IOException {
        List<AdminCommutingBusResponse> responses = new ArrayList<>();

        try (InputStream inputStream = commutingBusExcelFile.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)
        ) {
            for (Sheet sheet : workbook) {
                BusDirection commutingBusDirection = getCommutingBusDirection(sheet);
                ShuttleBusRegion commutingBusRegion = getCommutingBusRegion(sheet);

                ShuttleRouteType shuttleRouteType = getShuttleRouteType(sheet);
                if (shuttleRouteType.isNotCommuting()) {
                    throw new IllegalStateException();
                }

                String commutingBusRouteName = getCommutingBusRouteName(sheet);
                String commutingBusSubName = getCommutingBusSubName(sheet);

                // node_info가 있는 행 찾기
                int nodeInfoStartRowIndex = findNodeInfoRowIndexByPoint(sheet, NODE_INFO_START_POINT);
                if (nodeInfoStartRowIndex == -1) {
                    throw new IllegalStateException();
                }

                // node_info가 끝나는 행 찾기
                int nodeInfoEndRowIndex = findNodeInfoRowIndexByPoint(sheet, NODE_INFO_END_POINT);
                if (nodeInfoEndRowIndex == -1) {
                    throw new IllegalStateException();
                }

                // 등교, 하교 이름 찾기
                Row commutingBusNameRow = sheet.getRow(nodeInfoStartRowIndex);
                Cell commutingBusNorthNameCell = commutingBusNameRow.getCell(NORTH_CELL_NUMBER);
                Cell commutingBusSouthNameCell = commutingBusNameRow.getCell(SOUTH_CELL_NUMBER);
                String commutingBusNorthName = commutingBusNorthNameCell.getStringCellValue();
                String commutingBusSouthName = commutingBusSouthNameCell.getStringCellValue();

                RouteInfo commutingBusNorthRouteInfo = new RouteInfo(commutingBusNorthName);
                RouteInfo commutingBusSouthRouteInfo = new RouteInfo(commutingBusSouthName);
                NodeInfos nodeInfos = new NodeInfos();

                if (commutingBusDirection.isSouth()) {
                    for (int i = nodeInfoEndRowIndex; i > nodeInfoStartRowIndex; i--) {
                        Row nodeInfoRow = sheet.getRow(i);
                        if (nodeInfoRow == null) {
                            continue;
                        }

                        Cell nodeInfoNameCell = nodeInfoRow.getCell(NODE_INFO_NAME_CELL_NUMBER);
                        if (StringUtils.isBlank(nodeInfoNameCell.getStringCellValue())) {
                            continue;
                        }

                        String nodeInfoName = nodeInfoNameCell.getStringCellValue();
                        nodeInfos.addNodeInfo(nodeInfoName);

                        String commutingBusNorthTime = nodeInfoRow.getCell(NORTH_CELL_NUMBER).getStringCellValue();
                        String commutingBusSouthTime = nodeInfoRow.getCell(SOUTH_CELL_NUMBER).getStringCellValue();
                        commutingBusNorthRouteInfo.addArrivalTime(new ArrivalTime(commutingBusNorthTime));
                        commutingBusSouthRouteInfo.addArrivalTime(new ArrivalTime(commutingBusSouthTime));
                    }
                } else {
                    for (int i = nodeInfoStartRowIndex + 1; i <= nodeInfoEndRowIndex; i++) {
                        Row nodeInfoRow = sheet.getRow(i);
                        if (nodeInfoRow == null) {
                            continue;
                        }

                        Cell nodeInfoNameCell = nodeInfoRow.getCell(NODE_INFO_NAME_CELL_NUMBER);
                        if (StringUtils.isBlank(nodeInfoNameCell.getStringCellValue())) {
                            continue;
                        }

                        String nodeInfoName = nodeInfoNameCell.getStringCellValue();
                        nodeInfos.addNodeInfo(nodeInfoName);

                        String commutingBusNorthTime = nodeInfoRow.getCell(NORTH_CELL_NUMBER).getStringCellValue();
                        String commutingBusSouthTime = nodeInfoRow.getCell(SOUTH_CELL_NUMBER).getStringCellValue();
                        commutingBusNorthRouteInfo.addArrivalTime(new ArrivalTime(commutingBusNorthTime));
                        commutingBusSouthRouteInfo.addArrivalTime(new ArrivalTime(commutingBusSouthTime));

                        if (nodeInfoName.contains(NODE_INFO_END_POINT)) {
                            break;
                        }
                    }
                }

                List<RouteInfo> routeInfos = new ArrayList<>();
                if (commutingBusDirection.isNotSouth()) routeInfos.add(commutingBusNorthRouteInfo);
                if (commutingBusDirection.isNotNorth()) routeInfos.add(commutingBusSouthRouteInfo);

                AdminCommutingBusResponse response = AdminCommutingBusResponse.of(
                    commutingBusRegion.getLabel(),
                    shuttleRouteType.getLabel(),
                    commutingBusRouteName,
                    commutingBusSubName,
                    nodeInfos.getNodeInfos(),
                    routeInfos
                );

                responses.add(response);
            }
        }

        return responses;
    }

    private BusDirection getCommutingBusDirection(Sheet sheet) {
        Row subNameRow = sheet.getRow(BUS_DIRECTION_ROW_NUMBER);
        Cell subNameCell = subNameRow.getCell(BUS_DIRECTION_CELL_NUMBER);
        String busDirection = subNameCell.getStringCellValue();
        return BusDirection.of(busDirection);
    }

    private String getCommutingBusSubName(Sheet sheet) {
        Row subNameRow = sheet.getRow(SUB_NAME_ROW_NUMBER);
        Cell subNameCell = subNameRow.getCell(SUB_NAME_CELL_NUMBER);
        return subNameCell != null ? subNameCell.getStringCellValue() : null;
    }

    private String getCommutingBusRouteName(Sheet sheet) {
        Row routeNameRow = sheet.getRow(ROUTE_NAME_ROW_NUMBER);
        Cell routeNameCell = routeNameRow.getCell(ROUTE_NAME_CELL_NUMBER);
        return routeNameCell.getStringCellValue();
    }

    private ShuttleBusRegion getCommutingBusRegion(Sheet sheet) {
        Row regionRow = sheet.getRow(REGION_ROW_NUMBER);
        Cell regionCell = regionRow.getCell(REGION_CELL_NUMBER);
        String region = regionCell.getStringCellValue();
        return ShuttleBusRegion.of(region);
    }

    private ShuttleRouteType getShuttleRouteType(Sheet sheet) {
        Row routeTypeRow = sheet.getRow(ROUTE_TYPE_ROW_NUMBER);
        Cell routeTypeCell = routeTypeRow.getCell(ROUTE_TYPE_CELL_NUMBER);
        String routeType = routeTypeCell.getStringCellValue();
        return ShuttleRouteType.of(routeType);
    }

    private Integer findNodeInfoRowIndexByPoint(Sheet sheet, String point) {
        for (int index = 0; index <= MAX_EXCEL_ROW_NUMBER; index++) {
            Row row = sheet.getRow(index);
            if (row == null)
                continue;

            Cell firstCell = row.getCell(0);
            if (StringUtils.equals(firstCell.getStringCellValue(), point)) {
                return index;
            }
        }
        return -1;
    }
}
