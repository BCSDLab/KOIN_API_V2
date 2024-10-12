package in.koreatech.koin.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.domain.version.repository.VersionRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public final class VersionFixture {

    private final VersionRepository versionRepository;

    public VersionFixture(VersionRepository versionRepository) {
        this.versionRepository = versionRepository;
    }

    public Version android() {
        var updateVersion = versionRepository.save(
            Version.builder()
                .type(VersionType.ANDROID.getValue())
                .version("3.5.0")
                .title("코인 신기능 업데이트")
                .content("더 빠른 알림을 위해 업데이트 해주세요!")
                .build()
        );
        return versionRepository.save(updateVersion);
    }

    public Version ios() {
        var updateVersion = versionRepository.save(
            Version.builder()
                .type(VersionType.IOS.getValue())
                .version("3.5.0")
                .title("코인 신기능 업데이트")
                .content("더 빠른 알림을 위해 업데이트 해주세요!")
                .build()
        );
        return versionRepository.save(updateVersion);
    }
}
