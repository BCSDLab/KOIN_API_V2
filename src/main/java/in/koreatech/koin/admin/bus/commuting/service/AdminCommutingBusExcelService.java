package in.koreatech.koin.admin.bus.commuting.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.admin.bus.commuting.dto.AdminCommutingBusResponse;
import in.koreatech.koin.admin.bus.commuting.extractor.AdminCommutingBusDateExtractor;
import in.koreatech.koin.admin.bus.commuting.extractor.AdminCommutingBusExcelMetaDataExtractor;
import in.koreatech.koin.admin.bus.commuting.extractor.AdminCommutingBusNodeInfoRowIndexExtractor;
import in.koreatech.koin.admin.bus.commuting.extractor.AdminCommutingBusRouteInfoExtractor;
import in.koreatech.koin.admin.bus.commuting.model.CommutingBusData;
import in.koreatech.koin.admin.bus.commuting.model.CommutingBusExcelMetaData;
import in.koreatech.koin.admin.bus.commuting.model.CommutingBusNodeInfoRowIndex;
import in.koreatech.koin.admin.bus.commuting.model.RouteInfos;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminCommutingBusExcelService {

    private final AdminCommutingBusExcelMetaDataExtractor excelMetaDataExtractor;
    private final AdminCommutingBusNodeInfoRowIndexExtractor nodeInfoRowIndexExtractor;
    private final AdminCommutingBusRouteInfoExtractor routeInfoExtractor;
    private final AdminCommutingBusDateExtractor commutingBusDateExtractor;

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
        CommutingBusExcelMetaData excelMetaData = excelMetaDataExtractor.extract(sheet);
        CommutingBusNodeInfoRowIndex nodeInfoRowIndex = nodeInfoRowIndexExtractor.extract(sheet);
        RouteInfos routeInfos = routeInfoExtractor.extract(sheet, nodeInfoRowIndex.startRowIndex());
        CommutingBusData commutingBusData = commutingBusDateExtractor.extract(
            sheet,
            routeInfos,
            nodeInfoRowIndex,
            excelMetaData.busDirection()
        );

        return AdminCommutingBusResponse.of(
            excelMetaData.busRegion().getLabel(),
            excelMetaData.routeType().getLabel(),
            excelMetaData.routeName(),
            excelMetaData.routeSubName(),
            commutingBusData.nodeInfos().getNodeInfos(),
            commutingBusData.routeInfos()
        );
    }
}
