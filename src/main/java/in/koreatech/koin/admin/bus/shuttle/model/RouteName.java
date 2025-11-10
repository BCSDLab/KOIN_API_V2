package in.koreatech.koin.admin.bus.shuttle.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RouteName {

    private String name;
    private String subName;

    public static RouteName of(String name, String subName) {
        return new RouteName(name, subName);
    }
}
