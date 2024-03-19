package in.koreatech.koin.global.domain.upload;

import java.util.Arrays;

import org.springframework.core.convert.converter.Converter;

public class ImageUploadDomainEnumConverter implements Converter<String, ImageUploadDomain> {

    @Override
    public ImageUploadDomain convert(String source) {
        return Arrays.stream(ImageUploadDomain.values())
            .filter(it -> it.name().equalsIgnoreCase(source))
            .findAny()
            .orElseThrow(() -> ImageUploadDomainNotFoundException.withDetail("source: " + source));
    }
}
