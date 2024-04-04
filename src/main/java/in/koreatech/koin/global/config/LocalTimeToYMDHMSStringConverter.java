package in.koreatech.koin.global.config;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LocalTimeToYMDHMSStringConverter implements AttributeConverter<LocalTime, String> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String convertToDatabaseColumn(LocalTime localTime) {
        if(localTime == null) return null;
        return localTime.format(formatter);
    }

    @Override
    public LocalTime convertToEntityAttribute(String dbData) {
        if(dbData == null) return null;
        return LocalTime.parse(dbData, formatter);
    }
}

