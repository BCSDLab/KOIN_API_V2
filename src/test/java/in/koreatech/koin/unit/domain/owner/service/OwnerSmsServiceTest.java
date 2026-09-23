package in.koreatech.koin.unit.domain.owner.service;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import in.koreatech.koin.domain.owner.dto.sms.OwnerPasswordUpdateSmsRequest;
import in.koreatech.koin.domain.owner.dto.sms.OwnerSendSmsRequest;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.owner.service.OwnerSmsService;
import in.koreatech.koin.domain.owner.service.OwnerUtilService;
import in.koreatech.koin.domain.owner.service.OwnerVerificationService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.service.RefreshTokenService;

@ExtendWith(MockitoExtension.class)
class OwnerSmsServiceTest {

    private static final String ACCOUNT = "01012345678";

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private OwnerVerificationService ownerVerificationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    private OwnerSmsService ownerSmsService;
    private User user;

    @BeforeEach
    void setUp() {
        OwnerUtilService ownerUtilService = new OwnerUtilService(ownerRepository, null, null, null);
        ownerSmsService = new OwnerSmsService(
            null, passwordEncoder, ownerRepository, null, null, null,
            ownerUtilService, ownerVerificationService, refreshTokenService
        );
        user = User.builder()
            .id(42)
            .userType(OWNER)
            .loginPw("old-password")
            .isDeleted(false)
            .build();
        Owner owner = Owner.builder()
            .account(ACCOUNT)
            .user(user)
            .grantShop(false)
            .grantEvent(false)
            .build();
        when(ownerRepository.getByAccount(ACCOUNT)).thenCallRealMethod();
        when(ownerRepository.findByAccount(ACCOUNT)).thenReturn(Optional.of(owner));
    }

    @Test
    void 비밀번호_재설정_인증번호를_사장님_계정_전화번호로_발송한다() {
        ownerSmsService.sendResetPasswordBySms(new OwnerSendSmsRequest(ACCOUNT));

        assertThat(user.getPhoneNumber()).isNull();
        verify(ownerRepository).findByAccount(ACCOUNT);
        verify(ownerVerificationService).sendCertificationSms(ACCOUNT);
    }

    @Test
    void 사장님_계정_전화번호로_비밀번호를_변경한다() {
        when(passwordEncoder.encode("newpassword")).thenReturn("encoded-password");

        ownerSmsService.updatePasswordBySms(new OwnerPasswordUpdateSmsRequest(ACCOUNT, "new-password"));

        assertThat(user.getPhoneNumber()).isNull();
        assertThat(user.getLoginPw()).isEqualTo("encoded-password");
        verify(ownerRepository).findByAccount(ACCOUNT);
        verify(refreshTokenService).deleteAllRefreshTokens(42);
    }
}
