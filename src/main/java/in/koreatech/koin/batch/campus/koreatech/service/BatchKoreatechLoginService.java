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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.batch.campus.koreatech.model.HttpCookieJar;
import in.koreatech.koin.batch.campus.koreatech.repository.HttpCookieJarRepository;
import in.koreatech.koin.global.exception.KoinIllegalStateException;
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
            checkLoginId();

            setCookie("kut_login_type", "id", "/", "portal.koreatech.ac.kr");
            checkSecondLoginCert();

            ssoAssert();
            ssoLogin();

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

    private void checkLoginId() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://portal.koreatech.ac.kr/ktp/login/checkLoginId.do"))
            .header("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
            .headers(headers)
            .POST(HttpRequest.BodyPublishers.ofString("login_id=" + userId + "&login_pwd=" + userPwd))
            .build();

        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void checkSecondLoginCert() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://portal.koreatech.ac.kr/ktp/login/checkSecondLoginCert.do"))
            .header("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
            .headers(headers)
            .POST(HttpRequest.BodyPublishers.ofString("login_id=" + userId))
            .build();

        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void ssoAssert() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://portal.koreatech.ac.kr/exsignon/sso/sso_assert.jsp"))
            .headers(headers)
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();

        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void ssoLogin() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://kut90.koreatech.ac.kr/ssoLogin_ext.jsp?&PGM_ID=CO::CO0998W&locale=ko"))
            .headers(headers)
            .GET()
            .build();

        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void setCookie(String name, String value, String path, String domain) {
        CookieStore cookieStore = cookieManager.getCookieStore();

        HttpCookie cookie = new HttpCookie(name, value);
        cookie.setPath(path);
        cookieStore.add(URI.create("https://" + domain), cookie);
    }
}
