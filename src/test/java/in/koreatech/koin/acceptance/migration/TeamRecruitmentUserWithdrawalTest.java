package in.koreatech.koin.acceptance.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.flywaydb.core.Flyway;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.util.ReflectionUtils;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentApplication;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMember;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMessage;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentNotification;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentOutboxEvent;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentRole;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentUserLock;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMessageRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentApplicationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRoleRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentUserLockRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatRoomRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentNotificationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentOutboxEventRepository;
import in.koreatech.koin.domain.team.recruitment.service.TeamRecruitmentClosureService;
import in.koreatech.koin.domain.team.recruitment.service.TeamRecruitmentWithdrawalService;
import in.koreatech.koin.domain.teamrecruitment.dto.ChatMessageResponse;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.service.RefreshTokenService;
import in.koreatech.koin.domain.user.service.UserService;
import in.koreatech.koin.domain.user.service.UserValidationService;
import in.koreatech.koin.domain.user.verification.service.UserVerificationService;
import in.koreatech.koin.global.auth.JwtProvider;

@Testcontainers
class TeamRecruitmentUserWithdrawalTest {

    private static final AtomicInteger NEXT_ID = new AtomicInteger(1000);
    private static int existingRecruitmentBase;

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.29")
        .withDatabaseName("team_recruitment_user_withdrawal")
        .withUsername("test")
        .withPassword("test");

    @BeforeAll
    static void migrate() throws SQLException {
        Flyway.configure()
            .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
            .locations("classpath:db/migration")
            .target("15")
            .load()
            .migrate();
        existingRecruitmentBase = seedRecruitment();
        try (Connection connection = getConnection()) {
            assertThatThrownBy(() -> execute(connection, "DELETE FROM users WHERE id = " + existingRecruitmentBase))
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("fk_team_recruitment_application_applicant_id");
        }
        Flyway.configure()
            .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
            .locations("classpath:db/migration")
            .load()
            .migrate();
    }

    @Test
    void 승인된_지원자가_탈퇴하면_이력을_보존하고_참여_정보와_인원을_정리한다() throws SQLException {
        int base = existingRecruitmentBase;
        withdrawAndCommit(base);

        try (Connection connection = getConnection()) {
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM users WHERE id = " + base)).isZero();
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM students WHERE user_id = " + base)).isZero();
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM users WHERE id = " + (base + 1))).isOne();
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment WHERE id = " + (base + 10)))
                .isOne();
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_application WHERE id = "
                + (base + 20) + " AND applicant_id IS NULL AND status = 'ACCEPTED'")).isOne();
            assertThat(queryString(connection, "SELECT motivation FROM team_recruitment_application WHERE id = "
                + (base + 20))).isEqualTo("지원 동기");
            assertThat(queryInt(connection, "SELECT current_participants FROM team_recruitment WHERE id = "
                + (base + 10))).isOne();
            assertThat(queryInt(connection, "SELECT current_participants FROM team_recruitment_role WHERE id = "
                + (base + 11))).isOne();
            assertThat(queryString(connection, "SELECT status FROM team_recruitment WHERE id = " + (base + 10)))
                .isEqualTo("RECRUITING");
            assertThat(queryString(connection, "SELECT status FROM team_recruitment_chat_room WHERE id = "
                + (base + 30))).isEqualTo("ACTIVE");
            assertThat(queryString(connection, "SELECT status FROM team_recruitment_chat_room WHERE id = "
                + (base + 31))).isEqualTo("ACTIVE");
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_chat_message WHERE chat_room_id = "
                + (base + 30))).isEqualTo(4);
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_chat_message WHERE sender_id IS NULL"
                + " AND chat_room_id = " + (base + 30))).isEqualTo(2);
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_chat_member WHERE user_id = "
                + base)).isZero();
            assertThat(queryInt(connection, "SELECT last_read_message_id FROM team_recruitment_chat_member WHERE id = "
                + (base + 62))).isEqualTo(base + 40);
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_notification WHERE recipient_id = "
                + base)).isZero();
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_notification WHERE id = "
                + (base + 51))).isOne();
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_outbox_event WHERE id = "
                + (base + 70))).isZero();
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_outbox_event WHERE id = "
                + (base + 71) + " AND status = 'PUBLISHED'")).isOne();
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_outbox_event WHERE id = "
                + (base + 72))).isOne();
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_profile WHERE user_id = "
                + base)).isZero();
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_profile_skill WHERE profile_user_id = "
                + base)).isZero();
        }

        try (SessionFactory factory = sessionFactory(); Session session = factory.openSession()) {
            JpaRepositoryFactory repositories = new JpaRepositoryFactory(session);
            TeamRecruitmentChatMessageRepository messages = repositories.getRepository(
                TeamRecruitmentChatMessageRepository.class);
            ChatMessageResponse response = ChatMessageResponse.of(
                messages.findTopByChatRoom_IdOrderByIdDesc(base + 30).orElseThrow(), 1, "탈퇴한 회원");
            assertThat(response.userId()).isNull();
            assertThat(response.userNickname()).isEqualTo("탈퇴한 회원");
            assertThat(response.content()).isEqualTo("탈퇴 전 마지막 메시지");
            assertThat(messages.countUnreadMessagesByUserId(base + 2))
                .filteredOn(count -> count.getChatRoomId().equals(base + 30))
                .singleElement().satisfies(count -> assertThat(count.getUnreadMessageCount()).isEqualTo(2));
        }
    }

    @Test
    void 작성자가_탈퇴하면_모집글을_마감하고_다른_회원의_지원서와_채팅을_보존한다() throws SQLException {
        int base = seedRecruitment();
        withdrawAndCommit(base + 1);

        try (Connection connection = getConnection()) {
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM users WHERE id = " + (base + 1))).isZero();
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment WHERE id = " + (base + 10)
                + " AND author_id IS NULL AND status = 'CLOSED'")).isOne();
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_application WHERE recruitment_id = "
                + (base + 10))).isEqualTo(3);
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_application WHERE recruitment_id = "
                + (base + 10) + " AND status = 'ACCEPTED' AND applicant_id IS NOT NULL")).isEqualTo(2);
            assertThat(queryString(connection, "SELECT decision_reason FROM team_recruitment_application WHERE id = "
                + (base + 22))).isEqualTo("RECRUITMENT_CLOSED");
            assertThat(queryString(connection, "SELECT status FROM team_recruitment_application WHERE id = "
                + (base + 22))).isEqualTo("REJECTED");
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_chat_room WHERE recruitment_id = "
                + (base + 10) + " AND status = 'ACTIVE'")).isEqualTo(2);
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_chat_message WHERE chat_room_id = "
                + (base + 30))).isEqualTo(4);
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_chat_message WHERE id = "
                + (base + 42) + " AND sender_id IS NULL")).isOne();
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_chat_member WHERE chat_room_id = "
                + (base + 30))).isEqualTo(2);
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_notification WHERE recipient_id = "
                + (base + 1))).isZero();
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_notification WHERE recruitment_id = "
                + (base + 10) + " AND type IN ('RECRUITMENT_CLOSED', 'APPLICATION_REJECTED')")).isZero();
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_outbox_event WHERE aggregate_id = "
                + (base + 10))).isEqualTo(2);
        }

        try (SessionFactory factory = sessionFactory(); Session session = factory.openSession()) {
            TeamRecruitment recruitment = session.find(TeamRecruitment.class, base + 10);
            assertThat(recruitment.getAuthor()).isNull();
            assertThat(recruitment.getTitle()).isEqualTo("탈퇴 테스트");
        }
    }

    @Test
    void 대기_중인_지원자의_탈퇴는_지원_취소로_남기고_인원을_바꾸지_않는다() throws SQLException {
        int base = seedRecruitment();
        withdrawAndCommit(base + 3);

        try (Connection connection = getConnection()) {
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_application WHERE id = "
                + (base + 22) + " AND applicant_id IS NULL AND status = 'CANCELLED'")).isOne();
            assertThat(queryString(connection, "SELECT decision_reason FROM team_recruitment_application WHERE id = "
                + (base + 22))).isEqualTo("USER_WITHDRAWN");
            assertThat(queryInt(connection, "SELECT current_participants FROM team_recruitment WHERE id = "
                + (base + 10))).isEqualTo(2);
            assertThat(queryInt(connection, "SELECT current_participants FROM team_recruitment_role WHERE id = "
                + (base + 11))).isEqualTo(2);
        }
    }

    @Test
    void 탈퇴_도중_커밋된_지원_승인을_마감_처리가_덮어쓰지_않는다() throws SQLException {
        int base = seedRecruitment();
        try (SessionFactory factory = sessionFactory(); Session session = factory.openSession()) {
            session.beginTransaction();
            try {
                session.find(User.class, base + 1);
                try (Connection approval = getConnection()) {
                    approval.setAutoCommit(false);
                    execute(approval,
                        "UPDATE team_recruitment_application SET status = 'ACCEPTED' WHERE id = " + (base + 22),
                        "UPDATE team_recruitment SET current_participants = 3 WHERE id = " + (base + 10),
                        "UPDATE team_recruitment_role SET current_participants = 3 WHERE id = " + (base + 11),
                        "INSERT INTO team_recruitment_chat_member (chat_room_id, user_id) VALUES ("
                            + (base + 30) + ", " + (base + 3) + ")",
                        "INSERT INTO team_recruitment_outbox_event "
                            + "(id, event_key, event_type, aggregate_type, aggregate_id, payload, status) VALUES ("
                            + (base + 73) + ", 'fresh-approval-" + base
                            + "', 'TEAM_RECRUITMENT_NOTIFICATION', 'TEAM_RECRUITMENT', " + (base + 10)
                            + ", JSON_OBJECT('recipient_id', " + (base + 1) + "), 'PENDING')"
                    );
                    approval.commit();
                }
                userService(session).withdraw(base + 1);
                session.getTransaction().commit();
            } finally {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            }
        }
        try (Connection connection = getConnection()) {
            assertThat(queryString(connection, "SELECT status FROM team_recruitment_application WHERE id = "
                + (base + 22))).isEqualTo("ACCEPTED");
            assertThat(queryInt(connection, "SELECT current_participants FROM team_recruitment WHERE id = "
                + (base + 10))).isEqualTo(3);
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_notification WHERE application_id = "
                + (base + 22) + " AND type = 'APPLICATION_REJECTED'")).isZero();
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_outbox_event WHERE id = "
                + (base + 73))).isZero();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"PENDING", "ACCEPTED"})
    void 탈퇴_도중_새로_생긴_지원도_최신_상태로_정리한다(String status) throws SQLException {
        int base = seedRecruitment();
        try (Connection connection = getConnection()) {
            execute(connection, "DELETE FROM team_recruitment_application WHERE id = " + (base + 22));
        }
        try (SessionFactory factory = sessionFactory(); Session session = factory.openSession()) {
            session.beginTransaction();
            try {
                session.find(User.class, base + 3);
                try (Connection application = getConnection()) {
                    application.setAutoCommit(false);
                    execute(application, """
                        INSERT INTO team_recruitment_application
                            (id, recruitment_id, applicant_id, role_id, motivation, availability, status, profile_snapshot)
                        VALUES (%d, %d, %d, %d, '새 지원', '주말', '%s', JSON_OBJECT('nickname', '지원자'))
                        """.formatted(base + 22, base + 10, base + 3, base + 11, status));
                    if (status.equals("ACCEPTED")) {
                        execute(application,
                            "UPDATE team_recruitment SET current_participants = 3 WHERE id = " + (base + 10),
                            "UPDATE team_recruitment_role SET current_participants = 3 WHERE id = " + (base + 11));
                    }
                    application.commit();
                }
                userService(session).withdraw(base + 3);
                session.getTransaction().commit();
            } finally {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            }
        }

        try (Connection connection = getConnection()) {
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM users WHERE id = " + (base + 3))).isZero();
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_application WHERE id = "
                + (base + 22) + " AND applicant_id IS NULL")).isOne();
            assertThat(queryString(connection, "SELECT status FROM team_recruitment_application WHERE id = "
                + (base + 22))).isEqualTo(status.equals("PENDING") ? "CANCELLED" : "ACCEPTED");
            assertThat(queryInt(connection, "SELECT current_participants FROM team_recruitment WHERE id = "
                + (base + 10))).isEqualTo(2);
            assertThat(queryInt(connection, "SELECT current_participants FROM team_recruitment_role WHERE id = "
                + (base + 11))).isEqualTo(2);
        }
    }

    @Test
    void 탈퇴를_롤백하면_참조와_참여_인원과_미발송_알림도_복구된다() throws SQLException {
        int base = seedRecruitment();
        try (SessionFactory factory = sessionFactory(); Session session = factory.openSession()) {
            session.beginTransaction();
            try {
                userService(session).withdraw(base);
                session.flush();
            } finally {
                session.getTransaction().rollback();
            }
        }

        try (Connection connection = getConnection()) {
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM users WHERE id = " + base)).isOne();
            assertThat(queryInt(connection, "SELECT applicant_id FROM team_recruitment_application WHERE id = "
                + (base + 20))).isEqualTo(base);
            assertThat(queryInt(connection, "SELECT current_participants FROM team_recruitment WHERE id = "
                + (base + 10))).isEqualTo(2);
            assertThat(queryInt(connection, "SELECT current_participants FROM team_recruitment_role WHERE id = "
                + (base + 11))).isEqualTo(2);
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_outbox_event WHERE id = "
                + (base + 70))).isOne();
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_chat_member WHERE user_id = "
                + base)).isEqualTo(2);
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_notification WHERE recipient_id = "
                + base)).isOne();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void 탈퇴_트랜잭션이_열려도_무관한_모집_수정과_지원과_알림_등록은_완료된다(boolean hasHistory) throws Exception {
        int base = seedRecruitment();
        int unrelated = seedRecruitment();
        int withdrawingUser = hasHistory ? base : base + 4;
        try (Connection connection = getConnection()) {
            if (!hasHistory) {
                execute(connection, "INSERT INTO users (id, password, user_type, anonymous_nickname) VALUES ("
                    + withdrawingUser + ", 'test', 'STUDENT', '이력없는회원')",
                    "INSERT INTO students (user_id, is_graduated) VALUES (" + withdrawingUser + ", 0)");
            }
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (SessionFactory factory = sessionFactory(); Session session = factory.openSession()) {
            session.beginTransaction();
            try {
                userService(session).withdraw(withdrawingUser);
                session.flush();
                Future<?> unrelatedWrite = executor.submit(() -> {
                    try (Connection connection = getConnection()) {
                        execute(connection, "SET SESSION innodb_lock_wait_timeout = 2");
                        connection.setAutoCommit(false);
                        execute(connection, "UPDATE team_recruitment SET title = '무관한 수정' WHERE id = "
                            + (unrelated + 10));
                        insertApplicationAndOutbox(connection, unrelated, base + 1);
                        connection.commit();
                    }
                    return null;
                });
                unrelatedWrite.get(5, TimeUnit.SECONDS);
                // 탈퇴 잠금이 풀리기 전에 다른 회원의 변경이 커밋됐는지 확인한다.
                try (Connection connection = getConnection()) {
                    assertThat(queryString(connection, "SELECT title FROM team_recruitment WHERE id = "
                        + (unrelated + 10))).isEqualTo("무관한 수정");
                    assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_application WHERE id = "
                        + (unrelated + 23))).isOne();
                    assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_outbox_event WHERE id = "
                        + (unrelated + 73))).isOne();
                }
                session.getTransaction().commit();
            } finally {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            }
        } finally {
            executor.shutdownNow();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void 동일_회원의_등록과_탈퇴는_먼저_획득한_사용자_잠금_순서로_처리된다(boolean registrationFirst) throws Exception {
        int base = seedRecruitment();
        int userId = base + 4;
        try (Connection connection = getConnection()) {
            execute(connection, "INSERT INTO users (id, password, user_type, anonymous_nickname) VALUES ("
                + userId + ", 'test', 'STUDENT', '동시등록회원')",
                "INSERT INTO students (user_id, is_graduated) VALUES (" + userId + ", 0)");
            if (registrationFirst) {
                execute(connection, "INSERT INTO team_recruitment_user_lock (user_id) VALUES (" + userId + ")");
            }
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CountDownLatch attempting = new CountDownLatch(1);
        try (SessionFactory factory = sessionFactory(); Session first = factory.openSession()) {
            if (registrationFirst) {
                first.doWork(connection -> connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ));
            }
            first.beginTransaction();
            try {
                if (registrationFirst) {
                    assertThat(registerWithUserLock(first, base, userId)).isTrue();
                } else {
                    userService(first).withdraw(userId);
                    first.flush();
                }
                Future<Boolean> second = executor.submit(() -> {
                    try (Session session = factory.openSession()) {
                        if (!registrationFirst) {
                            session.doWork(connection ->
                                connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ));
                        }
                        session.beginTransaction();
                        try {
                            session.createNativeQuery("SET SESSION innodb_lock_wait_timeout = 5").executeUpdate();
                            attempting.countDown();
                            boolean registered = false;
                            if (registrationFirst) {
                                userService(session).withdraw(userId);
                            } else {
                                registered = registerWithUserLock(session, base, userId);
                            }
                            if (registrationFirst || registered) {
                                session.getTransaction().commit();
                            } else {
                                session.getTransaction().rollback();
                            }
                            return registered;
                        } finally {
                            if (session.getTransaction().isActive()) {
                                session.getTransaction().rollback();
                            }
                        }
                    }
                });
                assertThat(attempting.await(5, TimeUnit.SECONDS)).isTrue();
                assertThatThrownBy(() -> second.get(250, TimeUnit.MILLISECONDS))
                    .isInstanceOf(TimeoutException.class);
                first.getTransaction().commit();
                assertThat(second.get(5, TimeUnit.SECONDS)).isFalse();
            } finally {
                if (first.getTransaction().isActive()) {
                    first.getTransaction().rollback();
                }
            }
        } finally {
            executor.shutdownNow();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
        try (Connection connection = getConnection()) {
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM users WHERE id = " + userId)).isZero();
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_user_lock WHERE user_id = "
                + userId)).isZero();
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment WHERE id = "
                + (base + 12))).isEqualTo(registrationFirst ? 1 : 0);
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_application WHERE id = "
                + (base + 23))).isEqualTo(registrationFirst ? 1 : 0);
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment WHERE id = " + (base + 12)
                + " AND author_id IS NULL AND status = 'CLOSED'")).isEqualTo(registrationFirst ? 1 : 0);
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_application WHERE id = "
                + (base + 23) + " AND applicant_id IS NULL AND status = 'CANCELLED'"
                + " AND decision_reason = 'USER_WITHDRAWN'")).isEqualTo(registrationFirst ? 1 : 0);
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM team_recruitment_outbox_event WHERE id = "
                + (base + 73))).isZero();
        }
    }

    @Test
    void 탈퇴_알림_정리_대상을_조회한_뒤_발송이_끝난_알림은_보존한다() throws SQLException {
        int base = seedRecruitment();
        try (SessionFactory factory = sessionFactory(); Session session = factory.openSession()) {
            session.beginTransaction();
            try {
                var repository = new JpaRepositoryFactory(session)
                    .getRepository(TeamRecruitmentOutboxEventRepository.class);
                var eventIds = repository.findUnpublishedNotificationIdsByRecipientId(base);
                assertThat(eventIds).containsExactly(base + 70);
                try (Connection publisher = getConnection()) {
                    execute(publisher, "UPDATE team_recruitment_outbox_event SET status = 'PUBLISHED' WHERE id = "
                        + (base + 70));
                }
                eventIds.forEach(repository::deleteUnpublishedById);
                session.getTransaction().commit();
            } finally {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            }
        }
        try (Connection connection = getConnection()) {
            assertThat(queryString(connection, "SELECT status FROM team_recruitment_outbox_event WHERE id = "
                + (base + 70))).isEqualTo("PUBLISHED");
        }
    }

    private boolean registerWithUserLock(Session session, int base, int userId) {
        new JpaRepositoryFactory(session).getRepository(TeamRecruitmentUserLockRepository.class).lock(userId);
        if (session.find(User.class, userId) == null) {
            return false;
        }
        session.doWork(connection -> {
            execute(connection, """
                INSERT INTO team_recruitment
                    (id, author_id, category, title, meeting_type, activity_start_date, activity_end_date,
                     deadline_date, recruitment_type, max_participants, current_participants, description)
                VALUES (%d, %d, 'STUDY', '동시 등록', 'ONLINE', '2026-10-20', '2026-10-31',
                        '2026-10-19', 'ROLE_BASED', 5, 0, '동시 등록 테스트')
                """.formatted(base + 12, userId));
            insertApplicationAndOutbox(connection, base, userId);
        });
        return true;
    }

    private static void insertApplicationAndOutbox(Connection connection, int base, int userId) throws SQLException {
        execute(connection, """
            INSERT INTO team_recruitment_application
                (id, recruitment_id, applicant_id, role_id, motivation, availability, status, profile_snapshot)
            VALUES (%d, %d, %d, %d, '새 지원', '주말', 'PENDING', JSON_OBJECT('nickname', '지원자'))
            """.formatted(base + 23, base + 10, userId, base + 11),
            """
            INSERT INTO team_recruitment_outbox_event
                (id, event_key, event_type, aggregate_type, aggregate_id, payload, status)
            VALUES (%d, 'concurrent-%d', 'TEAM_RECRUITMENT_NOTIFICATION', 'TEAM_RECRUITMENT', %d,
                    JSON_OBJECT('recipient_id', %d), 'PENDING')
            """.formatted(base + 73, base + 73, base + 10, userId));
    }

    private static int seedRecruitment() throws SQLException {
        int base = NEXT_ID.getAndAdd(100);
        try (Connection connection = getConnection()) {
            execute(connection,
                """
                    INSERT INTO users (id, password, user_type, anonymous_nickname)
                    VALUES (%1$d, 'test', 'STUDENT', '지원자%1$d'),
                           (%2$d, 'test', 'STUDENT', '작성자%2$d'),
                           (%3$d, 'test', 'STUDENT', '다른회원%3$d'),
                           (%4$d, 'test', 'STUDENT', '대기회원%4$d')
                    """.formatted(base, base + 1, base + 2, base + 3),
                """
                    INSERT INTO students (user_id, is_graduated)
                    VALUES (%d, 0), (%d, 0), (%d, 0), (%d, 0)
                    """.formatted(base, base + 1, base + 2, base + 3),
                """
                    INSERT INTO team_recruitment
                        (id, author_id, category, title, meeting_type, activity_start_date, activity_end_date,
                         deadline_date, recruitment_type, max_participants, current_participants, description)
                    VALUES (%d, %d, 'STUDY', '탈퇴 테스트', 'ONLINE', '2026-10-20', '2026-10-31',
                            '2026-10-19', 'ROLE_BASED', 5, 2, '테스트 모집글')
                    """.formatted(base + 10, base + 1),
                """
                    INSERT INTO team_recruitment_role
                        (id, recruitment_id, name, max_participants, current_participants, display_order)
                    VALUES (%d, %d, '개발', 5, 2, 1)
                    """.formatted(base + 11, base + 10),
                """
                    INSERT INTO team_recruitment_application
                        (id, recruitment_id, applicant_id, role_id, motivation, availability, status, profile_snapshot)
                    VALUES (%1$d, %4$d, %5$d, %8$d, '지원 동기', '주말', 'ACCEPTED', JSON_OBJECT('nickname', '지원자')),
                           (%2$d, %4$d, %6$d, %8$d, '다른 지원 동기', '주말', 'ACCEPTED', JSON_OBJECT('nickname', '다른회원')),
                           (%3$d, %4$d, %7$d, %8$d, '대기 지원 동기', '주말', 'PENDING', JSON_OBJECT('nickname', '대기회원'))
                    """.formatted(base + 20, base + 21, base + 22, base + 10, base, base + 2, base + 3, base + 11),
                """
                    INSERT INTO team_recruitment_chat_room
                        (id, recruitment_id, room_scope_key, room_type, application_id)
                    VALUES (%d, %d, 'TEAM', 'TEAM', NULL), (%d, %d, 'DIRECT', 'DIRECT', %d)
                    """.formatted(base + 30, base + 10, base + 31, base + 10, base + 20),
                """
                    INSERT INTO team_recruitment_chat_message (id, chat_room_id, sender_id, sender_nickname, content)
                    VALUES (%1$d, %5$d, %6$d, '지원자', '지원자 메시지'),
                           (%2$d, %5$d, %7$d, '다른회원', '다른회원 메시지'),
                           (%3$d, %5$d, %8$d, '작성자', '작성자 메시지'),
                           (%4$d, %5$d, %6$d, '지원자', '탈퇴 전 마지막 메시지')
                    """.formatted(base + 40, base + 41, base + 42, base + 43, base + 30, base, base + 2, base + 1),
                """
                    INSERT INTO team_recruitment_chat_member (id, chat_room_id, user_id, last_read_message_id)
                    VALUES (%1$d, %6$d, %8$d, NULL), (%2$d, %6$d, %9$d, NULL),
                           (%3$d, %6$d, %10$d, %11$d),
                           (%4$d, %7$d, %8$d, NULL), (%5$d, %7$d, %9$d, NULL)
                    """.formatted(base + 60, base + 61, base + 62, base + 63, base + 64,
                    base + 30, base + 31, base, base + 1, base + 2, base + 40),
                """
                    INSERT INTO team_recruitment_notification
                        (id, recipient_id, type, target_type, message_preview, recruitment_id, application_id)
                    VALUES (%d, %d, 'APPLICATION_ACCEPTED', 'MY_APPLICATIONS', '승인', %d, %d),
                           (%d, %d, 'NEW_APPLICATION', 'APPLICANT_MANAGEMENT', '새 지원자', %d, %d)
                    """.formatted(base + 50, base, base + 10, base + 20,
                    base + 51, base + 1, base + 10, base + 20),
                """
                    INSERT INTO team_recruitment_outbox_event
                        (id, event_key, event_type, aggregate_type, aggregate_id, payload, status)
                    VALUES (%1$d, 'pending-%1$d', 'TEAM_RECRUITMENT_NOTIFICATION', 'TEAM_RECRUITMENT', %4$d,
                            JSON_OBJECT('recipient_id', %5$d, 'notification_id', %7$d), 'PENDING'),
                           (%2$d, 'published-%2$d', 'TEAM_RECRUITMENT_NOTIFICATION', 'TEAM_RECRUITMENT', %4$d,
                            JSON_OBJECT('recipient_id', %5$d, 'notification_id', %7$d), 'PUBLISHED'),
                           (%3$d, 'other-%3$d', 'TEAM_RECRUITMENT_NOTIFICATION', 'TEAM_RECRUITMENT', %4$d,
                            JSON_OBJECT('recipient_id', %6$d, 'notification_id', %8$d), 'PENDING')
                    """.formatted(base + 70, base + 71, base + 72, base + 10, base, base + 1, base + 50, base + 51),
                """
                    INSERT INTO team_recruitment_profile (user_id, profile_nickname, preferred_role, self_introduction)
                    VALUES (%d, '지원자', '개발', '자기소개')
                    """.formatted(base),
                """
                    INSERT INTO team_recruitment_profile_skill (profile_user_id, skill, display_order)
                    VALUES (%d, 'Java', 1)
                    """.formatted(base)
            );
        }
        return base;
    }

    private SessionFactory sessionFactory() {
        Configuration configuration = new Configuration()
            .addAnnotatedClass(User.class)
            .addAnnotatedClass(Student.class)
            .addAnnotatedClass(Department.class)
            .addAnnotatedClass(Major.class)
            .addAnnotatedClass(TeamRecruitment.class)
            .addAnnotatedClass(TeamRecruitmentRole.class)
            .addAnnotatedClass(TeamRecruitmentApplication.class)
            .addAnnotatedClass(TeamRecruitmentChatRoom.class)
            .addAnnotatedClass(TeamRecruitmentChatMember.class)
            .addAnnotatedClass(TeamRecruitmentChatMessage.class)
            .addAnnotatedClass(TeamRecruitmentNotification.class)
            .addAnnotatedClass(TeamRecruitmentOutboxEvent.class)
            .addAnnotatedClass(TeamRecruitmentUserLock.class)
            .setProperty("hibernate.connection.url", MYSQL.getJdbcUrl())
            .setProperty("hibernate.connection.username", MYSQL.getUsername())
            .setProperty("hibernate.connection.password", MYSQL.getPassword())
            .setProperty("hibernate.hbm2ddl.auto", "none");
        TransactionAttribute withdrawalTransaction = new AnnotationTransactionAttributeSource()
            .getTransactionAttribute(ReflectionUtils.findMethod(UserService.class, "withdraw", Integer.class),
                UserService.class);
        if (withdrawalTransaction != null
            && withdrawalTransaction.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
            configuration.setProperty("hibernate.connection.isolation",
                Integer.toString(withdrawalTransaction.getIsolationLevel()));
        }
        return configuration.buildSessionFactory();
    }

    private UserService userService(Session session) {
        JpaRepositoryFactory repositories = new JpaRepositoryFactory(session);
        return new UserService(
            repositories.getRepository(UserRepository.class),
            repositories.getRepository(StudentRepository.class),
            mock(OwnerRepository.class),
            mock(UserVerificationService.class),
            mock(TimetableFrameRepositoryV2.class),
            mock(ApplicationEventPublisher.class),
            mock(UserValidationService.class),
            mock(RefreshTokenService.class),
            mock(JwtProvider.class),
            mock(PasswordEncoder.class),
            new TeamRecruitmentWithdrawalService(
                repositories.getRepository(TeamRecruitmentRepository.class),
                repositories.getRepository(TeamRecruitmentApplicationRepository.class),
                repositories.getRepository(TeamRecruitmentRoleRepository.class),
                new TeamRecruitmentClosureService(
                    repositories.getRepository(TeamRecruitmentApplicationRepository.class),
                    repositories.getRepository(TeamRecruitmentChatRoomRepository.class),
                    repositories.getRepository(TeamRecruitmentNotificationRepository.class),
                    repositories.getRepository(TeamRecruitmentOutboxEventRepository.class)
                ),
                repositories.getRepository(TeamRecruitmentOutboxEventRepository.class)
            ),
            repositories.getRepository(TeamRecruitmentUserLockRepository.class)
        );
    }

    private void withdrawAndCommit(int userId) {
        try (SessionFactory factory = sessionFactory(); Session session = factory.openSession()) {
            session.beginTransaction();
            try {
                userService(session).withdraw(userId);
                session.getTransaction().commit();
            } catch (RuntimeException exception) {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
                throw exception;
            }
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
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
