package in.koreatech.koin.support;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonAssertions {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JsonStringAssert assertThat(String expect) {
        return new JsonStringAssert(expect);
    }

    public static class JsonStringAssert {

        private final String expect;

        JsonStringAssert(String expect) {
            this.expect = expect;
        }

        public void isEqualTo(String actual) {
            try {
                Object responseObj = parseJson(expect);
                Object expectedObj = parseJson(actual);

                Assertions.assertThat(responseObj).isEqualTo(expectedObj);
            } catch (Exception e) {
                throw new AssertionError("json parsing error\n" + e.getMessage());
            }
        }

        private Object parseJson(String json) throws IOException {
            try {
                return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
                });
            } catch (IOException e) {
                return objectMapper.readValue(json, new TypeReference<List<Object>>() {
                });
            }
        }
    }
}
