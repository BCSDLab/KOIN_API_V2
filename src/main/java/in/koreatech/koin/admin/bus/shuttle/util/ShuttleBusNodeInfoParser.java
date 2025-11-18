package in.koreatech.koin.admin.bus.shuttle.util;

import static in.koreatech.koin.admin.bus.shuttle.model.ShuttleBusTimetable.NodeInfo;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.util.StringUtils;

public class ShuttleBusNodeInfoParser {

    private static final int START_BUS_STOP_ROW = 5;
    private static final int START_BUS_STOP_COL = 0;

    public static List<NodeInfo> getNodeInfos(Sheet sheet) {
        List<NodeInfo> nodeInfos = new ArrayList<>();

        for (int i = START_BUS_STOP_ROW; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            if (row == null) {
                break;
            }

            Cell cell = row.getCell(START_BUS_STOP_COL);
            String nameWithDetail = ExcelStringUtil.getCellValueToString(cell);

            if (cell == null || !StringUtils.hasText(nameWithDetail)) {
                break;
            }

            nameWithDetail = nameWithDetail.trim();

            String name = ExcelStringUtil.extractNameWithoutBrackets(nameWithDetail);
            String detail = ExcelStringUtil.extractDetailFromBrackets(nameWithDetail);

            nodeInfos.add(NodeInfo.of(name, detail));
        }

        return nodeInfos;
    }
}
