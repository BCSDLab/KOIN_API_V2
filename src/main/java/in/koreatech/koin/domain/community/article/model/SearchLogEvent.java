package in.koreatech.koin.domain.community.article.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SearchLogEvent {
    private final String query;
    private final String ipAddress;
}
