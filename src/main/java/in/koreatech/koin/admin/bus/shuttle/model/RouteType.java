package in.koreatech.koin.admin.bus.shuttle.model;

import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RouteType {

    private String value;

    public static RouteType of(String value) {
        return new RouteType(
            ShuttleRouteType.of(value)
                .getLabel()
        );
    }
}
