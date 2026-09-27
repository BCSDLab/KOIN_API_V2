package in.koreatech.koin.unit.admin.bus;

import static in.koreatech.koin.admin.bus.shuttle.model.ShuttleBusTimetable.RouteInfo.InnerNameDetail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import in.koreatech.koin.admin.bus.shuttle.enums.RunningDays;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

class RunningDaysTest {

    @ParameterizedTest
    @MethodSource("explicitWeekdayDescriptions")
    void mapsOnlySupportedExplicitWeekdayDescriptions(String name, RunningDays expected) {
        assertThat(RunningDays.from(InnerNameDetail.of(name, null))).isEqualTo(expected);
    }

    private static Stream<Arguments> explicitWeekdayDescriptions() {
        return Stream.of(
            Arguments.of("1회", RunningDays.WEEKDAYS),
            Arguments.of("평일 1회", RunningDays.WEEKDAYS),
            Arguments.of("월요일 추가", RunningDays.MONDAY),
            Arguments.of("화요일 추가", RunningDays.TUESDAY),
            Arguments.of("수요일 추가", RunningDays.WEDNESDAY),
            Arguments.of("목요일 추가", RunningDays.THURSDAY),
            Arguments.of("금요일 추가", RunningDays.FRIDAY),
            Arguments.of("토요일 오후", RunningDays.SATURDAY),
            Arguments.of("토요일오후", RunningDays.SATURDAY),
            Arguments.of("일요일 야간 1회", RunningDays.SUNDAY),
            Arguments.of("일요일야간", RunningDays.SUNDAY),
            Arguments.of("일학습병행대학 1회", RunningDays.WEEKDAYS)
        );
    }

    @ParameterizedTest
    @MethodSource("thursdayFridayDescriptions")
    void acceptsExistingThursdayFridaySeparators(String name) {
        assertThat(RunningDays.from(InnerNameDetail.of(name, null)))
            .isEqualTo(RunningDays.THURSDAY_FRIDAY);
    }

    private static Stream<String> thursdayFridayDescriptions() {
        return Stream.of("목금 추가", "목금추가1", "목·금 추가", "목・금 추가", "목, 금 추가", "목요일/금요일");
    }

    @Test
    void rejectsConflictingExplicitDays() {
        assertThatThrownBy(() -> RunningDays.from(InnerNameDetail.of("월요일 금요일", null)))
            .isInstanceOf(CustomException.class)
            .extracting("errorCode")
            .isEqualTo(ApiResponseCode.INVALID_REQUEST_BODY);
    }

    @Test
    void rejectsConflictBetweenThursdayFridayAndAnotherExplicitDay() {
        assertThatThrownBy(() -> RunningDays.from(InnerNameDetail.of("목금 토요일", null)))
            .isInstanceOf(CustomException.class)
            .extracting("errorCode")
            .isEqualTo(ApiResponseCode.INVALID_REQUEST_BODY);
    }
}
