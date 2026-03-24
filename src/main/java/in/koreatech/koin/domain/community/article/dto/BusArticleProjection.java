package in.koreatech.koin.domain.community.article.dto;

import java.time.LocalDateTime;

public class BusArticleProjection {

    private final Integer id;
    private final String title;
    private final LocalDateTime createdAt;

    public BusArticleProjection(Integer id, String title, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
