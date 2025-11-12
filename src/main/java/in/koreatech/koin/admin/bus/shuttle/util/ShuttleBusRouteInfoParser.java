package in.koreatech.koin.admin.bus.shuttle.util;

import static in.koreatech.koin.admin.bus.shuttle.model.ShuttleBusTimeTable.RouteInfo;
import static in.koreatech.koin.admin.bus.shuttle.model.ShuttleBusTimeTable.RouteInfo.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.util.StringUtils;

import in.koreatech.koin.admin.bus.shuttle.enums.RunningDays;
import in.koreatech.koin.admin.bus.shuttle.model.ArrivalTime;

public class ShuttleBusRouteInfoParser {

    private static final int START_HEADER_ROW = 3;
    private static final int START_DETAIL_ROW = 4;
    private static final int START_TIME_DATA_ROW = 5;

    private static final int START_COL = 1;

    public static List<RouteInfo> getRouteInfos(Sheet sheet) {
        List<InnerNameDetail> innerNameDetails = extractRouteNameDetails(sheet);

        List<RunningDays> runningDays = innerNameDetails.stream()
            .map(RunningDays::from)
            .toList();

        List<ArrivalTime> arrivalTimes = extractArrivalTimes(sheet);

        return IntStream.range(0, innerNameDetails.size())
            .mapToObj(i -> from(
                innerNameDetails.get(i),
                runningDays.get(i),
                arrivalTimes.get(i)
            ))
            .toList();
    }

    private static List<InnerNameDetail> extractRouteNameDetails(Sheet sheet) {
        List<InnerNameDetail> innerNameDetails = new ArrayList<>();

        Row headerRow = sheet.getRow(START_HEADER_ROW);
        Row detailRow = sheet.getRow(START_DETAIL_ROW);

        if (headerRow == null || detailRow == null) {
            return innerNameDetails;
        }

        for (int col = START_COL; col <= headerRow.getLastCellNum(); col++) {
            Cell nameCell = headerRow.getCell(col);

            if (nameCell == null || !StringUtils.hasText(nameCell.toString())) {
                break;
            }

            String name = nameCell.getStringCellValue().trim();

            Cell detailCell = detailRow.getCell(col);

            String detail = (detailCell != null && StringUtils.hasText(detailCell.toString()))
                ? detailCell.getStringCellValue().trim()
                : null;

            innerNameDetails.add(InnerNameDetail.of(name, detail));
        }

        return innerNameDetails;
    }

    private static List<ArrivalTime> extractArrivalTimes(Sheet sheet) {
        List<ArrivalTime> arrivalTimes = new ArrayList<>();

        for (int colNum = START_COL;
             colNum < START_COL + ExcelRangeUtil.countUsedColumnsInRow(sheet, START_HEADER_ROW, START_COL);
             colNum++
        ) {
            List<String> times = new ArrayList<>();

            for (int rowNum = START_TIME_DATA_ROW;
                 rowNum < START_TIME_DATA_ROW + ExcelRangeUtil.countUsedRowsInColumn(sheet, START_TIME_DATA_ROW, 0);
                 rowNum++
            ) {
                Row row = sheet.getRow(rowNum);

                if (row == null) {
                    break;
                }

                Cell cell = row.getCell(colNum);

                if (cell == null || !StringUtils.hasText(cell.getStringCellValue())) {
                    times.add(null);
                } else {
                    times.add(cell.getStringCellValue().trim());
                }
            }

            arrivalTimes.add(ArrivalTime.of(times));
        }

        return arrivalTimes;
    }
}
