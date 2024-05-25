package in.koreatech.koin.domain.bus.model.express;

public record TmoneyOpenApiExpressBusArrival(
    String TER_FR_O,    // 출발지
    String TER_TO_O,    // 도착지
    String TIM_TIM_O,   // 출발 시간
    int LIN_TIM,     // 소요 시간
    String BUS_GRA_O   // 버스 등급
) {

}
