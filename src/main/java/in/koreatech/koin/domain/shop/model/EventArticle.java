package in.koreatech.koin.domain.shop.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDate;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "event_articles")
@Where(clause = "is_deleted=0")
@SQLDelete(sql = "UPDATE event_articles SET is_deleted = true WHERE id = ?")
@NoArgsConstructor(access = PROTECTED)
public class EventArticle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @Size(max = 255)
    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    /**
     * 미사용 컬럼
     * TODO: 마이그레이션 종료 후 flyway로 제거
     */
    @Size(max = 50)
    @NotNull
    @Column(name = "event_title", nullable = false)
    private String eventTitle = "";

    @NotNull
    @Column(name = "content", nullable = false)
    private String content;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 미사용 컬럼
     * TODO: 마이그레이션 종료 후 flyway로 제거
     */
    @Size(max = 50)
    @NotNull
    @Column(name = "nickname", nullable = false)
    private String nickname = "";

    @Size(max = 255)
    @Column(name = "thumbnail")
    private String thumbnail;

    @NotNull
    @Column(name = "hit", nullable = false)
    private Integer hit;

    @Size(max = 45)
    @NotNull
    @Column(name = "ip", nullable = false)
    private String ip;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * 미사용 컬럼
     * TODO: 마이그레이션 종료 후 flyway로 제거
     */
    @NotNull
    @Column(name = "comment_count", nullable = false)
    private boolean commentCount = false;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Builder
    private EventArticle(
        Shop shop,
        String title,
        String content,
        User user,
        String thumbnail,
        Integer hit,
        String ip,
        LocalDate startDate,
        LocalDate endDate
    ) {
        this.shop = shop;
        this.title = title;
        this.content = content;
        this.user = user;
        this.thumbnail = thumbnail;
        this.hit = hit;
        this.ip = ip;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void modifyArticle(
        String title,
        String content,
        String thumbnail,
        LocalDate startDate,
        LocalDate endDate
    ) {
        this.title = title;
        this.content = content;
        this.thumbnail = thumbnail;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
