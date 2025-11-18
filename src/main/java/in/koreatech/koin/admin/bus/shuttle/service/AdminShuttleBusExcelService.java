package in.koreatech.koin.admin.bus.shuttle.service;

import static in.koreatech.koin.admin.bus.shuttle.model.ShuttleBusTimetable.NodeInfo;
import static in.koreatech.koin.admin.bus.shuttle.model.ShuttleBusTimetable.RouteInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.admin.bus.shuttle.dto.response.AdminShuttleBusTimetableResponse;
import in.koreatech.koin.admin.bus.shuttle.model.RouteName;
import in.koreatech.koin.admin.bus.shuttle.model.RouteType;
import in.koreatech.koin.admin.bus.shuttle.model.ShuttleBusTimetable;
import in.koreatech.koin.admin.bus.shuttle.model.SubName;
import in.koreatech.koin.admin.bus.shuttle.extractor.ShuttleBusMetaDataExtractor;
import in.koreatech.koin.admin.bus.shuttle.extractor.ShuttleBusNodeInfoExtractor;
import in.koreatech.koin.admin.bus.shuttle.extractor.ShuttleBusRouteInfoExtractor;
import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

@Service
@Transactional(readOnly = true)
public class AdminShuttleBusExcelService {

    public List<AdminShuttleBusTimetableResponse> previewShuttleBusTimetable(MultipartFile file) {
        try (
            InputStream inputStream = file.getInputStream();
            Workbook workbook = WorkbookFactory.create(inputStream)
        ) {
            return extractShuttleBusTimetableData(workbook);
        } catch (IOException e) {
            throw CustomException.of(ApiResponseCode.INVALID_EXCEL_FILE_TYPE);
        }
    }

    private List<AdminShuttleBusTimetableResponse> extractShuttleBusTimetableData(Workbook workBook) {
        List<ShuttleBusTimetable> shuttleBusTimetables = new ArrayList<>();

        for (Sheet sheet : workBook) {
            List<NodeInfo> nodeInfos = ShuttleBusNodeInfoExtractor.getNodeInfos(sheet);
            List<RouteInfo> routeInfos = ShuttleBusRouteInfoExtractor.getRouteInfos(sheet);

            RouteName routeName = ShuttleBusMetaDataExtractor.getRouteNameFromSheet(sheet);
            SubName subName = ShuttleBusMetaDataExtractor.getSubNameFromSheet(sheet);
            ShuttleBusRegion region = ShuttleBusMetaDataExtractor.getRegionFromSheet(sheet);
            RouteType routeType = ShuttleBusMetaDataExtractor.getRouteTypeFromSheet(sheet);

            shuttleBusTimetables.add(
                ShuttleBusTimetable.from(nodeInfos, routeInfos, region, routeName, subName, routeType)
            );
        }

        return shuttleBusTimetables.stream()
            .map(AdminShuttleBusTimetableResponse::from)
            .toList();
    }
}
