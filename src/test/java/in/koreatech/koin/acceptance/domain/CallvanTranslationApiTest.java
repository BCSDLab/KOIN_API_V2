package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.CallvanAcceptanceFixture;
import in.koreatech.koin.acceptance.support.JsonAssertions;
import in.koreatech.koin.domain.callvan.model.CallvanChatMessage;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.model.enums.CallvanMessageType;
import in.koreatech.koin.domain.callvan.repository.CallvanChatMessageRepository;
import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.translation.service.TranslationAiClient;
import in.koreatech.koin.domain.translation.service.TranslationSourceFields;
import in.koreatech.koin.domain.translation.service.TranslationSourceVersion;
import in.koreatech.koin.domain.translation.service.TranslationStore;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.auth.JwtProvider;

/** MVC·원문 로더·MySQL 저장소는 실제 구현을 사용하고 AI 응답만 대체한다. */
@Transactional(propagation = NOT_SUPPORTED)
@TestPropertySource(properties = {"article.ai-summary.enabled=false", "upstage.api-key=", "translation.cleanup-cron=-"})
class CallvanTranslationApiTest extends AcceptanceTest {
    private static final String CHAT = "/callvan/posts/{postId}/chat";
    private static final String TRANSLATE = CHAT + "/messages/{messageId}/translations";
    private static final String ORIGINAL = "정문에서 오후 2시에 만나요.";

    @Autowired CallvanAcceptanceFixture fixture;
    @Autowired CallvanChatMessageRepository messages;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired JwtProvider jwtProvider;
    @Autowired JdbcTemplate jdbc;
    @Autowired TranslationStore store;
    @MockBean TranslationAiClient aiClient;
    private User author;
    private User outsider;
    private CallvanPost post;
    private CallvanChatMessage message;
    private CallvanChatMessage image;
    private String bearer;

    @BeforeEach
    void prepare() {
        clear();
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            author = fixture.콜벤_유저("callvantranslator", "콜밴참여자");
            outsider = fixture.콜벤_유저("callvanoutsider", "다른사용자");
            post = fixture.콜벤팟(author, LocalDateTime.now(clock).plusDays(1).with(LocalTime.of(14, 0)));
            message = messages.save(CallvanChatMessage.builder().chatRoom(post.getChatRoom()).sender(author)
                .senderNickname(author.getNickname()).content(ORIGINAL).build());
            image = messages.save(CallvanChatMessage.builder().chatRoom(post.getChatRoom()).sender(author)
                .senderNickname(author.getNickname()).content("https://example.com/callvan.png")
                .messageType(CallvanMessageType.IMAGE).isImage(true).build());
        });
        bearer = "Bearer " + jwtProvider.createToken(author);
    }

    @AfterEach
    void clean() {
        clear();
    }

    @Test
    void 원문_ID와_버전으로_번역하고_저장된_결과를_재사용하되_원문이_바뀌면_거절한다() throws Exception {
        var original = mockMvc.perform(get(CHAT, post.getId()).header(HttpHeaders.AUTHORIZATION, bearer))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.messages[0].message_id").value(message.getId()))
            .andExpect(jsonPath("$.messages[0].content").value(ORIGINAL))
            .andExpect(jsonPath("$.messages[1].translation_source_version").isEmpty()).andReturn();
        String version = JsonAssertions.convertJsonNode(original).path("messages").get(0)
            .path("translation_source_version").asText();
        assertThat(version).isEqualTo(TranslationSourceVersion.of(TranslationSourceFields.message(ORIGINAL)));

        String roomId = "callvan-" + post.getId() + "-" + post.getChatRoom().getId();
        mockMvc.perform(get("/chatrooms/{roomId}/messages", roomId).header(HttpHeaders.AUTHORIZATION, bearer))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].message_id").value(message.getId().toString()))
            .andExpect(jsonPath("$[0].translation_source_version").value(version))
            .andExpect(jsonPath("$[1].translation_source_version").isEmpty());

        when(aiClient.translate(Map.of("content", ORIGINAL), SupportedLanguage.ENGLISH))
            .thenReturn(Map.of("content", "Meet me at the main gate at 2 PM."));
        var translated = translate(message.getId(), bearer, version)
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data.content").value("Meet me at the main gate at 2 PM.")).andReturn();
        String id = JsonAssertions.convertJsonNode(translated).path("translation_id").asText();
        assertThat(store.findById(id).orElseThrow().expiresAt()).isEqualTo(LocalDateTime.now(clock).plusDays(1));
        assertThat(jdbc.queryForObject("SELECT translated_json FROM content_translations WHERE id = ?", String.class, id))
            .contains("Meet me at the main gate at 2 PM.");
        translate(message.getId(), bearer, version).andExpect(status().isOk())
            .andExpect(jsonPath("$.translation_id").value(id));
        mockMvc.perform(get("/translations/{id}", id).header(HttpHeaders.AUTHORIZATION, bearer))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.content").value("Meet me at the main gate at 2 PM."));
        translate(image.getId(), bearer, version).andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.status").value("UNAVAILABLE"))
            .andExpect(jsonPath("$.reason_code").value("UNSUPPORTED_CONTENT"));

        jdbc.update("UPDATE callvan_chat_message SET content = ? WHERE id = ?", "오후 3시에 만나요.", message.getId());
        translate(message.getId(), bearer, version).andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("SOURCE_CHANGED"));
        mockMvc.perform(get("/translations/{id}", id).header(HttpHeaders.AUTHORIZATION, bearer))
            .andExpect(status().isConflict());
        jdbc.update("UPDATE callvan_chat_message SET content = ? WHERE id = ?", ORIGINAL, message.getId());
        jdbc.update("UPDATE callvan_chat_room SET is_deleted = true WHERE id = ?", post.getChatRoom().getId());
        mockMvc.perform(get("/translations/{id}", id).header(HttpHeaders.AUTHORIZATION, bearer))
            .andExpect(status().isNotFound());
        translate(message.getId(), bearer, version).andExpect(status().isNotFound());
        verify(aiClient, times(1)).translate(anyMap(), any());
    }

    @Test
    void 다른_방_메시지는_번역할_수_없고_퇴장하면_저장된_결과도_조회할_수_없다() throws Exception {
        String version = TranslationSourceVersion.of(TranslationSourceFields.message(ORIGINAL));
        mockMvc.perform(post(TRANSLATE, post.getId(), message.getId()).contentType(MediaType.APPLICATION_JSON)
            .content(body(version))).andExpect(status().isUnauthorized());
        translate(message.getId(), "Bearer " + jwtProvider.createToken(outsider), version)
            .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN_PARTICIPANT"));
        var otherMessage = new TransactionTemplate(transactionManager).execute(status -> {
            var otherPost = fixture.콜벤팟(author, LocalDateTime.now(clock).plusDays(2));
            return messages.save(CallvanChatMessage.builder().chatRoom(otherPost.getChatRoom()).sender(author)
                .senderNickname(author.getNickname()).content(ORIGINAL).build());
        });
        translate(otherMessage.getId(), bearer, version).andExpect(status().isNotFound());
        verifyNoInteractions(aiClient);

        when(aiClient.translate(anyMap(), eq(SupportedLanguage.ENGLISH))).thenReturn(Map.of("content", "Let's meet."));
        var translated = translate(message.getId(), bearer, version).andExpect(status().isOk()).andReturn();
        String id = JsonAssertions.convertJsonNode(translated).path("translation_id").asText();
        mockMvc.perform(get("/translations/{id}", id)).andExpect(status().isUnauthorized());
        jdbc.update("UPDATE callvan_participant SET is_deleted = true WHERE post_id = ? AND member_id = ?",
            post.getId(), author.getId());
        mockMvc.perform(get(CHAT, post.getId()).header(HttpHeaders.AUTHORIZATION, bearer))
            .andExpect(status().isForbidden());
        translate(message.getId(), bearer, version).andExpect(status().isForbidden());
        mockMvc.perform(get("/translations/{id}", id).header(HttpHeaders.AUTHORIZATION, bearer))
            .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN_PARTICIPANT"));
        verify(aiClient, times(1)).translate(anyMap(), any());
    }

    @Test
    void 채팅_표시는_선택한_언어를_따르고_직접_입력한_지명과_원문_버전은_유지한다() throws Exception {
        String version = TranslationSourceVersion.of(TranslationSourceFields.message(ORIGINAL));
        // 실제 생성 API는 일반 장소명도 custom_name 컬럼에 함께 저장한다.
        jdbc.update("UPDATE callvan_post SET departure_custom_name = ?, arrival_custom_name = ? WHERE id = ?",
            "정문", "천안역", post.getId());
        mockMvc.perform(get(CHAT, post.getId()).header(HttpHeaders.AUTHORIZATION, bearer))
            .andExpect(status().isOk()).andExpect(jsonPath("$.room_name").value("정문 -> 천안역 (14:00) (4명)"))
            .andExpect(jsonPath("$.messages[0].time").value("오후 12:00"));
        mockMvc.perform(get(CHAT, post.getId()).header(HttpHeaders.AUTHORIZATION, bearer).param("language", "en-US"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.room_name").value("정문 -> Cheonan Station (14:00) (4 people)"))
            .andExpect(jsonPath("$.messages[0].time").value("PM 12:00"))
            .andExpect(jsonPath("$.messages[0].content").value(ORIGINAL))
            .andExpect(jsonPath("$.messages[0].translation_source_version").value(version));
        String roomId = "callvan-" + post.getId() + "-" + post.getChatRoom().getId();
        mockMvc.perform(get("/chatrooms/{roomId}", roomId).header(HttpHeaders.AUTHORIZATION, bearer)
                .param("language", "en-US"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.room_name").value("정문 -> Cheonan Station (14:00) (4 people)"));
        mockMvc.perform(get("/chatrooms").header(HttpHeaders.AUTHORIZATION, bearer)
                .param("domain_type", "CALLVAN").param("language", "en-US"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].room_name").value("정문 -> Cheonan Station (14:00) (4 people)"));
        jdbc.update("UPDATE callvan_post SET arrival_type = 'CUSTOM', arrival_custom_name = ? WHERE id = ?",
            "천안역 동쪽 출구", post.getId());
        mockMvc.perform(get(CHAT, post.getId()).header(HttpHeaders.AUTHORIZATION, bearer).param("language", "en-US"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.room_name").value("정문 -> 천안역 동쪽 출구 (14:00) (4 people)"));
        mockMvc.perform(get(CHAT, post.getId()).header(HttpHeaders.AUTHORIZATION, bearer).param("language", "fr-FR"))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(aiClient);
    }

    private ResultActions translate(Integer messageId, String token, String version) throws Exception {
        return mockMvc.perform(post(TRANSLATE, post.getId(), messageId).header(HttpHeaders.AUTHORIZATION, token)
            .contentType(MediaType.APPLICATION_JSON).content(body(version)));
    }

    private String body(String version) {
        return "{\"target_language\":\"en-US\",\"expected_source_version\":\"" + version + "\"}";
    }
}
