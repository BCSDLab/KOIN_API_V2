package in.koreatech.koin.admin.bus.shuttle.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class NodeInfo {

    private String name;
    private String detail;

    public static NodeInfo of(String name, String detail) {
        return new NodeInfo(name, detail);
    }
}
