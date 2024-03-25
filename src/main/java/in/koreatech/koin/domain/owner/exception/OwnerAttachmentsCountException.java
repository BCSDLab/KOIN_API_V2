package in.koreatech.koin.domain.owner.exception;

import in.koreatech.koin.global.exception.ArgumentCountException;

public class OwnerAttachmentsCountException extends ArgumentCountException {

    private static final String DEFAULT_MESSAGE = "첨부파일 개수가 부족합니다.";

    public OwnerAttachmentsCountException(String message) {
        super(message);
    }

    public static OwnerAttachmentsCountException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new OwnerAttachmentsCountException(message);
    }
}
