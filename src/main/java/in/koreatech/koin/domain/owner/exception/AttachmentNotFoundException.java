package in.koreatech.koin.domain.owner.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class AttachmentNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 첨부파일입니다.";

    public AttachmentNotFoundException(String message) {
        super(message);
    }

    public AttachmentNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static AttachmentNotFoundException withDetail(String detail) {
        return new AttachmentNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
