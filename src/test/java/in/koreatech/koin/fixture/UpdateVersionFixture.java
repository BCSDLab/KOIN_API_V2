package in.koreatech.koin.fixture;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.updateversion.model.UpdateContent;
import in.koreatech.koin.domain.updateversion.model.UpdateVersion;
import in.koreatech.koin.domain.updateversion.model.UpdateVersionType;
import in.koreatech.koin.domain.updateversion.repository.UpdateVersionRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public final class UpdateVersionFixture {

    private final UpdateVersionRepository updateVersionRepository;

    public UpdateVersionFixture(UpdateVersionRepository updateVersionRepository) {
        this.updateVersionRepository = updateVersionRepository;
    }

    public UpdateVersion Android() {
        var updateVersion = updateVersionRepository.save(
            UpdateVersion.builder()
                .type(UpdateVersionType.ANDROID)
                .version("3.5.0")
                .title("코인 신기능 업데이트")
                .contents(new ArrayList<>())
                .build()
        );
        updateVersion.getContents().addAll(
            List.of(
                UpdateContent.builder()
                    .type(updateVersion)
                    .title("Android 백그라운드 푸시 알림")
                    .content("더 빠른 알림을 위해 업데이트 해주세요!")
                    .build(),
                UpdateContent.builder()
                    .type(updateVersion)
                    .title("Android 키워드 알림")
                    .content("더 빠른 알림을 위해 업데이트 해주세요!")
                    .build()
            )
        );
        return updateVersionRepository.save(updateVersion);
    }
}
