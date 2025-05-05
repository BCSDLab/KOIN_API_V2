package in.koreatech.koin.domain.kakaobot.dto;

import static lombok.AccessLevel.PRIVATE;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import in.koreatech.koin.domain.kakaobot.exception.KakaoSkillFailedException;
import lombok.NoArgsConstructor;

/**
 * <a href="https://kakaobusiness.gitbook.io/main/tool/chatbot/skill_guide/answer_json_format#skillresponse">
 *     KAKAO 비즈니스 기술문서 - Bot-Skill 서버 연동 API
 * </a>
 */
@NoArgsConstructor(access = PRIVATE)
public class KakaoSkillResponse {

    private static final int OUTPUTS_LIMIT = 3;
    private static final int QUICK_REPLIES_LIMIT = 10;
    private static final int MAX_QUICK_REPLIES_LABEL_SIZE = 8;

    private static final String SIMPLE_TEXT = "simpleText";
    public static final String QUICK_ACTION_MESSAGE = "message";

    public static SkillResponseBuilder builder() {
        return new SkillResponseBuilder();
    }

    public static class SkillResponseBuilder {

        private final JsonArray outputs = new JsonArray();
        private final JsonObject template = new JsonObject();
        private final JsonArray quickReplies = new JsonArray();
        private final JsonObject skillPayload = new JsonObject();

        public SkillResponseBuilder() {
            skillPayload.addProperty("version", "2.0");
            skillPayload.add("template", template);
            template.add("outputs", outputs);
            template.add("quickReplies", quickReplies);
        }

        public SkillResponseBuilder simpleText(String text) {

            if (outputs.size() > OUTPUTS_LIMIT) {
                throw new KakaoSkillFailedException("outputs의 제한은 1개 이상 3개 이하입니다.");
            }
            JsonObject field = new JsonObject();
            JsonObject type = new JsonObject();
            field.addProperty("text", text);
            type.add(SIMPLE_TEXT, field);
            outputs.add(type);
            return this;
        }

        public SkillResponseBuilder quickReply(String label, String action, String messageText) {
            if (quickReplies.size() > QUICK_REPLIES_LIMIT) {
                throw new KakaoSkillFailedException("quickReplies의 제한은 10개 이하입니다.");
            }
            if (!QUICK_ACTION_MESSAGE.equals(action)) {
                throw new KakaoSkillFailedException("quickReplies의 action이 올바르게 설정되지 않았습니다.");
            }

            if (label.length() > MAX_QUICK_REPLIES_LABEL_SIZE) {
                throw new KakaoSkillFailedException("quickReplies에서 label은 최대 8자 제한입니다.");
            }

            JsonObject field = new JsonObject();
            field.addProperty("action", action);
            field.addProperty("label", label);
            field.addProperty("messageText", messageText);

            quickReplies.add(field);
            return this;
        }

        public String build() {
            if (outputs.isEmpty()) {
                throw new KakaoSkillFailedException("outputs는 필수 항목입니다.");
            }
            return skillPayload.toString();
        }
    }
}
