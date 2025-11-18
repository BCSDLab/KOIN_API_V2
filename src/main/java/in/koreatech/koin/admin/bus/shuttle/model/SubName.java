package in.koreatech.koin.admin.bus.shuttle.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class SubName {

    private String name;

    public static SubName of(String name) {
        return new SubName(name);
    }
}
