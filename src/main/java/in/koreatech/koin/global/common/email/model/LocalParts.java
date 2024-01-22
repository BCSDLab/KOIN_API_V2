package in.koreatech.koin.global.common.email.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LocalParts {

    public static final int BEGIN_INDEX = 0;

    private final String value;

    public static LocalParts from(String fullAddress) {
        return new LocalParts(localPartsFrom(fullAddress));
    }

    private static String localPartsFrom(String fullAddress) {
        return fullAddress.substring(BEGIN_INDEX, Email.getSeparateIndex(fullAddress));
    }
}
