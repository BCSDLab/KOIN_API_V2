package in.koreatech.koin.global.log;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.jboss.logging.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.PathMatcher;
import org.springframework.util.StopWatch;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestLoggingFilter implements Filter {

    public static final String REQUEST_ID = "requestId";

    private final ObjectProvider<PathMatcher> pathMatcherProvider;
    private final Set<String> setIgnoredUrlPatterns = new HashSet<>();

    public void setIgnoredUrlPatterns(String... ignoredUrlPatterns) {
        this.setIgnoredUrlPatterns.addAll(Arrays.asList(ignoredUrlPatterns));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (CorsUtils.isPreFlightRequest(httpRequest) || isIgnoredUrl(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        var cachedRequest = new ContentCachingRequestWrapper((HttpServletRequest) request);
        StopWatch stopWatch = new StopWatch();
        try {
            MDC.put(REQUEST_ID, getRequestId(httpRequest));
            stopWatch.start();
            log.info("request start [uri: {} {}]", httpRequest.getMethod(), httpRequest.getRequestURI());
            log.info("request header: {}", getHeaders(cachedRequest));
            log.info("request query string: {}", getQueryString(cachedRequest));
            log.info("request body: {}", getRequestBody(cachedRequest));
            chain.doFilter(cachedRequest, response);
        } finally {
            stopWatch.stop();
            log.info("request end [time: {}ms]", stopWatch.getTotalTimeMillis());
            MDC.clear();
        }
    }

    private boolean isIgnoredUrl(HttpServletRequest request) {
        PathMatcher pathMatcher = this.pathMatcherProvider.getIfAvailable();
        Objects.requireNonNull(pathMatcher);
        return setIgnoredUrlPatterns.stream()
            .anyMatch(pattern -> pathMatcher.match(pattern, request.getRequestURI()));
    }

    private String getRequestId(HttpServletRequest httpRequest) {
        String requestId = httpRequest.getHeader("X-Request-ID");
        if (ObjectUtils.isEmpty(requestId)) {
            return UUID.randomUUID().toString().replace("-", "");
        }
        return requestId;
    }

    private Map<String, Object> getHeaders(HttpServletRequest request) {
        Map<String, Object> headerMap = new HashMap<>();
        Enumeration<String> headerArray = request.getHeaderNames();
        while (headerArray.hasMoreElements()) {
            String headerName = headerArray.nextElement();
            headerMap.put(headerName, request.getHeader(headerName));
        }
        return headerMap;
    }

    private String getQueryString(HttpServletRequest httpRequest) {
        String queryString = httpRequest.getQueryString();
        if (queryString == null) {
            return " - ";
        }
        return queryString;
    }

    private String getRequestBody(HttpServletRequest request) {
        var wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper == null) {
            return " - ";
        }
        try {
            // body 가 읽히지 않고 예외처리 되는 경우에 캐시하기 위함
            wrapper.getInputStream().readAllBytes();
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length == 0) {
                return " - ";
            }
            return new String(buf, wrapper.getCharacterEncoding());
        } catch (Exception e
        ) {
            return " - ";
        }
    }
}
