package in.koreatech.koin.domain.bus.util.converter;

import java.util.Arrays;

import org.springframework.core.convert.converter.Converter;

import in.koreatech.koin.domain.bus.model.enums.BusType;
import in.koreatech.koin.global.domain.upload.exception.ImageUploadDomainNotFoundException;

public class BusTypeEnumConverter implements Converter<String, BusType> {

    @Override
    public BusType convert(String source) {
        return Arrays.stream(BusType.values())
            .filter(it -> it.name().equalsIgnoreCase(source))
            .findAny()
            .orElseThrow(() -> ImageUploadDomainNotFoundException.withDetail("source: " + source));
    }
}
