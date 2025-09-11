package in.koreatech.koin.unit.domain.club;

import in.koreatech.koin.domain.club.club.service.ClubService;
import in.koreatech.koin.domain.club.qna.repository.ClubQnaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ClubTest {

    @InjectMocks
    private ClubService clubService;

    @Mock
    private ClubQnaRepository clubQnaRepository;

    @Test
    void QNA를_성공적으로_작성한다() {
        //given

        //when

        //then
    }
}
