package in.koreatech.koin.admin.coopShop.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.admin.coopShop.dto.AdminCoopShopsResponse;
import in.koreatech.koin.admin.coopShop.dto.AdminCoopShopsResponse.InnerCoopShop;
import in.koreatech.koin.admin.coopShop.dto.AdminCoopShopsResponse.InnerCoopShop.InnerCoopShopInfo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminCoopShopExcelService {

    private final ExcelParser excelParser;

    public AdminCoopShopsResponse parse(MultipartFile excel) {
        List<InnerCoopShop> coopShopResponses = excelParser.parse(excel).stream()
            .collect(Collectors.groupingBy(
                InnerCoopShopInfo::from,
                LinkedHashMap::new,
                Collectors.toList()))
            .entrySet().stream()
            .map(InnerCoopShop::from)
            .toList();
        return new AdminCoopShopsResponse(coopShopResponses);
    }
}
