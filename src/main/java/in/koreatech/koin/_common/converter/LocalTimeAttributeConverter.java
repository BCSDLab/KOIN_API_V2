package in.koreatech.koin._common.converter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LocalTimeAttributeConverter implements AttributeConverter<LocalTime, String> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public String convertToDatabaseColumn(LocalTime localTime) {
        if (localTime == null)
            return null;
        return localTime.format(formatter);
    }

    @Override
    public LocalTime convertToEntityAttribute(String dbData) {
        if (dbData == null)
            return null;
        return LocalTime.parse(dbData, formatter);
    }
}
