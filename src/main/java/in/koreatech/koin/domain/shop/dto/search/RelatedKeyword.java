package in.koreatech.koin.domain.shop.dto.search;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public record RelatedKeyword(
        Set<InnerKeyword> keywords
) {
    public static RelatedKeyword of(List<InnerKeyword> menuKeywords, List<InnerKeyword> shopKeywords) {
        Set<InnerKeyword> keywords = new TreeSet<>(shopKeywords);
        keywords.addAll(menuKeywords);
        return new RelatedKeyword(
                keywords.stream()
                        .filter(innerKeyword -> !innerKeyword.shopIds.isEmpty() || innerKeyword.shopId != null)
                        .limit(5)
                        .collect(Collectors.toCollection(TreeSet::new))
        );
    }

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
