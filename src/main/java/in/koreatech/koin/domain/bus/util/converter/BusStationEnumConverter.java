package in.koreatech.koin.domain.bus.util.converter;

import java.util.Arrays;

import org.springframework.core.convert.converter.Converter;

import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.global.domain.upload.exception.ImageUploadDomainNotFoundException;

public class BusStationEnumConverter implements Converter<String, BusStation> {

    @Override
    public BusStation convert(String source) {
        return Arrays.stream(BusStation.values())
            .filter(it -> it.name().equalsIgnoreCase(source))
            .findAny()
            .orElseThrow(() -> ImageUploadDomainNotFoundException.withDetail("source: " + source));
    }
}
