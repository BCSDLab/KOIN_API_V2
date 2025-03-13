package in.koreatech.koin.web.logging;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
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
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        if (CorsUtils.isPreFlightRequest(httpRequest) || isIgnoredUrl(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        var cachedRequest = new ContentCachingRequestWrapper((HttpServletRequest)request);
        StopWatch stopWatch = new StopWatch();
        try {
            MDC.put(REQUEST_ID, getRequestId(httpRequest));
            stopWatch.start();
            log.info("request start [uri: {} {}]", httpRequest.getMethod(), httpRequest.getRequestURI());
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
}
