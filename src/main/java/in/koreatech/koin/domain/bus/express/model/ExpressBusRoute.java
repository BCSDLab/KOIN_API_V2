package in.koreatech.koin.domain.bus.express.model;

public record ExpressBusRoute(
    String depTerminalName,      // 출발지
    String arrTerminalName       // 도착지
) {

}
