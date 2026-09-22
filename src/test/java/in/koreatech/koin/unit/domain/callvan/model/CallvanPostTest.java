package in.koreatech.koin.unit.domain.callvan.model;

import static in.koreatech.koin.domain.callvan.model.enums.CallvanLocation.FRONT_GATE;
import static in.koreatech.koin.domain.callvan.model.enums.CallvanLocation.STATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.model.enums.CallvanStatus;
import in.koreatech.koin.global.exception.CustomException;

@SuppressWarnings("NonAsciiCharacters")
class CallvanPostTest {

    @Test
    void 출발_시간이_지나지_않았으면_참여할_수_있다() {
        CallvanPost callvanPost = callvanPost(LocalDateTime.now().plusHours(1), 1, 4);

        callvanPost.checkJoinable();
    }

    @Test
    void 출발_시간이_지나면_참여할_수_없다() {
        CallvanPost callvanPost = callvanPost(LocalDateTime.now().minusMinutes(1), 1, 4);

        assertThatThrownBy(callvanPost::checkJoinable)
            .isInstanceOf(CustomException.class);
    }

    @Test
    void 모집_중이_아니면_출발_시간과_무관하게_참여할_수_없다() {
        CallvanPost callvanPost = callvanPost(LocalDateTime.now().plusHours(1), 1, 4, CallvanStatus.CLOSED);

        assertThatThrownBy(callvanPost::checkJoinable)
            .isInstanceOf(CustomException.class);
    }

    @Test
    void 출발_시간이_지나지_않았어도_정원이_가득_차면_참여할_수_없다() {
        CallvanPost callvanPost = callvanPost(LocalDateTime.now().plusHours(1), 4, 4);

        assertThatThrownBy(callvanPost::checkJoinable)
            .isInstanceOf(CustomException.class);
        assertThat(callvanPost.getCurrentParticipants()).isEqualTo(4);
    }

    private CallvanPost callvanPost(LocalDateTime departureAt, int currentParticipants, int maxParticipants) {
        return callvanPost(departureAt, currentParticipants, maxParticipants, CallvanStatus.RECRUITING);
    }

    private CallvanPost callvanPost(
        LocalDateTime departureAt, int currentParticipants, int maxParticipants, CallvanStatus status
    ) {
        return CallvanPost.builder()
            .title("콜벤 같이 타요")
            .departureType(FRONT_GATE)
            .arrivalType(STATION)
            .departureDate(departureAt.toLocalDate())
            .departureTime(departureAt.toLocalTime())
            .maxParticipants(maxParticipants)
            .currentParticipants(currentParticipants)
            .status(status)
            .build();
    }
}
