package in.koreatech.koin.domain.bus.model.enums;

public enum OpenApiResultCode {
    SERVICE_DISPOSE("12"),
    SERVICE_ACCESS_DENIED("20"),
    SERVICE_REQUEST_OVER("22"),
    KEY_UNREGISTERED("30"),
    SERVICE_KEY_EXPIRED("31"),
    SERVICE_SUCCESS("00"),
    ;

    private final String code;

    private OpenApiResultCode(String code){
        this.code = code;
    }

    public String getCode(){
        return code;
    }
}
