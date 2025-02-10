package in.koreatech.koin.domain.bus.service.express.model;

import java.time.LocalTime;
import java.util.List;

/**
 * 한기대와 천안터미널 사이를 운행하는 대성 고속버스의 운행 스케줄을 정적인 데이터로 저장한 클래스입니다.
 * 외부 API 가 동작하지 않는 이슈의 해결 전까지 임시적으로 사용하기 위해 작성되었습니다.
 */
public final class ExpressBusSchedule {

    /**
     * 천안 터미널 -> 한기대 출발 시간
     */
    private static final List<LocalTime> KOREA_TECH_SCHEDULE = List.of(
        LocalTime.of(7, 0),
        LocalTime.of(8, 30),
        LocalTime.of(9, 0),
        LocalTime.of(10, 0),
        LocalTime.of(12, 0),
        LocalTime.of(12, 30),
        LocalTime.of(13, 0),
        LocalTime.of(15, 0),
        LocalTime.of(16, 0),
        LocalTime.of(16, 40),
        LocalTime.of(18, 0),
        LocalTime.of(19, 30),
        LocalTime.of(20, 30)
    );

    /**
     * 한기대 -> 천안 터미널 출발 시간
     */
    private static final List<LocalTime> TERMINAL_SCHEDULE = List.of(
        LocalTime.of(8, 35),
        LocalTime.of(10, 35),
        LocalTime.of(11, 5),
        LocalTime.of(11, 35),
        LocalTime.of(13, 35),
        LocalTime.of(14, 35),
        LocalTime.of(15, 5),
        LocalTime.of(16, 35),
        LocalTime.of(17, 35),
        LocalTime.of(19, 5),
        LocalTime.of(19, 35),
        LocalTime.of(21, 5),
        LocalTime.of(22, 5)
    );

    public static List<LocalTime> TerminalToKoreaTech() {
        return KOREA_TECH_SCHEDULE;
    }

    public static List<LocalTime> KoreaTechToTerminal() {
        return TERMINAL_SCHEDULE;
    }
}
