package in.koreatech.koin.admin.coopShop.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.admin.coopShop.dto.AdminCoopShopsResponse;
import in.koreatech.koin.admin.coopShop.dto.AdminCoopShopsResponse.InnerCoopShop;
import in.koreatech.koin.admin.coopShop.dto.AdminCoopShopsResponse.InnerCoopShop.InnerCoopShopInfo;
import in.koreatech.koin.admin.coopShop.model.CoopShopRow;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminCoopShopExcelService {

    private final ExcelParser excelParser;

    public AdminCoopShopsResponse parse(MultipartFile excelFile) {
        List<InnerCoopShop> coopShopResponses = excelParser.parse(excelFile).stream()
            .map(withForwardFill())
            .collect(Collectors.groupingBy(InnerCoopShopInfo::from))
            .entrySet().stream()
            .map(InnerCoopShop::from)
            .toList();
        return new AdminCoopShopsResponse(coopShopResponses);
    }

    private Function<CoopShopRow, CoopShopRow> withForwardFill() {
        Map<String, String> last = new HashMap<>();
        return coopShopRow -> new CoopShopRow(
            fill("name", coopShopRow.coopShopName(), last),
            fill("phone", coopShopRow.phone(), last),
            fill("location", coopShopRow.location(), last),
            fill("remark", coopShopRow.remark(), last),
            coopShopRow.type(),
            coopShopRow.dayOfWeek(),
            coopShopRow.openTime(),
            coopShopRow.closeTime()
        );
    }

    private String fill(String key, String value, Map<String, String> last) {
        if (StringUtils.isBlank(value)) {
            return last.getOrDefault(key, "");
        }
        last.put(key, value);
        return value;
    }
}
