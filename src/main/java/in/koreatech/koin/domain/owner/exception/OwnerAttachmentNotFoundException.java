package in.koreatech.koin.domain.owner.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class OwnerAttachmentNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "해당 첨부파일을 찾을 수 없습니다.";

    public OwnerAttachmentNotFoundException(String message) {
        super(message);
    }

    public OwnerAttachmentNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static OwnerAttachmentNotFoundException withDetail(String detail) {
        return new OwnerAttachmentNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
