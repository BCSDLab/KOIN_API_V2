package in.koreatech.koin.domain.owner.model;

import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import org.hibernate.annotations.Where;

import in.koreatech.koin.domain.owner.exception.AttachmentNotFoundException;
import in.koreatech.koin._common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
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
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(cascade = {PERSIST, MERGE, REMOVE})
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @NotNull
    @Column(name = "url", nullable = false)
    private String url;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

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
    private OwnerAttachment(String url, Owner owner, Boolean isDeleted, String name) {
        this.url = url;
        this.owner = owner;
        this.isDeleted = isDeleted;
        this.name = name;
    }
}
