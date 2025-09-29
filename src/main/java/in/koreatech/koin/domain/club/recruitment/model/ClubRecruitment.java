package in.koreatech.koin.domain.club.recruitment.model;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.club.club.model.Club;
import in.koreatech.koin.domain.club.recruitment.enums.ClubRecruitmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

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
    private ClubRecruitmentDday clubRecruitmentDday;

    @Transient
    private ClubRecruitmentStatus clubRecruitmentStatus;

    @Builder
    private ClubRecruitment(
        Integer id,
        LocalDate startDate,
        LocalDate endDate,
        Boolean isAlwaysRecruiting,
        String imageUrl,
        String content,
        Club club
    ) {
        this.id = id;
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
        this.clubRecruitmentStatus = ClubRecruitmentStatus.from(isAlwaysRecruiting, startDate, endDate);
        this.clubRecruitmentDday = ClubRecruitmentDday.from(clubRecruitmentStatus, endDate);
    }
}
