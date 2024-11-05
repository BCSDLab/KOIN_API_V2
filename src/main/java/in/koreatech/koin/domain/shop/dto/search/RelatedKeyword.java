package in.koreatech.koin.domain.shop.dto.search;

import java.util.List;

public record RelatedKeyword(
        List<String> keywords
) {
    public static RelatedKeyword from(List<String> keywords) {
        return new RelatedKeyword(
                keywords.stream().sorted().limit(5).toList()
        );
    }
}
