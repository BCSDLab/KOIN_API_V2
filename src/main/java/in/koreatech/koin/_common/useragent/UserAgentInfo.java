package in.koreatech.koin._common.useragent;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserAgentInfo {

    private String model;
    private String type;

    @Builder
    private UserAgentInfo(String model, String type) {
        this.model = model;
        this.type = type;
    }
}
