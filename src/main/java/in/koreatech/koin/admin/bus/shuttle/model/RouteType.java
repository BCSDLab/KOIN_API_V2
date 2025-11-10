package in.koreatech.koin.admin.bus.shuttle.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RouteType {

    private String value;

    public static RouteType of(String value) {
        return new RouteType(value);
    }
}