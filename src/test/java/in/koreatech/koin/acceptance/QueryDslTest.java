package in.koreatech.koin.acceptance;

import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.shop.repository.ShopCustomRepositoryImpl;

@SuppressWarnings("NonAsciiCharacters")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class QueryDslTest extends AcceptanceTest {

    @Autowired
    private ShopCustomRepositoryImpl shopCustomRepository;

    @BeforeAll
    void setup() {
        clear();
    }

    @Test
    public void 최적화() {

    }

}
