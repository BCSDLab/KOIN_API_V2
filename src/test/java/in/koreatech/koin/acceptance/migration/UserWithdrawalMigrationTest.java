package in.koreatech.koin.acceptance.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.flywaydb.core.Flyway;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.service.RefreshTokenService;
import in.koreatech.koin.domain.user.service.UserService;
import in.koreatech.koin.domain.user.service.UserValidationService;
import in.koreatech.koin.domain.user.verification.service.UserVerificationService;
import in.koreatech.koin.global.auth.JwtProvider;

@Testcontainers
class UserWithdrawalMigrationTest {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.29")
        .withDatabaseName("user_withdrawal_migration")
        .withUsername("test")
        .withPassword("test");

    @BeforeAll
    static void migrateExistingData() throws SQLException {
        Flyway.configure()
            .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
            .locations("classpath:db/migration")
            .target("7")
            .load()
            .migrate();

        try (Connection connection = getConnection()) {
            execute(connection,
                """
                    INSERT INTO users (id, password, user_type, anonymous_nickname)
                    VALUES (101, 'test', 'GENERAL', '탈퇴회원'),
                           (102, 'test', 'GENERAL', '다른회원'),
                           (103, 'test', 'GENERAL', '멱등키회원'),
                           (104, 'test', 'GENERAL', '신규회원'),
                           (105, 'test', 'GENERAL', '롤백회원')
                    """,
                """
                    INSERT INTO `order` (id, order_type, phone_number, total_price, user_id, is_deleted)
                    VALUES ('active-order', 'DELIVERY', '01000000000', 12000, 101, 0),
                           ('deleted-order', 'TAKEOUT', '01000000000', 8000, 101, 1),
                           ('other-order', 'TAKEOUT', '01000000001', 9000, 102, 0),
                           ('rollback-order', 'TAKEOUT', '01000000002', 10000, 105, 0)
                    """,
                """
                    INSERT INTO order_delivery (order_id, address, delivery_tip)
                    VALUES ('active-order', '테스트 배달 주소', 1000)
                    """,
                """
                    INSERT INTO order_takeout (order_id, to_owner)
                    VALUES ('deleted-order', '테스트 요청')
                    """,
                """
                    INSERT INTO order_menu (id, menu_name, menu_price, quantity, order_id)
                    VALUES (201, '테스트 메뉴', 11000, 1, 'active-order')
                    """,
                """
                    INSERT INTO order_menu_option
                        (id, option_name, option_price, quantity, order_menu_id, option_group_name)
                    VALUES (301, '테스트 옵션', 0, 1, 201, '테스트 옵션 그룹')
                    """,
                """
                    INSERT INTO payment
                        (id, payment_key, amount, status, method, requested_at, approved_at, order_id)
                    VALUES (401, 'test-active-payment', 12000, 'DONE', 'CARD', NOW(), NOW(), 'active-order'),
                           (402, 'test-deleted-payment', 8000, 'CANCELED', 'CARD', NOW(), NOW(), 'deleted-order')
                    """,
                """
                    INSERT INTO payment_cancel
                        (id, transaction_key, cancel_reason, cancel_amount, canceled_at, payment_id)
                    VALUES (501, 'test-cancel', '테스트 취소', 8000, NOW(), 402)
                    """,
                """
                    INSERT INTO payment_idempotency_key (user_id, idempotency_key)
                    VALUES (101, 'withdrawal-key'), (102, 'other-key'),
                           (103, 'key-only'), (105, 'rollback-key')
                    """
            );

            assertThatThrownBy(() -> execute(connection, "DELETE FROM users WHERE id = 101"))
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("fk_order_user");
            assertThatThrownBy(() -> execute(connection, "DELETE FROM users WHERE id = 103"))
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("fk_user");
        }

        Flyway.configure()
            .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
            .locations("classpath:db/migration")
            .load()
            .migrate();
    }

    @Test
    void 탈퇴하면_기존_주문과_결제를_보존하고_회원_참조와_멱등키를_정리한다() throws SQLException {
        deleteUserAndCommit(101);

        try (Connection connection = getConnection()) {
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM users WHERE id = 101")).isZero();
            assertThat(queryInt(connection, """
                SELECT COUNT(*) FROM `order`
                WHERE id IN ('active-order', 'deleted-order') AND user_id IS NULL
                """)).isEqualTo(2);
            assertThat(queryInt(connection, """
                SELECT COUNT(*) FROM `order`
                WHERE (id = 'active-order' AND is_deleted = 0 AND total_price = 12000)
                   OR (id = 'deleted-order' AND is_deleted = 1 AND total_price = 8000)
                """)).isEqualTo(2);
            assertThat(queryString(connection, """
                SELECT address FROM order_delivery WHERE order_id = 'active-order'
                """)).isEqualTo("테스트 배달 주소");
            assertThat(queryString(connection, """
                SELECT to_owner FROM order_takeout WHERE order_id = 'deleted-order'
                """)).isEqualTo("테스트 요청");
            assertThat(queryString(connection, "SELECT menu_name FROM order_menu WHERE id = 201"))
                .isEqualTo("테스트 메뉴");
            assertThat(queryString(connection, "SELECT option_name FROM order_menu_option WHERE id = 301"))
                .isEqualTo("테스트 옵션");
            assertThat(queryInt(connection, """
                SELECT COUNT(*) FROM payment
                WHERE (id = 401 AND order_id = 'active-order' AND amount = 12000 AND status = 'DONE')
                   OR (id = 402 AND order_id = 'deleted-order' AND amount = 8000 AND status = 'CANCELED')
                """)).isEqualTo(2);
            assertThat(queryInt(connection, "SELECT cancel_amount FROM payment_cancel WHERE id = 501"))
                .isEqualTo(8000);
            assertThat(queryInt(connection, """
                SELECT COUNT(*) FROM payment_idempotency_key WHERE user_id = 101
                """)).isZero();
            assertThat(queryInt(connection, "SELECT user_id FROM `order` WHERE id = 'other-order'"))
                .isEqualTo(102);
            assertThat(queryString(connection, """
                SELECT idempotency_key FROM payment_idempotency_key WHERE user_id = 102
                """)).isEqualTo("other-key");
        }
    }

    @Test
    void 주문없이_결제_멱등키만_있는_회원이_탈퇴한다() throws SQLException {
        deleteUserAndCommit(103);

        try (Connection connection = getConnection()) {
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM users WHERE id = 103")).isZero();
            assertThat(queryInt(connection, """
                SELECT COUNT(*) FROM payment_idempotency_key WHERE user_id = 103
                """)).isZero();
        }
    }

    @Test
    void 주문과_결제_멱등키가_없는_회원이_탈퇴한다() throws SQLException {
        deleteUserAndCommit(104);

        try (Connection connection = getConnection()) {
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM users WHERE id = 104")).isZero();
        }
    }

    @Test
    void 탈퇴_서비스가_회원을_물리_삭제하고_같은_정보로_재등록해도_이전_주문이_연결되지_않는다() throws SQLException {
        String insertUser = """
            INSERT INTO users
                (password, user_type, anonymous_nickname, nickname, phone_number, email, user_id)
            VALUES ('test', 'GENERAL', '재등록익명', '재등록회원', '01000000106',
                    'withdrawal@example.com', 'withdrawal-test')
            """;
        int withdrawnUserId;
        try (Connection connection = getConnection()) {
            execute(connection, insertUser);
            withdrawnUserId = queryInt(connection,
                "SELECT id FROM users WHERE email = 'withdrawal@example.com'");
            execute(connection,
                """
                    INSERT INTO `order` (id, order_type, phone_number, user_id)
                    VALUES ('reregister-order', 'TAKEOUT', '01000000106', %d)
                    """.formatted(withdrawnUserId),
                """
                    INSERT INTO payment_idempotency_key (user_id, idempotency_key)
                    VALUES (%d, 'reregister-key')
                    """.formatted(withdrawnUserId)
            );
        }

        // Flyway 스키마에서 실제 UserService와 JPA 저장소를 사용하고 커밋 시점의 DELETE까지 검증한다.
        try (SessionFactory sessionFactory = new Configuration()
            .addAnnotatedClass(User.class)
            .setProperty("hibernate.connection.url", MYSQL.getJdbcUrl())
            .setProperty("hibernate.connection.username", MYSQL.getUsername())
            .setProperty("hibernate.connection.password", MYSQL.getPassword())
            .setProperty("hibernate.hbm2ddl.auto", "none")
            .buildSessionFactory();
            Session session = sessionFactory.openSession()) {
            UserRepository userRepository = new JpaRepositoryFactory(session).getRepository(UserRepository.class);
            UserService userService = new UserService(
                userRepository,
                mock(StudentRepository.class),
                mock(OwnerRepository.class),
                mock(UserVerificationService.class),
                mock(TimetableFrameRepositoryV2.class),
                mock(ApplicationEventPublisher.class),
                mock(UserValidationService.class),
                mock(RefreshTokenService.class),
                mock(JwtProvider.class),
                mock(PasswordEncoder.class)
            );
            session.beginTransaction();
            try {
                userService.withdraw(withdrawnUserId);
                session.getTransaction().commit();
            } catch (RuntimeException exception) {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
                throw exception;
            }
        }

        try (Connection connection = getConnection()) {
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM users WHERE id = " + withdrawnUserId)).isZero();
            assertThat(queryInt(connection,
                "SELECT COUNT(*) FROM payment_idempotency_key WHERE user_id = " + withdrawnUserId)).isZero();
            execute(connection, insertUser);
            assertThat(queryInt(connection,
                "SELECT id FROM users WHERE email = 'withdrawal@example.com'")).isNotEqualTo(withdrawnUserId);
            assertThat(queryInt(connection, """
                SELECT COUNT(*) FROM `order` WHERE id = 'reregister-order' AND user_id IS NULL
                """)).isOne();
        }
    }

    @Test
    void 탈퇴를_롤백하면_주문_참조와_결제_멱등키도_복구된다() throws SQLException {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                execute(connection, "DELETE FROM users WHERE id = 105");
                assertThat(queryInt(connection, """
                    SELECT COUNT(*) FROM `order` WHERE id = 'rollback-order' AND user_id IS NULL
                    """)).isOne();
                assertThat(queryInt(connection, """
                    SELECT COUNT(*) FROM payment_idempotency_key WHERE user_id = 105
                    """)).isZero();
            } finally {
                connection.rollback();
            }
        }

        try (Connection connection = getConnection()) {
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM users WHERE id = 105")).isOne();
            assertThat(queryInt(connection, "SELECT user_id FROM `order` WHERE id = 'rollback-order'"))
                .isEqualTo(105);
            assertThat(queryString(connection, """
                SELECT idempotency_key FROM payment_idempotency_key WHERE user_id = 105
                """)).isEqualTo("rollback-key");
        }
    }

    @Test
    void 존재하지_않는_회원의_주문과_결제_멱등키는_저장할_수_없다() throws SQLException {
        try (Connection connection = getConnection()) {
            assertThatThrownBy(() -> execute(connection, """
                INSERT INTO `order` (id, order_type, phone_number, user_id)
                VALUES ('invalid-order', 'TAKEOUT', '01000000000', 999)
                """))
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("fk_order_user_withdrawal");
            assertThatThrownBy(() -> execute(connection, """
                INSERT INTO payment_idempotency_key (user_id, idempotency_key)
                VALUES (999, 'invalid-key')
                """))
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("fk_payment_idempotency_key_user");
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
    }

    private void deleteUserAndCommit(int userId) throws SQLException {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                execute(connection, "DELETE FROM users WHERE id = " + userId);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private static void execute(Connection connection, String... queries) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (String query : queries) {
                statement.executeUpdate(query);
            }
        }
    }

    private int queryInt(Connection connection, String query) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(query)) {
            assertThat(result.next()).isTrue();
            return result.getInt(1);
        }
    }

    private String queryString(Connection connection, String query) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(query)) {
            assertThat(result.next()).isTrue();
            return result.getString(1);
        }
    }
}
