package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.DiningAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.dining.model.DiningLikes;
import in.koreatech.koin.domain.dining.repository.DiningLikesRepository;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.config.WebAuthProperties;
import jakarta.servlet.http.Cookie;

class WebAuthCompatibilityTest extends AcceptanceTest {

    private static final String ORIGIN = "http://localhost:3000";
    private static final String MOBILE_USER_AGENT = "koin/1.0 (Android 14) OKHttp/4.12.0";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private WebAuthProperties properties;

    @Autowired
    private DiningAcceptanceFixture diningFixture;

    @Autowired
    private DiningLikesRepository diningLikesRepository;

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
            .loginId("web-compatibility")
            .loginPw(passwordEncoder.encode("1234"))
            .name("웹호환성테스트")
            .nickname("웹호환성")
            .phoneNumber("01055557777")
            .email("web-compatibility@koreatech.ac.kr")
            .userType(UserType.GENERAL)
            .isAuthed(true)
            .isDeleted(false)
            .build());
    }

    @Test
    void 기존_이메일_로그인은_잘못된_웹_쿠키가_남아도_JSON_토큰을_발급한다() throws Exception {
        MvcResult result = mockMvc.perform(post("/user/login")
                .header(HttpHeaders.USER_AGENT, MOBILE_USER_AGENT)
                .cookie(new Cookie(properties.accessCookieName(), "invalid-web-access"),
                    new Cookie(properties.refreshCookieName(), "invalid-web-refresh"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"web-compatibility@koreatech.ac.kr","password":"1234"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.refresh_token").isNotEmpty())
            .andExpect(jsonPath("$.user_type").value("GENERAL"))
            .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE))
            .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(response.size()).isEqualTo(3);
        mockMvc.perform(get("/user/auth")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + response.get("token").asText()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user_type").value("GENERAL"));
    }

    @Test
    void 일반_사용자의_웹_쿠키로_관리자_API를_호출할_수_없다() throws Exception {
        Cookie access = login();

        mockMvc.perform(get("/admin/users/{id}", user.getId()).header(HttpHeaders.ORIGIN, ORIGIN).cookie(access))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(ApiResponseCode.FORBIDDEN_USER_TYPE.getCode()));
    }

    @Test
    void 학생도_웹_쿠키로_로그인하고_기존_학생_API를_사용한다() throws Exception {
        Student student = userFixture.성빈_학생(departmentFixture.컴퓨터공학부());
        MvcResult result = mockMvc.perform(post("/v2/web/auth/login").header(HttpHeaders.ORIGIN, ORIGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("login_id", student.getUser().getLoginId(), "login_pw", "1234"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.user_type").value("STUDENT"))
            .andExpect(jsonPath("$.token").doesNotExist())
            .andExpect(jsonPath("$.refresh_token").doesNotExist())
            .andReturn();

        Cookie access = result.getResponse().getCookie(properties.accessCookieName());
        mockMvc.perform(get("/user/student/me").header(HttpHeaders.ORIGIN, ORIGIN).cookie(access))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(student.getId()))
            .andExpect(jsonPath("$.user_type").value("STUDENT"));
    }

    @Test
    void 선택_인증_API는_쿠키와_Bearer_사용자를_인식하고_익명_조회도_허용한다() throws Exception {
        Dining dining = diningFixture.A코너_점심(LocalDate.of(2026, 9, 10));
        dining.likesDining();
        diningLikesRepository.save(DiningLikes.builder().diningId(dining.getId()).userId(user.getId()).build());
        Cookie access = login();

        mockMvc.perform(get("/dinings").param("date", "260910").header(HttpHeaders.ORIGIN, ORIGIN).cookie(access))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(dining.getId()))
            .andExpect(jsonPath("$[0].is_liked").value(true));

        mockMvc.perform(get("/dinings").param("date", "260910"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(dining.getId()))
            .andExpect(jsonPath("$[0].is_liked").value(false));

        mockMvc.perform(get("/dinings").param("date", "260910")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtProvider.createToken(user)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(dining.getId()))
            .andExpect(jsonPath("$[0].is_liked").value(true));
    }

    private Cookie login() throws Exception {
        MvcResult result = mockMvc.perform(post("/v2/web/auth/login").header(HttpHeaders.ORIGIN, ORIGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"login_id":"web-compatibility","login_pw":"1234"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.user_type").value("GENERAL"))
            .andReturn();
        return result.getResponse().getCookie(properties.accessCookieName());
    }
}
