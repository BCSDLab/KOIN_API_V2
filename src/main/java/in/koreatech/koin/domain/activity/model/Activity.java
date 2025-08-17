package in.koreatech.koin.domain.activity.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDate;

import in.koreatech.koin.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "activities")
@NoArgsConstructor(access = PROTECTED)
public class Activity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "image_urls")
    private String imageUrls;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Builder
    private Activity(
        String title,
        String description,
        String imageUrls,
        LocalDate date,
        boolean isDeleted
    ) {
        this.title = title;
        this.description = description;
        this.imageUrls = imageUrls;
        this.date = date;
        this.isDeleted = isDeleted;
    }
}
