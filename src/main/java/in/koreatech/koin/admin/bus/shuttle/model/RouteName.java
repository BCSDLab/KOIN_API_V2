package in.koreatech.koin.admin.bus.shuttle.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RouteName {

    private String name;

    public static RouteName of(String name) {
        return new RouteName(name);
    }
}
