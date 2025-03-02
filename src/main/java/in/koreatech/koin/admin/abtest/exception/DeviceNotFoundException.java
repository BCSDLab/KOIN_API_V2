package in.koreatech.koin.admin.abtest.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class DeviceNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 디바이스입니다.";

    public DeviceNotFoundException(String message) {
        super(message);
    }

    public DeviceNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static DeviceNotFoundException withDetail(String detail) {
        return new DeviceNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
