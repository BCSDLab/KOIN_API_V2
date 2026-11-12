package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

@TestPropertySource(properties = {"article.ai-summary.enabled=false", "upstage.api-key="})
class UserLanguagePersistenceTest extends AcceptanceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @BeforeAll
    void clearDatabase() {
        clear();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 언어와_프로필의_동시_변경은_저장_순서와_관계없이_서로_덮어쓰지_않는다(boolean languageFirst) {
        User user = userRepository.save(User.builder()
            .userType(UserType.GENERAL).name("기존이름").loginPw("test-password")
            .isAuthed(true).isDeleted(false).build());
        EntityManager profileContext = entityManagerFactory.createEntityManager();
        EntityManager languageContext = entityManagerFactory.createEntityManager();
        LocalDateTime loginTime = LocalDateTime.of(2026, 11, 8, 10, 0);

        try {
            profileContext.getTransaction().begin();
            languageContext.getTransaction().begin();
            User profileUser = profileContext.find(User.class, user.getId());
            User languageUser = languageContext.find(User.class, user.getId());
            profileUser.updateName("변경이름");
            profileUser.updateLastLoggedTime(loginTime);
            languageUser.changeLanguage(SupportedLanguage.ENGLISH);

            if (languageFirst) {
                languageContext.getTransaction().commit();
                profileContext.getTransaction().commit();
            } else {
                profileContext.getTransaction().commit();
                languageContext.getTransaction().commit();
            }

            User stored = userRepository.getById(user.getId());
            assertThat(stored.getPreferredLanguage()).isEqualTo("en-US");
            assertThat(stored.getName()).isEqualTo("변경이름");
            assertThat(stored.getLastLoggedAt()).isEqualTo(loginTime);
        } finally {
            close(profileContext);
            close(languageContext);
            userRepository.delete(userRepository.getById(user.getId()));
        }
    }

    private void close(EntityManager context) {
        if (context.getTransaction().isActive()) {
            context.getTransaction().rollback();
        }
        context.close();
    }
}
