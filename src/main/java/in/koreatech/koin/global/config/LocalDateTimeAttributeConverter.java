package in.koreatech.koin.global.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LocalDateTimeAttributeConverter implements AttributeConverter<LocalDateTime, String> {

    private final DateTimeFormatter formatterWithMillis = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSS]");
    private final DateTimeFormatter formatterWithTwoMillis = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS");

    @Override
    public String convertToDatabaseColumn(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.format(formatterWithMillis);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        if (dbData.matches(".*\\.\\d{3}$")) {
            return LocalDateTime.parse(dbData, formatterWithMillis);
        } else {
            return LocalDateTime.parse(dbData, formatterWithTwoMillis);
        }
    }
}
