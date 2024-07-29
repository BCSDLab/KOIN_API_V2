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
        int totalRatio = setChildsRange(childApis);

        return ParentApi.builder()
            .childApis(childApis)
            .totalRatio(totalRatio)
            .build();
    }

    private static int setChildsRange(List<ChildApi> childApis) {
        int total = 0;
        for (var subApi : childApis) {
            total += subApi.setRange(total);
        }
        return total;
    }
}
