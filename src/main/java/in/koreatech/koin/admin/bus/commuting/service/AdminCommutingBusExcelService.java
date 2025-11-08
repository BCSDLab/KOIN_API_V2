package in.koreatech.koin.admin.bus.commuting.service;

import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_SHUTTLE_ROUTE_TYPE;

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
import in.koreatech.koin.admin.bus.commuting.model.CommutingBusExcelMetaData;
import in.koreatech.koin.admin.bus.commuting.model.CommutingBusNodeInfoRowIndex;
import in.koreatech.koin.admin.bus.commuting.model.NodeInfos;
import in.koreatech.koin.admin.bus.commuting.model.RouteInfo;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminCommutingBusExcelService {

    private static final Integer NORTH_CELL_NUMBER = 1;
    private static final Integer SOUTH_CELL_NUMBER = 2;

    private static final Integer NODE_INFO_NAME_CELL_NUMBER = 0;

    private static final String NODE_INFO_END_POINT = "대학(본교)";

    private final AdminCommutingBusExcelMetaDataExtractor commutingBusExcelMetaDataExtractor;
    private final AdminCommutingBusNodeInfoRowIndexExtractor nodeInfoRowIndexExtractor;

    public List<AdminCommutingBusResponse> parseCommutingBusExcel(MultipartFile commutingBusExcelFile) throws
        IOException {
        List<AdminCommutingBusResponse> responses = new ArrayList<>();

        try (InputStream inputStream = commutingBusExcelFile.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)
        ) {
            for (Sheet sheet : workbook) {
                AdminCommutingBusResponse response = parseSheet(sheet);
                responses.add(response);
            }
        }

        return responses;
    }

    private AdminCommutingBusResponse parseSheet(Sheet sheet) {
        CommutingBusExcelMetaData commutingBusExcelMetaData = commutingBusExcelMetaDataExtractor.extract(sheet);
        if (commutingBusExcelMetaData.routeType().isNotCommuting()) {
            throw CustomException.of(INVALID_SHUTTLE_ROUTE_TYPE,
                "shuttleRouteType: " + commutingBusExcelMetaData.routeType().name());
        }

        CommutingBusNodeInfoRowIndex commutingBusNodeInfoRowIndex = nodeInfoRowIndexExtractor.extract(sheet);

        Row commutingBusNameRow = sheet.getRow(commutingBusNodeInfoRowIndex.startRowIndex());
        String commutingBusNorthName = getCellValueAsString(commutingBusNameRow, NORTH_CELL_NUMBER);
        String commutingBusSouthName = getCellValueAsString(commutingBusNameRow, SOUTH_CELL_NUMBER);

        RouteInfo commutingBusNorthRouteInfo = new RouteInfo(commutingBusNorthName);
        RouteInfo commutingBusSouthRouteInfo = new RouteInfo(commutingBusSouthName);
        NodeInfos nodeInfos = new NodeInfos();

        readNodeInfoRow(
            sheet,
            commutingBusNodeInfoRowIndex.startRowIndex(),
            commutingBusNodeInfoRowIndex.endRowIndex(),
            commutingBusExcelMetaData.busDirection(),
            nodeInfos,
            commutingBusNorthRouteInfo, commutingBusSouthRouteInfo
        );

        List<RouteInfo> routeInfos = new ArrayList<>();
        if (commutingBusExcelMetaData.busDirection().isNotSouth()) {
            routeInfos.add(commutingBusNorthRouteInfo);
        }
        if (commutingBusExcelMetaData.busDirection().isNotNorth()) {
            routeInfos.add(commutingBusSouthRouteInfo);
        }

        return AdminCommutingBusResponse.of(
            commutingBusExcelMetaData.busRegion().getLabel(),
            commutingBusExcelMetaData.routeType().getLabel(),
            commutingBusExcelMetaData.routeName(),
            commutingBusExcelMetaData.routeSubName(),
            nodeInfos.getNodeInfos(),
            routeInfos
        );
    }

    private void readNodeInfoRow(
        Sheet sheet,
        int startRowIndex,
        int endRowIndex,
        BusDirection busDirection,
        NodeInfos nodeInfos,
        RouteInfo northRouteInfo,
        RouteInfo southRouteInfo
    ) {
        List<Integer> range = busDirection.isSouth() ? getSouthBusNodeInfoRowRange(startRowIndex, endRowIndex) :
            getNotSouthBusNodeInfoRowRange(startRowIndex, endRowIndex);

        for (int index : range) {
            Row nodeInfoRow = sheet.getRow(index);
            if (nodeInfoRow == null) {
                continue;
            }

            String nodeInfoName = getCellValueAsString(nodeInfoRow, NODE_INFO_NAME_CELL_NUMBER);
            if (StringUtils.isBlank(nodeInfoName)) {
                continue;
            }

            nodeInfos.addNodeInfo(nodeInfoName);

            String northTime = getCellValueAsString(nodeInfoRow, NORTH_CELL_NUMBER);
            String southTime = getCellValueAsString(nodeInfoRow, SOUTH_CELL_NUMBER);
            northRouteInfo.addArrivalTime(new ArrivalTime(northTime));
            southRouteInfo.addArrivalTime(new ArrivalTime(southTime));

            if (busDirection.isNotSouth() && nodeInfoName.contains(NODE_INFO_END_POINT)) {
                break;
            }
        }
    }

    private List<Integer> getSouthBusNodeInfoRowRange(int start, int end) {
        List<Integer> range = new ArrayList<>();
        for (int index = start; index > end; index--) {
            range.add(index);
        }
        return range;
    }

    private List<Integer> getNotSouthBusNodeInfoRowRange(int start, int end) {
        List<Integer> range = new ArrayList<>();
        for (int index = start; index <= end; index++) {
            range.add(index);
        }
        return range;
    }

    private String getCellValueAsString(Row row, int cellNumber) {
        Cell cell = row.getCell(cellNumber);
        return cell != null ? cell.getStringCellValue() : "";
    }
}
