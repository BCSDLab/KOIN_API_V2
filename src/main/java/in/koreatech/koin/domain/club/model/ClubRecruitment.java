package in.koreatech.koin.domain.club.model;

import static in.koreatech.koin.domain.club.enums.ClubRecruitmentStatus.*;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import in.koreatech.koin._common.model.BaseEntity;
import in.koreatech.koin.domain.club.enums.ClubRecruitmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    schema = "koin",
    name = "club_recruitment",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_club_recruitment_club_id", columnNames = "club_id")
    }
)
@NoArgsConstructor(access = PROTECTED)
public class ClubRecruitment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @NotNull
    @Column(name = "is_always_recruiting", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isAlwaysRecruiting;

    @Size(max = 255)
    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "club_id", nullable = false, updatable = false)
    private Club club;

    @Transient
    private Integer Dday;

    @Transient
    private ClubRecruitmentStatus clubRecruitmentStatus;

    @Builder
    private ClubRecruitment(
        LocalDate startDate,
        LocalDate endDate,
        Boolean isAlwaysRecruiting,
        String imageUrl,
        String content,
        Club club
    ) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.isAlwaysRecruiting = isAlwaysRecruiting;
        this.imageUrl = imageUrl;
        this.content = content;
        this.club = club;
    }

    public void modifyClubRecruitment(
        LocalDate startDate,
        LocalDate endDate,
        Boolean isAlwaysRecruiting,
        String imageUrl,
        String content
    ) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.isAlwaysRecruiting = isAlwaysRecruiting;
        this.imageUrl = imageUrl;
        this.content = content;
    }

    @PostLoad
    private void calculateRecruitmentInfo() {
        LocalDate today = LocalDate.now();

        if (this.isAlwaysRecruiting) {
            this.clubRecruitmentStatus = ALWAYS;
            this.Dday = null;
            return;
        }

        if (today.isBefore(this.startDate)) {
            this.clubRecruitmentStatus = BEFORE;
            this.Dday = null;
        }
        else if (!today.isAfter(this.endDate)) {
            this.clubRecruitmentStatus = RECRUITING;
            this.Dday = (int)ChronoUnit.DAYS.between(today, endDate);
        } else {
            this.clubRecruitmentStatus = CLOSED;
            this.Dday = null;
        }
    }
}
