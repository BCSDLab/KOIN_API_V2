package in.koreatech.koin.admin.bus.shuttle.model;

import in.koreatech.koin.admin.bus.shuttle.util.NameParser;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class SubName {

    private String name;

    public static SubName of(String sheetName) {
        NameParser.ParsedName parsedName = NameParser.parse(sheetName);

        return new SubName(parsedName.detail());
    }
}
