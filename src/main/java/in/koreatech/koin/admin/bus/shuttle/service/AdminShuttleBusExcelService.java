package in.koreatech.koin.admin.bus.shuttle.service;

import static in.koreatech.koin.admin.bus.shuttle.dto.response.AdminShuttleBusTimetableResponse.InnerAdminShuttleBusTimetableResponse;

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
import in.koreatech.koin.admin.bus.shuttle.extractor.ShuttleBusMetaDataExtractor;
import in.koreatech.koin.admin.bus.shuttle.extractor.ShuttleBusNodeInfoExtractor;
import in.koreatech.koin.admin.bus.shuttle.extractor.ShuttleBusRouteInfoExtractor;
import in.koreatech.koin.admin.bus.shuttle.model.RouteName;
import in.koreatech.koin.admin.bus.shuttle.model.RouteType;
import in.koreatech.koin.admin.bus.shuttle.model.ShuttleBusTimetable;
import in.koreatech.koin.admin.bus.shuttle.model.ShuttleBusTimetable.NodeInfo;
import in.koreatech.koin.admin.bus.shuttle.model.ShuttleBusTimetable.RouteInfo;
import in.koreatech.koin.admin.bus.shuttle.model.SubName;
import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

@Service
@Transactional(readOnly = true)
public class AdminShuttleBusExcelService {

    public AdminShuttleBusTimetableResponse previewShuttleBusTimetable(MultipartFile file) {
        try (
            InputStream inputStream = file.getInputStream();
            Workbook workbook = WorkbookFactory.create(inputStream)
        ) {
            return extractShuttleBusTimetableData(workbook);
        } catch (IOException e) {
            throw CustomException.of(ApiResponseCode.INVALID_EXCEL_FILE_TYPE);
        }
    }

    private AdminShuttleBusTimetableResponse extractShuttleBusTimetableData(Workbook workBook) {
        List<ShuttleBusTimetable> shuttleBusTimetables = new ArrayList<>();

        for (Sheet sheet : workBook) {
            ShuttleBusNodeInfoExtractor nodeInfoExtractor = new ShuttleBusNodeInfoExtractor(sheet);
            ShuttleBusRouteInfoExtractor routeInfoExtractor = new ShuttleBusRouteInfoExtractor(sheet);
            ShuttleBusMetaDataExtractor metaDataExtractor = new ShuttleBusMetaDataExtractor(sheet);

            List<NodeInfo> nodeInfos = nodeInfoExtractor.getNodeInfos();
            List<RouteInfo> routeInfos = routeInfoExtractor.getRouteInfos();

            RouteName routeName = metaDataExtractor.getRouteNameFromSheet();
            SubName subName = metaDataExtractor.getSubNameFromSheet();
            ShuttleBusRegion region = metaDataExtractor.getRegionFromSheet();
            RouteType routeType = metaDataExtractor.getRouteTypeFromSheet();

            shuttleBusTimetables.add(
                ShuttleBusTimetable.from(nodeInfos, routeInfos, region, routeName, subName, routeType)
            );
        }

        List<InnerAdminShuttleBusTimetableResponse> innerResponses = shuttleBusTimetables.stream()
            .map(InnerAdminShuttleBusTimetableResponse::from)
            .toList();

        return new AdminShuttleBusTimetableResponse(innerResponses);
    }
}
