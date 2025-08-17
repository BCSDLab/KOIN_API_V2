package in.koreatech.koin.admin.land.execption;

import in.koreatech.koin.global.exception.custom.DuplicationException;

public class LandNameDuplicationException extends DuplicationException {

    private static final String DEFAULT_MESSAGE = "매물명이 이미 존재합니다";

    public LandNameDuplicationException(String message) {
        super(message);
    }

    public LandNameDuplicationException(String message, String detail) {
        super(message, detail);
    }

    public static LandNameDuplicationException withDetail(String name) {
        return new LandNameDuplicationException(DEFAULT_MESSAGE,"name: '" + name + "'");
    }
}

