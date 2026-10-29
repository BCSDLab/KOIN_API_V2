package in.koreatech.koin.admin.bus.shuttle.extractor;

import static in.koreatech.koin.admin.bus.shuttle.model.ShuttleBusTimetable.NodeInfo;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.util.StringUtils;

import in.koreatech.koin.admin.bus.shuttle.util.ExcelRangeUtil;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ShuttleBusNodeInfoExtractor {

    private final Sheet sheet;

    private static final int START_BUS_STOP_ROW = 5;
    private static final int START_BUS_STOP_COL = 0;

    public List<NodeInfo> extractNodeInfos() {
        List<NodeInfo> nodeInfos = new ArrayList<>();
        List<Integer> routeColumns = ExcelRangeUtil.findContiguousRouteColumns(sheet, 3, 1);
        List<Integer> stopRows = ExcelRangeUtil.findContiguousStopRows(
            sheet, START_BUS_STOP_ROW, START_BUS_STOP_COL, routeColumns
        );

        for (Integer rowNum : stopRows) {
            Row row = sheet.getRow(rowNum);

            Cell cell = row.getCell(START_BUS_STOP_COL);
            String nameWithDetail = (cell == null) ? "" : PoiCellExtractor.extractStringValue(cell);

            if (!StringUtils.hasText(nameWithDetail)) {
                throw ExcelRangeUtil.invalidTemplate("정류소 이름이 없습니다: " + rowNum);
            }

            nameWithDetail = nameWithDetail.trim();

            nodeInfos.add(NodeInfo.of(nameWithDetail));
        }

        return nodeInfos;
    }
}
