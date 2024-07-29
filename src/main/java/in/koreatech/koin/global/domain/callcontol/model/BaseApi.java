package in.koreatech.koin.global.domain.callcontol.model;

import java.util.List;

import in.koreatech.koin.global.model.ReflectionUtils;
import lombok.Builder;

@Builder
public record BaseApi(
    List<SubApi> subApis,
    int totalRatio
) {

    public static BaseApi of(Class<?> baseApiType) {
        List<SubApi> subApis = ReflectionUtils.getSubTypeBeans(baseApiType).stream()
            .map(SubApi::of)
            .toList();
        int totalRatio = setSubApisRange(subApis);

        return BaseApi.builder()
            .subApis(subApis)
            .totalRatio(totalRatio)
            .build();
    }

    private static int setSubApisRange(List<SubApi> subApis) {
        int total = 0;
        for (var subApi : subApis) {
            total += subApi.setRange(total);
        }
        return total;
    }
}
