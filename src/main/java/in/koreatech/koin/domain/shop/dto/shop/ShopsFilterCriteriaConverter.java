package in.koreatech.koin.domain.shop.dto.shop;

import java.util.Arrays;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ShopsFilterCriteriaConverter implements Converter<String, ShopsFilterCriteria> {

    @Override
    public ShopsFilterCriteria convert(String source) {
        return Arrays.stream(ShopsFilterCriteria.values())
            .filter(criteria -> criteria.name().equalsIgnoreCase(source))
            .findFirst()
            .orElse(null);
    }
}
