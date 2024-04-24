package in.koreatech.koin.domain.owner.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class OwnerAttachmentNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "해당 첨부파일을 찾을 수 없습니다.";

    public OwnerAttachmentNotFoundException(String message) {
        super(message);
    }

    public static OwnerAttachmentNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new OwnerAttachmentNotFoundException(message);
    }

    @Override
    public String getDefaultMessage() {
        return DEFAULT_MESSAGE;
    }
}
