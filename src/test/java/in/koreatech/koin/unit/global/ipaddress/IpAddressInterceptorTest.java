package in.koreatech.koin.unit.global.ipaddress;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;

import in.koreatech.koin.global.ipaddress.IpAddressInterceptor;

class IpAddressInterceptorTest {

    private static final String REMOTE_ADDRESS = "192.0.2.10";

    @ParameterizedTest
    @ValueSource(strings = {"203.0.113.10", "2001:db8::1234", "::ffff:192.0.2.1"})
    void 단일_IP는_기존_표기를_유지한다(String ipAddress) {
        MockHttpServletRequest request = request();
        request.addHeader("X-Forwarded-For", ipAddress);

        assertThat(IpAddressInterceptor.getClientIP(request)).isEqualTo(ipAddress);
    }

    @Test
    void 긴_XFF_목록에서_첫_IP만_공백을_제거해_추출한다() {
        MockHttpServletRequest request = request();
        request.addHeader("X-Forwarded-For", " 2001:db8:1234:5678:9abc:def0:1234:5678 , 10.0.0.12, 10.0.0.13");

        assertThat(IpAddressInterceptor.getClientIP(request))
            .isEqualTo("2001:db8:1234:5678:9abc:def0:1234:5678");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "", " ", "unknown", "localhost", "example.com", "256.0.0.1", "127.1", "1.2.3", "1.2.3.4.5",
        "1.2.3.-1", "1.2.3.01", "192.0.2.1:443", "[2001:db8::1]:443", "[2001:db8::1]", "fe80::1%eth0",
        "2001:db8:::1", ", 203.0.113.20", "unknown, 203.0.113.20", "example.com, 203.0.113.20"
    })
    void 첫_주소가_잘못되면_후속_주소나_헤더가_아닌_접속_IP를_사용한다(String forwardedFor) {
        MockHttpServletRequest request = request();
        request.addHeader("X-Forwarded-For", forwardedFor);
        request.addHeader("Proxy-Client-IP", "203.0.113.30");

        assertThat(IpAddressInterceptor.getClientIP(request)).isEqualTo(REMOTE_ADDRESS);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"
    })
    void 기존_IP_헤더의_첫_주소를_사용한다(String headerName) {
        MockHttpServletRequest request = request();
        request.addHeader(headerName, "203.0.113.20, 10.0.0.1");

        assertThat(IpAddressInterceptor.getClientIP(request)).isEqualTo("203.0.113.20");
    }

    @ParameterizedTest
    @CsvSource({
        "X-Forwarded-For, Proxy-Client-IP",
        "Proxy-Client-IP, WL-Proxy-Client-IP",
        "WL-Proxy-Client-IP, HTTP_CLIENT_IP",
        "HTTP_CLIENT_IP, HTTP_X_FORWARDED_FOR"
    })
    void 기존_헤더_우선순위를_유지한다(String firstHeader, String nextHeader) {
        MockHttpServletRequest request = request();
        request.addHeader(firstHeader, "203.0.113.20");
        request.addHeader(nextHeader, "203.0.113.30");

        assertThat(IpAddressInterceptor.getClientIP(request)).isEqualTo("203.0.113.20");
    }

    @Test
    void 전달_헤더가_없으면_접속_IP를_사용한다() {
        assertThat(IpAddressInterceptor.getClientIP(request())).isEqualTo(REMOTE_ADDRESS);
    }

    @Test
    void 전달_헤더와_접속_IP가_모두_잘못되면_IP를_반환하지_않는다() {
        MockHttpServletRequest request = request();
        request.addHeader("X-Forwarded-For", "unknown");
        request.setRemoteAddr("not-an-ip");

        assertThat(IpAddressInterceptor.getClientIP(request)).isNull();
    }

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(REMOTE_ADDRESS);
        return request;
    }
}
