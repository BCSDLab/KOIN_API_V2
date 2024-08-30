package in.koreatech.koin.global.config;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class LocalDateAttributeConverter implements AttributeConverter<LocalDate, String> {

    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd")
        .optionalStart()
        .appendPattern(" HH:mm:ss")
        .optionalEnd()
        .toFormatter();

    @Override
    public String convertToDatabaseColumn(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return localDate.format(formatter);
    }

    @Override
    public LocalDate convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return LocalDate.parse(dbData, formatter);
    }
}
