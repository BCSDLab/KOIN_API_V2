package in.koreatech.koin.admin.coopShop.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.admin.coopShop.dto.AdminCoopShopsResponse;
import in.koreatech.koin.admin.coopShop.dto.AdminCoopShopsResponse.InnerCoopShop;
import in.koreatech.koin.admin.coopShop.dto.AdminCoopShopsResponse.InnerCoopShop.InnerCoopShopInfo;
import in.koreatech.koin.admin.coopShop.dto.AdminCoopShopsResponse.InnerCoopShop.InnerOperationHour;
import in.koreatech.koin.admin.coopShop.model.CoopShopRow;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminCoopShopExcelService {

    private final ExcelParser excelParser;

    public AdminCoopShopsResponse parse(MultipartFile excelFile) {
        List<CoopShopRow> rawCoopShops = excelParser.parse(excelFile);
        List<InnerCoopShop> coopShopResponses = rawCoopShops.stream()
            .collect(Collectors.groupingBy(rawCoopShop -> new InnerCoopShopInfo(
                rawCoopShop.coopShopName(),
                rawCoopShop.phone(),
                rawCoopShop.location(),
                rawCoopShop.remark())
            ))
            .entrySet()
            .stream()
            .map(entry -> new InnerCoopShop(
                    entry.getKey(),
                    entry.getValue().stream().map(rawCoopShop -> new InnerOperationHour(
                        rawCoopShop.type(),
                        rawCoopShop.dayOfWeek(),
                        rawCoopShop.openTime(),
                        rawCoopShop.closeTime()
                    )).toList()
                )
            ).toList();
        return new AdminCoopShopsResponse(coopShopResponses);
    }
}
