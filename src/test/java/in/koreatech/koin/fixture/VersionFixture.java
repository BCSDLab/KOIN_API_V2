package in.koreatech.koin.fixture;

import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionContent;
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
                .build()
        );
        updateVersion.getContents().addAll(
            List.of(
                VersionContent.builder()
                    .version(updateVersion)
                    .title("Android 백그라운드 푸시 알림")
                    .content("더 빠른 알림을 위해 업데이트 해주세요!")
                    .build(),
                VersionContent.builder()
                    .version(updateVersion)
                    .title("Android 키워드 알림")
                    .content("더 빠른 알림을 위해 업데이트 해주세요!")
                    .build()
            )
        );
        return versionRepository.save(updateVersion);
    }

    public Version ios() {
        var updateVersion = versionRepository.save(
            Version.builder()
                .type(VersionType.IOS.getValue())
                .version("3.5.0")
                .title("코인 신기능 업데이트")
                .build()
        );
        updateVersion.getContents().addAll(
            List.of(
                VersionContent.builder()
                    .version(updateVersion)
                    .title("iOS 백그라운드 푸시 알림")
                    .content("더 빠른 알림을 위해 업데이트 해주세요!")
                    .build()
            )
        );
        return versionRepository.save(updateVersion);
    }
}
