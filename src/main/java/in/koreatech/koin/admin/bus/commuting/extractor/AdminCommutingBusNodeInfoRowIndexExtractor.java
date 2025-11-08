package in.koreatech.koin.admin.bus.commuting.extractor;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Component;

import in.koreatech.koin.admin.bus.commuting.model.CommutingBusNodeInfoRowIndex;

@Component
public class AdminCommutingBusNodeInfoRowIndexExtractor {

    private static final Integer MAX_EXCEL_ROW_NUMBER = 30;

    private static final String NODE_INFO_START_POINT = "정거장";
    private static final String NODE_INFO_END_POINT = "대학(본교)";

    public CommutingBusNodeInfoRowIndex extract(Sheet sheet) {
        Optional<Integer> nodeInfoStartRowIndex = findNodeInfoRowIndexByPoint(sheet, NODE_INFO_START_POINT);
        Optional<Integer> nodeInfoEndRowIndex = findNodeInfoRowIndexByPoint(sheet, NODE_INFO_END_POINT);
        return CommutingBusNodeInfoRowIndex.from(nodeInfoStartRowIndex, nodeInfoEndRowIndex);
    }

    private Optional<Integer> findNodeInfoRowIndexByPoint(Sheet sheet, String point) {
        for (int index = 0; index <= MAX_EXCEL_ROW_NUMBER; index++) {
            Row row = sheet.getRow(index);
            if (row == null) {
                continue;
            }

            if (StringUtils.equals(getCellValueAsString(row, 0), point)) {
                return Optional.of(index);
            }
        }
        return Optional.empty();
    }

    private String getCellValueAsString(Row row, int cellNumber) {
        if (row == null) {
            return "";
        }
        Cell cell = row.getCell(cellNumber);
        return cell != null ? cell.getStringCellValue() : "";
    }
}
