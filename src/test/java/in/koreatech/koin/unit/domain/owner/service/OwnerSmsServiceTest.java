package in.koreatech.koin.unit.domain.owner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.domain.owner.dto.sms.OwnerPasswordUpdateSmsRequest;
import in.koreatech.koin.domain.owner.dto.sms.OwnerSendSmsRequest;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.service.OwnerSmsService;
import in.koreatech.koin.domain.owner.service.OwnerUtilService;
import in.koreatech.koin.domain.owner.service.OwnerVerificationService;
import in.koreatech.koin.domain.user.service.RefreshTokenService;
import in.koreatech.koin.unit.fixture.OwnerFixture;

@ExtendWith(MockitoExtension.class)
class OwnerSmsServiceTest {

    private static final Integer OWNER_ID = 42;

    @InjectMocks
    private OwnerSmsService ownerSmsService;

    @Mock
    private OwnerUtilService ownerUtilService;

    @Mock
    private OwnerVerificationService ownerVerificationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    private Owner owner;

    @BeforeEach
    void setUp() {
        owner = OwnerFixture.현수_사장님();
        ReflectionTestUtils.setField(owner.getUser(), "id", OWNER_ID);
        ReflectionTestUtils.setField(owner.getUser(), "phoneNumber", null);
        when(ownerUtilService.extractUserByAccount(owner.getAccount())).thenReturn(owner.getUser());
    }

    @Test
    void 비밀번호_재설정_인증번호를_사장님_계정_전화번호로_발송한다() {
        ownerSmsService.sendResetPasswordBySms(new OwnerSendSmsRequest(owner.getAccount()));

        verify(ownerUtilService).extractUserByAccount(owner.getAccount());
        verify(ownerVerificationService).sendCertificationSms(owner.getAccount());
    }

    @Test
    void 사장님_계정_전화번호로_비밀번호를_변경한다() {
        when(passwordEncoder.encode("newpassword")).thenReturn("encoded-password");

        ownerSmsService.updatePasswordBySms(new OwnerPasswordUpdateSmsRequest(owner.getAccount(), "new-password"));

        assertThat(owner.getUser().getLoginPw()).isEqualTo("encoded-password");
        verify(ownerUtilService).extractUserByAccount(owner.getAccount());
        verify(refreshTokenService).deleteAllRefreshTokens(OWNER_ID);
    }
}
