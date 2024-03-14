package in.koreatech.koin.domain.activity.model;

import java.time.LocalDate;

import in.koreatech.koin.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "activities")
public class Activity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    // @Pattern(regexp = "\\[([\\\"\\']https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&\\/\\/=]*)[\\\"\\'],?\\s*)*\\]", message = "이미지 링크 형식이 올바르지 않습니다.")
    @Column(name = "image_urls")
    private String imageUrls;

    @Column(name = "date")
    // private String date;
    private LocalDate date;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    // @Builder
    // public Activity(Long id, String title, String description, String imageUrls, String date, Boolean isDeleted) {
    //     this.id = id;
    //     this.title = title;
    //     this.description = description;
    //     this.imageUrls = imageUrls;
    //     this.date = date;
    //     this.isDeleted = isDeleted;
    // }
    @Builder
    public Activity(Long id, String title, String description, String imageUrls, LocalDate date, Boolean isDeleted) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrls = imageUrls;
        this.date = date;
        this.isDeleted = isDeleted;
    }
}
