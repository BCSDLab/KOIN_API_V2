package in.koreatech.koin._common.config;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import lombok.extern.slf4j.Slf4j;

@EnableAsync
@Configuration
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        return null;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            String errorMessage = throwable.getMessage();
            String errorFile = throwable.getStackTrace()[0].getFileName();
            int errorLine = throwable.getStackTrace()[0].getLineNumber();
            String errorName = throwable.getClass().getSimpleName();
            String detail = String.format("""
                Exception: *%s*
                Location: *%s Line %d*
                
                ```%s```
                """,
                errorName, errorFile, errorLine, errorMessage);
            log.error("""
                서버에서 에러가 발생했습니다. method: {}
                {}
                """, method.getName(), detail);
        };
    }
}
