package in.koreatech.koin.batch.acceptance;

import org.junit.jupiter.api.TestInstance;
import org.springframework.transaction.annotation.Transactional;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class BatchSchoolBusApiTest {
    // TODO : 배치 테스트 추가하기
}
