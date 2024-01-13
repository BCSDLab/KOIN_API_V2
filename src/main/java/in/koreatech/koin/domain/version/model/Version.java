package in.koreatech.koin.domain.version.model;

import java.util.Objects;

import in.koreatech.koin.domain.version.dto.VersionCreateRequest;
import in.koreatech.koin.domain.version.dto.VersionUpdateRequest;
import in.koreatech.koin.domain.version.exception.VersionException;
import in.koreatech.koin.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "versions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Version extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "version", nullable = false, length = 50)
    private String version;

    @Size(max = 20)
    @Column(name = "type", length = 50)
    private String type;

    public static Version from(VersionCreateRequest versionCreateRequest){
        Version version = new Version();
        version.version = versionCreateRequest.version();
        version.type = versionCreateRequest.type();
        return version;
    }
    public void update(VersionUpdateRequest versionDTO) {
        if (Objects.equals(this.version, versionDTO.version())) {
            throw VersionException.withDetail("해당 타입은 이미 같은 버전을 가지고 있습니다.");
        }
        this.version = versionDTO.version();
    }
}
