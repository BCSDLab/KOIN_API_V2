package in.koreatech.koin.domain.owner.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class OwnerAttachmentNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "해당 첨부파일을 찾을 수 없습니다.";
    private final String detail;

    public OwnerAttachmentNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public OwnerAttachmentNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static OwnerAttachmentNotFoundException withDetail(String detail) {
        return new OwnerAttachmentNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
