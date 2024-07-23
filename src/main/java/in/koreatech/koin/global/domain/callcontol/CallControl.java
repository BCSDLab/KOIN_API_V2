package in.koreatech.koin.global.domain.callcontol;

import java.util.List;

public record CallControl(
    List<ApiInfo> apiInfos,
    int totalRatio
) {

}
