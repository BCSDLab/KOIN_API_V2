package in.koreatech.koin.global.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ApiCallInfo {

    private final Map<Class<?>, List<Integer>> apiInfos = new HashMap<>();
    private int totalRatio = 0;

    public void sumRatio(int ratio) {
        totalRatio += ratio;
    }
}
