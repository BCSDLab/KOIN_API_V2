package in.koreatech.koin.admin.member.exception;

import in.koreatech.koin.global.exception.custom.DuplicationException;

public class TrackNameDuplicationException extends DuplicationException {

    private static final String DEFAULT_MESSAGE = "트랙명이 이미 존재합니다";

    protected TrackNameDuplicationException(String message) {
        super(message);
    }

    protected TrackNameDuplicationException(String message, String detail) {
        super(message, detail);
    }

    public static TrackNameDuplicationException withDetail(String name) {
        return new TrackNameDuplicationException(DEFAULT_MESSAGE, "name: " + name);
    }
}
