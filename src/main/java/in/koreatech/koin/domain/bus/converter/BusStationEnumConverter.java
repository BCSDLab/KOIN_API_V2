package in.koreatech.koin.domain.bus.converter;

import java.util.Arrays;

import org.springframework.core.convert.converter.Converter;

import in.koreatech.koin.domain.bus.enums.BusStation;
import in.koreatech.koin.infrastructure.s3.exception.ImageUploadDomainNotFoundException;

public class BusStationEnumConverter implements Converter<String, BusStation> {

    @Override
    public BusStation convert(String source) {
        return Arrays.stream(BusStation.values())
            .filter(it -> it.name().equalsIgnoreCase(source))
            .findAny()
            .orElseThrow(() -> ImageUploadDomainNotFoundException.withDetail("source: " + source));
    }
}
