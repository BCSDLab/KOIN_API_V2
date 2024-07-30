package in.koreatech.koin.global.domain.callcontoller.model;

import java.util.List;

import in.koreatech.koin.global.model.ReflectionUtils;
import lombok.Builder;

@Builder
public record ParentApi(
    List<ChildApi> childApis,
    int totalRatio
) {

    public static ParentApi of(Class<?> parentType) {
        List<ChildApi> childApis = ReflectionUtils.getChildTypes(parentType).stream()
            .map(ChildApi::of)
            .toList();
        int totalRatio = setChildSelectedRange(childApis);

        return ParentApi.builder()
            .childApis(childApis)
            .totalRatio(totalRatio)
            .build();
    }

    private static int setChildSelectedRange(List<ChildApi> childApis) {
        int totalRange = 0;
        for (var subApi : childApis) {
            totalRange += subApi.setRange(totalRange);
        }
        return totalRange;
    }
}
