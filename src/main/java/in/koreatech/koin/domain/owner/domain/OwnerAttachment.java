package in.koreatech.koin.domain.owner.domain;

import in.koreatech.koin.domain.owner.exception.AttachmentNotFoundException;
import in.koreatech.koin.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Entity
@Table(name = "owner_attachments")
public class OwnerAttachment extends BaseEntity {

    private static final String NAME_SEPARATOR = "/";
    public static final int NOT_FOUND_IDX = -1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, referencedColumnName = "user_id")
    private Owner owner;

    @NotNull
    @Lob
    @Column(name = "url", nullable = false)
    private String url;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Transient
    private String name;

    @PostLoad
    public void updateName() {
        int separateIndex = url.lastIndexOf(NAME_SEPARATOR);

        if (separateIndex == NOT_FOUND_IDX) {
            throw AttachmentNotFoundException.withDetail("코인 파일 저장 형식(static.koreatech.in)이 아닙니다.");
        }

        name = url.substring(separateIndex + NAME_SEPARATOR.length());
    }
}
