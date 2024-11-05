package in.koreatech.koin.domain.shop.dto.search;

import java.util.List;
import java.util.stream.Stream;

public record RelatedKeyword(
        List<String> keywords
) {
    public static RelatedKeyword from(List<String> menuKeywords, List<String> shopNames) {
        return new RelatedKeyword(
                Stream.concat(menuKeywords.stream(), shopNames.stream())
                .limit(5)
                .toList()
        );
    }
}
