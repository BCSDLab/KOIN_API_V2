package in.koreatech.koin.domain.owner.model;

import org.hibernate.annotations.Where;

import in.koreatech.koin.domain.owner.exception.AttachmentNotFoundException;
import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import static lombok.AccessLevel.PROTECTED;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Where(clause = "is_deleted=0")
@Table(name = "owner_attachments")
@NoArgsConstructor(access = PROTECTED)
public class OwnerAttachment extends BaseEntity {

    private static final String NAME_SEPARATOR = "/";
    private static final int NOT_FOUND_IDX = -1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Lob
    @Column(name = "url", nullable = false)
    private String url;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Transient
    private String name;

    @PostPersist
    @PostLoad
    public void updateName() {
        int separateIndex = url.lastIndexOf(NAME_SEPARATOR);
        if (separateIndex == NOT_FOUND_IDX) {
            throw AttachmentNotFoundException.withDetail("코인 파일 저장 형식(static.koreatech.in)이 아닙니다. url: " + url);
        }

        name = url.substring(separateIndex + NAME_SEPARATOR.length());
    }

    @Builder
    private OwnerAttachment(String url, Boolean isDeleted, String name) {
        this.url = url;
        this.isDeleted = isDeleted;
        this.name = name;
    }
}
