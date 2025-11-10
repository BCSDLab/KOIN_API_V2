package in.koreatech.koin.admin.bus.shuttle.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.admin.bus.shuttle.dto.response.AdminShuttleBusTimeTableResponse;
import in.koreatech.koin.admin.bus.shuttle.model.NodeInfo;
import in.koreatech.koin.admin.bus.shuttle.model.Region;
import in.koreatech.koin.admin.bus.shuttle.model.RouteInfo;
import in.koreatech.koin.admin.bus.shuttle.model.RouteName;
import in.koreatech.koin.admin.bus.shuttle.model.RouteType;
import in.koreatech.koin.admin.bus.shuttle.model.ShuttleBusTimeTable;
import in.koreatech.koin.admin.bus.shuttle.util.ShuttleBusMetaDataParser;
import in.koreatech.koin.admin.bus.shuttle.util.ShuttleBusNodeInfoParser;
import in.koreatech.koin.admin.bus.shuttle.util.ShuttleBusRouteInfoParser;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

@Service
@Transactional(readOnly = true)
public class AdminShuttleBusTimeTableService {

    public List<AdminShuttleBusTimeTableResponse> previewShuttleBusTimeTable(MultipartFile file) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            return extractShuttleBusTimeTableData(workbook);
        } catch (IOException e) {
            throw CustomException.of(ApiResponseCode.INVALID_EXCEL_FILE_TYPE);
        }
    }

    private List<AdminShuttleBusTimeTableResponse> extractShuttleBusTimeTableData(XSSFWorkbook workBook) {
        List<ShuttleBusTimeTable> shuttleBusTimeTables = new ArrayList<>();

        for (Sheet sheet : workBook) {
            List<NodeInfo> nodeInfos = ShuttleBusNodeInfoParser.getNodeInfos(sheet);
            List<RouteInfo> routeInfos = ShuttleBusRouteInfoParser.getRouteInfos(sheet);

            RouteName routeName = ShuttleBusMetaDataParser.getRouteNameFromSheet(sheet);
            Region region = ShuttleBusMetaDataParser.getRegionFromSheet(sheet);
            RouteType routeType = ShuttleBusMetaDataParser.getRouteTypeFromSheet(sheet);

            shuttleBusTimeTables.add(ShuttleBusTimeTable.from(nodeInfos, routeInfos, region, routeName, routeType));
        }

        return shuttleBusTimeTables.stream()
            .map(AdminShuttleBusTimeTableResponse::from)
            .toList();
    }
}
