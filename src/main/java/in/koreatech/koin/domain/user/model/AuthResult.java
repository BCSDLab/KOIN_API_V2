package in.koreatech.koin.domain.user.model;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.servlet.ModelAndView;

public class AuthResult {

    private final Optional<User> user;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public AuthResult(Optional<User> user, ApplicationEventPublisher eventPublisher, Clock clock) {
        this.user = user;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    public ModelAndView toModelAndViewForStudent() {
        return user.map(user -> {
            if (user.getAuthExpiredAt().isBefore(LocalDateTime.now(clock))) {
                return createErrorModelAndView("이미 만료된 토큰입니다.");
            }
            if (!user.isAuthed()) {
                user.auth();
                eventPublisher.publishEvent(new StudentRegisterEvent(user.getEmail()));
                return createSuccessModelAndView();
            }
            return createErrorModelAndView("이미 인증된 사용자입니다.");
        }).orElseGet(() -> createErrorModelAndView("토큰에 해당하는 사용자를 찾을 수 없습니다."));
    }

    private ModelAndView createErrorModelAndView(String errorMessage) {
        ModelAndView modelAndView = new ModelAndView("error_config");
        modelAndView.addObject("errorMessage", errorMessage);
        return modelAndView;
    }

    private ModelAndView createSuccessModelAndView() {
        return new ModelAndView("success_register_config");
    }
}
