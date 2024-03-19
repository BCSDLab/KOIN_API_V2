package in.koreatech.koin.global.domain.upload.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class ImageUploadDomainNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "타입이 존재하지 않습니다.";

    public ImageUploadDomainNotFoundException(String message) {
        super(message);
    }

    public static ImageUploadDomainNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new ImageUploadDomainNotFoundException(message);
    }
}
