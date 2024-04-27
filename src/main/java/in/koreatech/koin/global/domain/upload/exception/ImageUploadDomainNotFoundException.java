package in.koreatech.koin.global.domain.upload.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class ImageUploadDomainNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "타입이 존재하지 않습니다.";
    private final String detail;

    public ImageUploadDomainNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public ImageUploadDomainNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static ImageUploadDomainNotFoundException withDetail(String detail) {
        return new ImageUploadDomainNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
