package in.koreatech.koin.infrastructure.s3.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class ImageUploadDomainNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 이미지 도메인 타입입니다.";

    public ImageUploadDomainNotFoundException(String message) {
        super(message);
    }

    public ImageUploadDomainNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static ImageUploadDomainNotFoundException withDetail(String detail) {
        return new ImageUploadDomainNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
