package in.koreatech.koin.admin.bus.shuttle.model;

import in.koreatech.koin.admin.bus.shuttle.parser.NameParser;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RouteName {

    private String name;

    public static RouteName of(String sheetName) {
        NameParser.ParsedName parsedName = NameParser.parse(sheetName);

        return new RouteName(parsedName.name());
    }
}
