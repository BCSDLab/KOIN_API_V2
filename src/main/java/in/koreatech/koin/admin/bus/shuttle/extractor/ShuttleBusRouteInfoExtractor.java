package in.koreatech.koin.admin.bus.shuttle.extractor;

import static in.koreatech.koin.admin.bus.shuttle.model.ShuttleBusTimetable.RouteInfo;
import static in.koreatech.koin.admin.bus.shuttle.model.ShuttleBusTimetable.RouteInfo.InnerNameDetail;
import static in.koreatech.koin.admin.bus.shuttle.model.ShuttleBusTimetable.RouteInfo.from;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.util.StringUtils;

import in.koreatech.koin.admin.bus.shuttle.enums.RunningDays;
import in.koreatech.koin.admin.bus.shuttle.model.ArrivalTime;
import in.koreatech.koin.admin.bus.shuttle.util.ExcelRangeUtil;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ShuttleBusRouteInfoExtractor {

    private final Sheet sheet;

    private static final int START_HEADER_ROW = 3;
    private static final int START_DETAIL_ROW = 4;
    private static final int START_TIME_DATA_ROW = 5;

    private static final int START_COL = 1;

    public List<RouteInfo> extractRouteInfos() {
        List<Integer> routeColumns = ExcelRangeUtil.findContiguousRouteColumns(sheet, START_HEADER_ROW, START_COL);
        List<Integer> stopRows = ExcelRangeUtil.findContiguousStopRows(sheet, START_TIME_DATA_ROW, 0, routeColumns);
        Row detailRow = sheet.getRow(START_DETAIL_ROW);
        ExcelRangeUtil.requireRow(sheet, START_DETAIL_ROW, "회차 세부 정보");
        ExcelRangeUtil.rejectValuesAfterRouteColumns(detailRow, routeColumns.get(routeColumns.size() - 1),
            START_DETAIL_ROW);

        List<InnerNameDetail> innerNameDetails = extractRouteNameDetails(routeColumns, detailRow);

        List<RunningDays> runningDays = innerNameDetails.stream()
            .map(RunningDays::from)
            .toList();

        List<ArrivalTime> arrivalTimes = extractArrivalTimes(routeColumns, stopRows);

        List<RouteInfo> routeInfos = new ArrayList<>();
        for (int i = 0; i < innerNameDetails.size(); i++) {
            routeInfos.add(from(innerNameDetails.get(i), runningDays.get(i), arrivalTimes.get(i)));
        }
        return List.copyOf(routeInfos);
    }

    private List<InnerNameDetail> extractRouteNameDetails(List<Integer> routeColumns, Row detailRow) {
        List<InnerNameDetail> innerNameDetails = new ArrayList<>();

        Row headerRow = sheet.getRow(START_HEADER_ROW);
        for (Integer col : routeColumns) {
            Cell nameCell = headerRow.getCell(col);

            String name = PoiCellExtractor.extractStringValue(nameCell);

            Cell detailCell = detailRow.getCell(col);
            String detail = detailCell == null ? null : PoiCellExtractor.extractStringValue(detailCell);
            if (!StringUtils.hasText(detail)) {
                detail = null;
            }

            innerNameDetails.add(InnerNameDetail.of(name, detail));
        }

        return innerNameDetails;
    }

    private List<ArrivalTime> extractArrivalTimes(List<Integer> routeColumns, List<Integer> stopRows) {
        List<ArrivalTime> arrivalTimes = new ArrayList<>();

        for (Integer colNum : routeColumns) {
            List<String> times = new ArrayList<>();

            for (Integer rowNum : stopRows) {
                Row row = sheet.getRow(rowNum);

                Cell cell = row.getCell(colNum);
                String strTime = PoiCellExtractor.extractStringValue(cell);

                if (!StringUtils.hasText(strTime)) {
                    times.add(null);
                } else {
                    times.add(strTime);
                }
            }

            arrivalTimes.add(ArrivalTime.of(times));
        }

        return arrivalTimes;
    }
}
