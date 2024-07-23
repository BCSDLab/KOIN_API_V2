package in.koreatech.koin.global.model;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.Builder;

@Builder
@Component
public class ApiCallManagerRegistry {

    private final Map<String, ApiCallInfo> apiCallManagerMaps = new HashMap<>();

    public ApiCallInfo apiCallInfo(String name, ApiCallInfo apiCallInfo) {
        apiCallManagerMaps.put(name, apiCallInfo);
        return apiCallInfo;
    }

    public ApiCallInfo findByName(String name) {
        return apiCallManagerMaps.get(name);
    }
}
