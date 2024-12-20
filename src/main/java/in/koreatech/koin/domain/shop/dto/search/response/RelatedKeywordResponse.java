package in.koreatech.koin.domain.shop.dto.search.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@JsonNaming(value = SnakeCaseStrategy.class)
public record RelatedKeywordResponse(
        Set<InnerKeyword> keywords
) {
    public static RelatedKeywordResponse of(List<InnerKeyword> menuKeywords, List<InnerKeyword> shopKeywords) {
        Set<InnerKeyword> keywords = new TreeSet<>(shopKeywords);
        keywords.addAll(menuKeywords);
        return new RelatedKeywordResponse(
                keywords.stream()
                        .filter(innerKeyword -> !innerKeyword.shopIds.isEmpty() || innerKeyword.shopId != null)
                        .limit(5)
                        .collect(Collectors.toCollection(TreeSet::new))
        );
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerKeyword(
            String keyword,
            List<Integer> shopIds,
            Integer shopId
    ) implements Comparable<InnerKeyword> {

        @Override
        public int compareTo(InnerKeyword other) {
            if (this.shopId != null && other.shopId == null) {
                return -1;
            }
            if (this.shopId == null && other.shopId != null) {
                return 1;
            }
            return this.keyword.compareTo(other.keyword);
        }
    }
}
