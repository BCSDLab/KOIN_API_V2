package in.koreatech.koin.common.converter;

import java.util.HexFormat;

import org.springframework.util.StringUtils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class HashAttributeConverter implements AttributeConverter<String, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(String attribute) {
        if (!StringUtils.hasText(attribute)) {
            return new byte[0];
        }
        return HexFormat.of().parseHex(attribute);
    }

    @Override
    public String convertToEntityAttribute(byte[] dbData) {
        if (dbData == null) {
            return null;
        }
        return HexFormat.of().formatHex(dbData);
    }
}
