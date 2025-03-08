package in.koreatech.koin.batch.campus.koreatech.model;

import java.net.HttpCookie;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash("HttpCookieJar")
public class HttpCookieJar {

    @Id
    private String id;

    private List<String> cookies;

    // 아우누리 포탈 로그인 세션이 6시간임
    @TimeToLive(unit = TimeUnit.MINUTES)
    private Long expiration = (5 * 60) + 30L;  // 5시간 30분

    @Builder
    private HttpCookieJar(String id, List<String> cookies, Long expiration) {
        this.id = id;
        this.cookies = cookies;
        this.expiration = expiration;
    }

    public static HttpCookieJar of(String id, List<HttpCookie> cookies) {
        return HttpCookieJar.builder()
            .id(id)
            .cookies(cookies.stream().map(HttpCookie::toString).toList())
            .expiration((5 * 60) + 30L)
            .build();
    }

    public List<HttpCookie> getCookies() {
        return cookies.stream().flatMap(s -> HttpCookie.parse(s).stream()).toList();
    }
}
