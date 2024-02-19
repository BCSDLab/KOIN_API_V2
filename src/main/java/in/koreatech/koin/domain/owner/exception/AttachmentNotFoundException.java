package in.koreatech.koin.domain.owner.exception;

import in.koreatech.koin.domain.shop.exception.MenuNotFoundException;
import in.koreatech.koin.global.exception.DataNotFoundException;

public class AttachmentNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 첨부파일입니다.";

    public AttachmentNotFoundException(String message) {
        super(message);
    }

    public static MenuNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new MenuNotFoundException(message);
    }
}
