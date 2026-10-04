package in.koreatech.koin.unit.domain.dining.service;

import static in.koreatech.koin.global.code.ApiResponseCode.DINING_REPORT_DATE_NOT_ALLOWED;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REPORT_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import com.amazonaws.services.s3.AmazonS3;

import in.koreatech.koin.domain.coop.repository.DiningSoldOutCacheRepository;
import in.koreatech.koin.domain.coopshop.service.CoopShopService;
import in.koreatech.koin.domain.dining.dto.DiningReportCreateRequest;
import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.dining.model.DiningReport;
import in.koreatech.koin.domain.dining.model.DiningReportStatus;
import in.koreatech.koin.domain.dining.model.DiningType;
import in.koreatech.koin.domain.dining.repository.DiningReportRepository;
import in.koreatech.koin.domain.dining.repository.DiningRepository;
import in.koreatech.koin.domain.dining.service.DiningReportChangeService;
import in.koreatech.koin.domain.dining.service.DiningReportService;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.infrastructure.s3.client.S3Client;
import in.koreatech.koin.infrastructure.s3.dto.UploadUrlRequest;
import in.koreatech.koin.infrastructure.s3.model.ImageUploadDomain;
import in.koreatech.koin.infrastructure.s3.service.UploadService;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

class DiningReportImageValidationTest {

    private static final String BUCKET_NAME = "test-bucket";
    private static final String DOMAIN_URL = "https://static.koreatech.in/";
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-02T03:00:00Z"), ZoneId.of("Asia/Seoul"));

    private AmazonS3 amazonS3;
    private S3Client s3Client;
    private DiningRepository diningRepository;
    private DiningReportRepository reportRepository;
    private DiningReportService service;

    @BeforeEach
    void setUp() {
        amazonS3 = mock(AmazonS3.class);
        s3Client = new S3Client(BUCKET_NAME, DOMAIN_URL, S3Presigner.builder()
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create("test-access-key", "test-secret-key")))
            .region(Region.AP_NORTHEAST_2), amazonS3, clock);
        diningRepository = mock(DiningRepository.class);
        reportRepository = mock(DiningReportRepository.class);
        service = new DiningReportService(diningRepository, reportRepository,
            mock(DiningReportChangeService.class), mock(DiningSoldOutCacheRepository.class),
            mock(CoopShopService.class), mock(ApplicationEventPublisher.class), s3Client, clock);
    }

    @ParameterizedTest
    @ValueSource(strings = {"soldout.jpg", "soldout photo.jpg", "품절.jpg", "soldout+photo.jpg",
        "soldout%20photo.jpg", "100%.jpg"})
    void 공용_COOP_업로드의_파일명과_URL을_원문으로_접수한다(String fileName) {
        String imageUrl = new UploadService(s3Client, clock).getPresignedUrl(ImageUploadDomain.COOP,
            new UploadUrlRequest(1, "image/jpeg", fileName)).fileUrl();
        String rawKey = imageUrl.substring(DOMAIN_URL.length());
        assertThat(imageUrl).startsWith(DOMAIN_URL + "upload/COOP/2026/10/2/").endsWith("/" + fileName);
        when(amazonS3.doesObjectExist(BUCKET_NAME, rawKey)).thenReturn(true);

        Integer reporterId = 42;
        Integer diningId = 1;
        UUID requestKey = UUID.randomUUID();
        Dining dining = Dining.builder().date(LocalDate.now(clock)).type(DiningType.LUNCH)
            .place("A코너").menu("[\"돈까스\"]").build();
        when(reportRepository.findByReporterIdAndRequestKey(reporterId, requestKey)).thenReturn(Optional.empty());
        when(reportRepository.findRequestForUpdate(reporterId, requestKey)).thenReturn(Optional.empty());
        when(reportRepository.findAllByDiningIdForUpdate(diningId)).thenReturn(List.of());
        when(diningRepository.findByIdForUpdate(diningId)).thenReturn(Optional.of(dining));

        var response = service.create(reporterId, diningId, requestKey, new DiningReportCreateRequest(imageUrl));

        ArgumentCaptor<DiningReport> reportCaptor = ArgumentCaptor.forClass(DiningReport.class);
        verify(reportRepository).saveAndFlush(reportCaptor.capture());
        assertThat(response.status()).isEqualTo(DiningReportStatus.PENDING);
        assertThat(reportCaptor.getValue().getImageUrl()).isEqualTo(imageUrl);
        verify(amazonS3).doesObjectExist(BUCKET_NAME, rawKey);
    }

    @ParameterizedTest
    @CsvSource({
        "2026-10-02T14:59:59Z, 2026-10-02, true",
        "2026-10-02T15:00:00Z, 2026-10-02, false",
        "2026-10-02T15:00:00Z, 2026-10-03, true"
    })
    void KST_자정_전후에는_한국날짜의_당일_식단만_접수한다(String instant, String diningDate, boolean allowed) {
        Clock utcClock = Clock.fixed(Instant.parse(instant), ZoneOffset.UTC);
        DiningReportService boundaryService = new DiningReportService(diningRepository, reportRepository,
            mock(DiningReportChangeService.class), mock(DiningSoldOutCacheRepository.class),
            mock(CoopShopService.class), mock(ApplicationEventPublisher.class), s3Client, utcClock);
        String imageUrl = new UploadService(s3Client, clock).getPresignedUrl(ImageUploadDomain.COOP,
            new UploadUrlRequest(1, "image/jpeg", "soldout.jpg")).fileUrl();
        when(amazonS3.doesObjectExist(BUCKET_NAME, imageUrl.substring(DOMAIN_URL.length()))).thenReturn(true);
        Dining dining = Dining.builder().date(LocalDate.parse(diningDate)).type(DiningType.LUNCH)
            .place("A코너").menu("[\"돈까스\"]").build();
        when(diningRepository.findByIdForUpdate(1)).thenReturn(Optional.of(dining));
        UUID requestKey = UUID.randomUUID();
        var request = new DiningReportCreateRequest(imageUrl);

        if (!allowed) {
            assertThatThrownBy(() -> boundaryService.create(42, 1, requestKey, request))
                .isInstanceOfSatisfying(CustomException.class,
                    exception -> assertThat(exception.getErrorCode()).isEqualTo(DINING_REPORT_DATE_NOT_ALLOWED));
            verify(reportRepository, never()).saveAndFlush(any(DiningReport.class));
            return;
        }

        var response = boundaryService.create(42, 1, requestKey, request);
        assertThat(response.status()).isEqualTo(DiningReportStatus.PENDING);
        assertThat(response.createdAt().toLocalDate()).isEqualTo(dining.getDate());
        verify(reportRepository).saveAndFlush(any(DiningReport.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "https://outside.example/upload/COOP/soldout.jpg",
        "https://static.koreatech.in.evil.example/upload/COOP/soldout.jpg",
        "https://user:password@static.koreatech.in/upload/COOP/soldout.jpg",
        "https://static.koreatech.in/upload/COOP/soldout.jpg?version=1",
        "https://static.koreatech.in/upload/COOP/soldout.jpg#photo",
        "https://static.koreatech.in/upload/SHOPS/soldout.jpg"
    })
    void 허용되지_않은_URL은_S3_확인과_식단_잠금_전에_거부한다(String imageUrl) {
        assertThatThrownBy(() -> service.create(42, 1, UUID.randomUUID(), new DiningReportCreateRequest(imageUrl)))
            .isInstanceOf(CustomException.class)
            .hasMessage(INVALID_REPORT_IMAGE.getMessage());
        verifyNoInteractions(amazonS3, diningRepository);
    }
}
