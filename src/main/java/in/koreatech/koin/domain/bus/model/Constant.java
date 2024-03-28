package in.koreatech.koin.domain.bus.model;

public final class Constant {
    private Constant() {
        throw new IllegalStateException("Utility class");
    }

    public static final long CACHE_EXPIRE_MINUTE = 1L;

    public static final String CODE_SERVICE_DISPOSE = "12";
    public static final String CODE_SERVICE_ACCESS_DENIED = "20";
    public static final String CODE_SERVICE_REQUEST_OVER = "22";
    public static final String CODE_KEY_UNREGISTERED = "30";
    public static final String CODE_SERVICE_KEY_EXPIRED = "31";
    public static final String CODE_SERVICE_SUCCESS = "00";
}
