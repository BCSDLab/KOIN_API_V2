package in.koreatech.koin.batch.campus.koreatech.service;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.exception.custom.KoinIllegalStateException;
import in.koreatech.koin.batch.campus.koreatech.model.HttpCookieJar;
import in.koreatech.koin.batch.campus.koreatech.repository.HttpCookieJarRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class BatchKoreatechLoginService {

    private static final String COOKIE_JAR_ID = "KOREATECH";

    private final HttpCookieJarRepository httpCookieJarRepository;

    private final String userId;
    private final String userPwd;
    private final String[] headers;

    private final CookieManager cookieManager;
    private final HttpClient httpClient;

    public BatchKoreatechLoginService(
        HttpCookieJarRepository httpCookieJarRepository,
        @Value("${portal.koreatech.id}") String userId,
        @Value("${portal.koreatech.password}") String userPwd,
        @Value("${portal.koreatech.ip}") String ip
    ) {
        this.httpCookieJarRepository = httpCookieJarRepository;

        this.userId = userId;
        this.userPwd = userPwd;
        this.headers = new String[] {
            "X-Forwarded-For", ip,
            "X-Real-IP", ip,
        };

        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        this.httpClient = HttpClient.newBuilder()
            .cookieHandler(cookieManager)
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    public void login() {
        List<HttpCookie> cookies;
        try {
            // 통합 로그인
            requestPost(
                "https://tsso.koreatech.ac.kr/svc/tk/Login.do",
                "RelayState=/exsignon/sso/main.jsp&id=testwww&targetId=testwww&" +
                    "user_id=" + userId + "&user_password=" + userPwd
            );
            requestGet("https://www.koreatech.ac.kr/sso/sessionChecker.es");

            // 아우누리 로드
            requestGet("https://portal.koreatech.ac.kr");

            // 식단 로드
            requestGet("https://kut90.koreatech.ac.kr/ssoLogin_ext.jsp?&PGM_ID=CO::CO0998W&locale=ko");

            // 학생종합경력개 로드
            jobLogin();

            cookies = new ArrayList<>(cookieManager.getCookieStore().getCookies());
        } catch (IOException e) {
            throw new KoinIllegalStateException("");
        } catch (InterruptedException e) { // TODO : 잘못된 에러 처리 수정하기
            throw new RuntimeException(e);
        } finally {
            cookieManager.getCookieStore().removeAll();
        }

        httpCookieJarRepository.save(HttpCookieJar.of(COOKIE_JAR_ID, cookies));
    }

    private void jobLogin() throws IOException, InterruptedException {
        requestGet("https://job.koreatech.ac.kr/");
        requestGet("https://job.koreatech.ac.kr/Main/default.aspx");
        HttpResponse<String> response = requestGet(
            "https://tsso.koreatech.ac.kr/svc/tk/Auth.do?id=STEMS-JOB&ac=N&ifa=N&RelayState=%2fMain%2fdefault.aspx&"
        );

        log.info("Job login response: {}", response.body());

        // js 코드에서 쿠키 파싱
        Pattern cookiePattern = Pattern.compile("document\\.cookie\\s*=\\s*\"([^=]+)=([^;]+);");

        Matcher matcher = cookiePattern.matcher(response.body());
        while (matcher.find()) {
            log.info("[SIB] {} {}", matcher.group(1), matcher.group(2));
            setCookie(matcher.group(1), matcher.group(2), "/", "job.koreatech.ac.kr");
        }

        requestGet("https://job.koreatech.ac.kr/Career/");
    }

    private HttpResponse<String> requestPost(String url, String body) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
            .headers(headers);

        HttpRequest request;
        if (body == null) {
            request = requestBuilder
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        } else {
            request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        }

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> requestGet(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .headers(headers)
            .GET()
            .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void setCookie(String name, String value, String path, String domain) {
        CookieStore cookieStore = cookieManager.getCookieStore();

        HttpCookie cookie = new HttpCookie(name, value);
        cookie.setPath(path);
        cookieStore.add(URI.create("https://" + domain), cookie);
    }
}
