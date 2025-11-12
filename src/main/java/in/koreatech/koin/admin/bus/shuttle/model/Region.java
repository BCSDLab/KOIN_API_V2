package in.koreatech.koin.admin.bus.shuttle.model;

import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Region {

    private String value;

    public static Region of(String value) {
        return new Region(
            ShuttleBusRegion.of(value)
                .name()
        );
    }
}
