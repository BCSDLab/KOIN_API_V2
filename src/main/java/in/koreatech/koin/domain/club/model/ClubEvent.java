package in.koreatech.koin.domain.club.model;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import in.koreatech.koin._common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "club_event")
@NoArgsConstructor(access = PROTECTED)
public class ClubEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @NotNull
    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Size(max = 255)
    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls;

    @NotNull
    @Column(name = "start_date", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime startDate;

    @NotNull
    @Column(name = "end_date", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime endDate;

    @NotNull
    @Column(name = "introduce", nullable = false, length = 70)
    private String introduce;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @NotNull
    @Column(name = "notified_before_one_hour", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean notifiedBeforeOneHour = false;

    @Builder
    private ClubEvent(
        Club club,
        String name,
        String imageUrls,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String introduce,
        String content,
        Boolean notifiedBeforeOneHour
    ) {
        this.club = club;
        this.name = name;
        this.imageUrls = imageUrls;
        this.startDate = startDate;
        this.endDate = endDate;
        this.introduce = introduce;
        this.content = content;
        this.notifiedBeforeOneHour = notifiedBeforeOneHour;
    }

    public void modifyClubEvent(
        String name,
        String imageUrls,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String introduce,
        String content
    ) {
        this.name = name;
        this.imageUrls = imageUrls;
        this.startDate = startDate;
        this.endDate = endDate;
        this.introduce = introduce;
        this.content = content;
    }

    public void markAsNotifiedBeforeOneHour() {
        this.notifiedBeforeOneHour = true;
    }
}
