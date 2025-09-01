package in.koreatech.koin.unit.domain.club.service;

import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import in.koreatech.koin.common.event.ClubCreateEvent;
import in.koreatech.koin.domain.club.dto.request.ClubCreateRequest;
import in.koreatech.koin.domain.club.model.redis.ClubCreateRedis;
import in.koreatech.koin.domain.club.repository.ClubCategoryRepository;
import in.koreatech.koin.domain.club.repository.ClubEventImageRepository;
import in.koreatech.koin.domain.club.repository.ClubEventRepository;
import in.koreatech.koin.domain.club.repository.ClubEventSubscriptionRepository;
import in.koreatech.koin.domain.club.repository.ClubHotRepository;
import in.koreatech.koin.domain.club.repository.ClubLikeRepository;
import in.koreatech.koin.domain.club.repository.ClubListQueryRepository;
import in.koreatech.koin.domain.club.repository.ClubManagerRepository;
import in.koreatech.koin.domain.club.repository.ClubQnaRepository;
import in.koreatech.koin.domain.club.repository.ClubRecruitmentRepository;
import in.koreatech.koin.domain.club.repository.ClubRecruitmentSubscriptionRepository;
import in.koreatech.koin.domain.club.repository.ClubRepository;
import in.koreatech.koin.domain.club.repository.ClubSNSRepository;
import in.koreatech.koin.domain.club.repository.redis.ClubCreateRedisRepository;
import in.koreatech.koin.domain.club.repository.redis.ClubHitsRedisRepository;
import in.koreatech.koin.domain.club.repository.redis.ClubHotRedisRepository;
import in.koreatech.koin.domain.club.service.ClubService;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class ClubServiceTest {

    @Mock private ClubHotRedisRepository hotClubRedisRepository;
    @Mock private ClubHotRepository clubHotRepository;
    @Mock private ClubQnaRepository clubQnaRepository;
    @Mock private ClubRepository clubRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private ClubManagerRepository clubManagerRepository;
    @Mock private ClubCategoryRepository clubCategoryRepository;
    @Mock private ClubSNSRepository clubSNSRepository;
    @Mock private ClubLikeRepository clubLikeRepository;
    @Mock private UserRepository userRepository;
    @Mock private ClubCreateRedisRepository clubCreateRedisRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private ClubHitsRedisRepository clubHitsRedisRepository;
    @Mock private ClubRecruitmentRepository clubRecruitmentRepository;
    @Mock private ClubListQueryRepository clubListQueryRepository;
    @Mock private ClubRecruitmentSubscriptionRepository clubRecruitmentSubscriptionRepository;
    @Mock private ClubEventRepository clubEventRepository;
    @Mock private ClubEventImageRepository clubEventImageRepository;
    @Mock private ClubEventSubscriptionRepository clubEventSubscriptionRepository;
    
    @InjectMocks private ClubService clubService;

    @Test
    void 동아리_생성_요청을_처리한다() {
        // given
        ClubCreateRequest request = new ClubCreateRequest(
            "BCSD",
            "https://bcsdlab.com/static/img/logo.d89d9cc.png",
            List.of(new ClubCreateRequest.InnerClubManagerRequest("bcsdlab")),
            1,
            "학생회관",
            "즐겁게 일하고 열심히 노는 IT 특성화 동아리",
            "https://www.instagram.com/bcsdlab/",
            "https://forms.gle/example",
            "https://open.kakao.com/example",
            "01012345678",
            "회장",
            false
        );

        Integer studentId = 1;

        // when
        clubService.createClubRequest(request, studentId);

        // then
        ArgumentCaptor<ClubCreateRedis> redisCaptor = ArgumentCaptor.forClass(ClubCreateRedis.class);
        verify(clubCreateRedisRepository).save(redisCaptor.capture());

        ArgumentCaptor<ClubCreateEvent> eventCaptor = ArgumentCaptor.forClass(ClubCreateEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
    }
}


