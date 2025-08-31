package in.koreatech.koin.domain.member.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.common.model.BaseEntity;
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

/**
 * BCSDLab 회원에 대한 정보를 다루는 엔티티
 */
@Getter
@Entity
@Table(name = "members")
@NoArgsConstructor(access = PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Size(max = 20)
    @Column(name = "student_number", length = 20)
    private String studentNumber;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "track_id")
    private Track track;

    @Size(max = 20)
    @NotNull
    @Column(name = "position", nullable = false, length = 20)
    private String position;

    @Size(max = 100)
    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "image_url")
    private String imageUrl;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Builder
    private Member(
        String name,
        String studentNumber,
        Track track,
        String position,
        String email,
        String imageUrl,
        boolean isDeleted
    ) {
        this.name = name;
        this.studentNumber = studentNumber;
        this.track = track;
        this.position = position;
        this.email = email;
        this.imageUrl = imageUrl;
        this.isDeleted = isDeleted;
    }

    public void delete() {
        this.isDeleted = true;
    }

    public void undelete() {
        this.isDeleted = false;
    }

    public void update(String name, String studentNumber, String position, String email, String imageUrl) {
        this.name = name;
        this.studentNumber = studentNumber;
        this.position = position;
        this.email = email;
        this.imageUrl = imageUrl;
    }

    public void updateTrack(Track track) {
        this.track = track;
    }
}
