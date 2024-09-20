package in.koreatech.koin.admin.acceptance;

import org.junit.jupiter.api.TestInstance;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.AcceptanceTest;

@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AdminBenefitApiTest extends AcceptanceTest {

}
