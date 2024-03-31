package in.koreatech.koin.domain.bus.model;

public enum Constant {
    CODE_SERVICE_DISPOSE("12"),
    CODE_SERVICE_ACCESS_DENIED("20"),
    CODE_SERVICE_REQUEST_OVER("22"),
    CODE_KEY_UNREGISTERED("30"),
    CODE_SERVICE_KEY_EXPIRED("31"),
    CODE_SERVICE_SUCCESS("00"),
    ;

    private final String code;

    private Constant(String code){
        this.code = code;
    }

    public String getCode(){
        return code;
    }
}
