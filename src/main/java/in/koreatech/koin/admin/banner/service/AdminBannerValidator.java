package in.koreatech.koin.admin.banner.service;

import org.springframework.stereotype.Service;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;

@Service
public class AdminBannerValidator {

    // 모바일 유효성 검증
    public void ValidateMobileField(Boolean isReleased, String redirectLink, String minimumVersion) {
        if (isReleased) {
            validateMobileReleased(redirectLink, minimumVersion);
        } else {
            validateMobileUnreleased(redirectLink, minimumVersion);
        }
    }

    private void validateMobileReleased(String redirectLink, String minimumVersion) {
        if (isPresent(redirectLink) != isPresent(minimumVersion)) {
            throw new KoinIllegalArgumentException("모바일 배포가 활성화된 경우, 리다이렉션 링크와 최소 버전은 모두 존재하거나 모두 없어야 합니다.");
        }
    }

    private void validateMobileUnreleased(String redirectLink, String minimumVersion) {
        if (isPresent(redirectLink) || isPresent(minimumVersion)) {
            throw new KoinIllegalArgumentException("모바일 배포가 비활성화된 경우, 리다이렉션 링크와 최소버전을 설정할 수 없습니다.");
        }
    }

    // 웹 유효성 검증
    public void ValidateWebField(Boolean isReleased, String redirectLink) {
        if (!isReleased) {
            validateWebUnreleased(redirectLink);
        }
    }

    private void validateWebUnreleased(String redirectLink) {
        if (isPresent(redirectLink)) {
            throw new KoinIllegalArgumentException("웹 배포여부가 비활성화된 경우, 리다이렉션 링크는 존재하면 안됩니다.");
        }
    }

    private boolean isPresent(String value) {
        return value != null;
    }
}
