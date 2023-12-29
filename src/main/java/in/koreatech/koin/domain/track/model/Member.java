package in.koreatech.koin.domain.track.model;

import in.koreatech.koin.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Size(max = 20)
    @Column(name = "student_number", length = 20)
    private String studentNumber;

    @NotNull
    @Column(name = "track_id")
    private Long trackId;

    @Size(max = 20)
    @NotNull
    @Column(name = "position", nullable = false, length = 20)
    private String position;

    @Size(max = 100)
    @Column(name = "email", length = 100)
    private String email;

    @Lob
    @Column(name = "image_url")
    private String imageUrl;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder
    private Member(String name, String studentNumber, Long trackId, String position, String email, String imageUrl,
        Boolean isDeleted) {
        this.name = name;
        this.studentNumber = studentNumber;
        this.trackId = trackId;
        this.position = position;
        this.email = email;
        this.imageUrl = imageUrl;
        this.isDeleted = isDeleted;
    }
}
