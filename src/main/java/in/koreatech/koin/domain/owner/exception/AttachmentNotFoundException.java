package in.koreatech.koin.domain.owner.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class AttachmentNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 첨부파일입니다.";
    private final String detail;

    public AttachmentNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public AttachmentNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static AttachmentNotFoundException withDetail(String detail) {
        return new AttachmentNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return DEFAULT_MESSAGE;
    }
}
