package in.koreatech.koin.domain.dining.service;

import static in.koreatech.koin.domain.dining.model.DiningReportChange.EventType.CREATED;
import static in.koreatech.koin.domain.dining.model.DiningReportChange.EventType.PROCESSED;
import static in.koreatech.koin.domain.dining.model.DiningReportProcessingType.COOP_PREPROCESSED;
import static in.koreatech.koin.domain.dining.model.DiningReportProcessingType.MANUAL;
import static in.koreatech.koin.domain.dining.model.DiningReportProcessingType.SAME_DINING_APPROVED;
import static in.koreatech.koin.domain.dining.model.DiningReportStatus.APPROVED;
import static in.koreatech.koin.domain.dining.model.DiningReportStatus.PENDING;
import static in.koreatech.koin.domain.dining.model.DiningReportStatus.REJECTED;
import static in.koreatech.koin.global.code.ApiResponseCode.*;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.AmazonClientException;

import in.koreatech.koin.common.event.DiningSoldOutEvent;
import in.koreatech.koin.domain.coop.exception.MenuNotFoundException;
import in.koreatech.koin.domain.coop.repository.DiningSoldOutCacheRepository;
import in.koreatech.koin.domain.coopshop.model.CoopShopType;
import in.koreatech.koin.domain.coopshop.service.CoopShopService;
import in.koreatech.koin.domain.dining.dto.DiningReportActor;
import in.koreatech.koin.domain.dining.dto.DiningReportCreateRequest;
import in.koreatech.koin.domain.dining.dto.DiningReportCreateResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportDecisionResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportResponse;
import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.dining.model.DiningReport;
import in.koreatech.koin.domain.dining.model.DiningReportStatus;
import in.koreatech.koin.domain.dining.model.DiningSoldOutSource;
import in.koreatech.koin.domain.dining.repository.DiningReportRepository;
import in.koreatech.koin.domain.dining.repository.DiningRepository;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.global.exception.custom.KoinIllegalStateException;
import in.koreatech.koin.infrastructure.s3.client.S3Client;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
@RequiredArgsConstructor
public class DiningReportService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final DiningRepository diningRepository;
    private final DiningReportRepository reportRepository;
    private final DiningReportChangeService changeService;
    private final DiningSoldOutCacheRepository soldOutCacheRepository;
    private final CoopShopService coopShopService;
    private final ApplicationEventPublisher eventPublisher;
    private final S3Client s3Client;
    private final Clock clock;

    public DiningReportCreateResponse create(Integer reporterId, Integer diningId, UUID requestKey,
        DiningReportCreateRequest request) {
        Optional<DiningReport> previous = reportRepository.findByReporterIdAndRequestKey(reporterId, requestKey);
        if (previous.isPresent()) {
            return replayCreation(previous.get(), diningId, request.imageUrl());
        }

        validateImage(request.imageUrl());
        Dining dining = lockDining(diningId);
        previous = reportRepository.findRequestForUpdate(reporterId, requestKey);
        if (previous.isPresent()) {
            return replayCreation(previous.get(), diningId, request.imageUrl());
        }
        List<DiningReport> reports = reportRepository.findAllByDiningIdForUpdate(diningId);
        if (reports.stream().anyMatch(report -> Objects.equals(report.getReporterId(), reporterId))) {
            throw CustomException.of(DINING_REPORT_ALREADY_SUBMITTED);
        }
        LocalDateTime now = now();
        if (!dining.getDate().equals(now.toLocalDate())) {
            throw CustomException.of(DINING_REPORT_DATE_NOT_ALLOWED);
        }
        if (dining.getSoldOut() != null) {
            throw CustomException.of(DINING_ALREADY_SOLD_OUT);
        }
        DiningReport report = DiningReport.create(dining, reporterId, request.imageUrl(), requestKey, now);
        try {
            reportRepository.saveAndFlush(report);
        } catch (DataIntegrityViolationException exception) {
            String detail = exception.getMostSpecificCause().getMessage();
            if (detail != null && detail.contains("uk_dining_report_request")) {
                throw CustomException.of(IDEMPOTENCY_KEY_CONFLICT);
            }
            if (detail != null && detail.contains("uk_dining_report_student")) {
                throw CustomException.of(DINING_REPORT_ALREADY_SUBMITTED);
            }
            throw exception;
        }
        changeService.append(List.of(report), CREATED, now);
        return DiningReportCreateResponse.from(report);
    }

    public DiningReportDecisionResponse approve(Integer reportId, DiningReportActor actor) {
        return decide(reportId, actor, APPROVED);
    }

    public DiningReportDecisionResponse reject(Integer reportId, DiningReportActor actor) {
        return decide(reportId, actor, REJECTED);
    }

    public void changeSoldOutByCoop(Integer diningId, boolean soldOut) {
        Dining dining = diningRepository.findByIdForUpdate(diningId)
            .orElseThrow(() -> MenuNotFoundException.withDetail("menuId: " + diningId));
        if (!soldOut) {
            dining.cancelSoldOut();
            return;
        }
        List<DiningReport> pending = reportRepository.findAllByDiningIdForUpdate(diningId).stream()
            .filter(report -> report.getStatus() == PENDING).toList();
        LocalDateTime now = now();
        boolean changed = dining.markSoldOut(now, DiningSoldOutSource.COOP);
        UUID batchId = UUID.randomUUID();
        for (DiningReport report : pending) {
            report.process(REJECTED, COOP_PREPROCESSED, batchId, null, now, null, null, null);
        }
        if (changed) {
            publishSoldOut(dining, now);
        }
        changeService.append(pending, PROCESSED, now);
    }

    private DiningReportDecisionResponse decide(Integer reportId, DiningReportActor actor,
        DiningReportStatus result) {
        Integer diningId = reportRepository.findDiningIdById(reportId)
            .orElseThrow(() -> CustomException.of(NOT_FOUND_DINING_REPORT));
        Dining dining = lockDining(diningId);
        List<DiningReport> reports = reportRepository.findAllByDiningIdForUpdate(diningId);
        DiningReport target = reports.stream().filter(report -> report.getId().equals(reportId)).findFirst()
            .orElseThrow(() -> CustomException.of(NOT_FOUND_DINING_REPORT));
        if (target.getStatus() != PENDING) {
            if (target.getStatus() != result) {
                throw CustomException.of(DINING_REPORT_ALREADY_PROCESSED);
            }
            List<Integer> ids = reports.stream()
                .filter(report -> Objects.equals(report.getProcessingId(), target.getProcessingId()))
                .map(DiningReport::getId).toList();
            return new DiningReportDecisionResponse(DiningReportResponse.from(target), ids, true);
        }

        LocalDateTime now = now();
        UUID batchId = UUID.randomUUID();
        List<DiningReport> affected = result == APPROVED
            ? reports.stream().filter(report -> report.getStatus() == PENDING).toList() : List.of(target);
        for (DiningReport report : affected) {
            boolean manual = report.getId().equals(reportId);
            report.process(result, manual ? MANUAL : SAME_DINING_APPROVED, batchId,
                manual ? null : reportId, now, manual ? actor.workspaceId() : null,
                manual ? actor.userId() : null, manual ? actor.displayName() : null);
        }
        if (result == APPROVED && dining.markSoldOut(now, DiningSoldOutSource.REPORT)) {
            publishSoldOut(dining, now);
        }
        changeService.append(affected, PROCESSED, now);
        return new DiningReportDecisionResponse(DiningReportResponse.from(target),
            affected.stream().map(DiningReport::getId).toList(), false);
    }

    private DiningReportCreateResponse replayCreation(DiningReport report, Integer diningId, String imageUrl) {
        if (!report.getDining().getId().equals(diningId) || !report.getImageUrl().equals(imageUrl)) {
            throw CustomException.of(IDEMPOTENCY_KEY_CONFLICT);
        }
        return DiningReportCreateResponse.from(report);
    }

    private Dining lockDining(Integer diningId) {
        return diningRepository.findByIdForUpdate(diningId)
            .orElseThrow(() -> CustomException.of(NOT_FOUND_DINING));
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock.withZone(KST)).withNano(0);
    }

    private void validateImage(String imageUrl) {
        if (imageUrl == null) {
            throw CustomException.of(INVALID_REPORT_IMAGE);
        }
        String key;
        try {
            key = s3Client.extractKeyFromUrl(imageUrl);
            // 업로드 키는 원문을 유지하고, 공백과 '%'는 URI 검증에만 이스케이프한다.
            String validationUrl = s3Client.getDomainUrlPrefix() + key.replace("%", "%25").replace(" ", "%20");
            URI uri = URI.create(validationUrl);
            if (!s3Client.isCustomDomainUrl(validationUrl) || uri.getQuery() != null || uri.getFragment() != null
                || !key.startsWith("upload/COOP/") || key.endsWith("/")) {
                throw CustomException.of(INVALID_REPORT_IMAGE);
            }
        } catch (IllegalArgumentException | KoinIllegalStateException exception) {
            throw CustomException.of(INVALID_REPORT_IMAGE);
        }
        try {
            if (!s3Client.doesFileExist(key)) {
                throw CustomException.of(INVALID_REPORT_IMAGE);
            }
        } catch (AmazonClientException exception) {
            throw CustomException.of(IMAGE_STORAGE_UNAVAILABLE);
        }
    }

    private void publishSoldOut(Dining dining, LocalDateTime now) {
        if (dining.getDate().equals(now.toLocalDate())
            && coopShopService.getIsOpened(now, CoopShopType.CAFETERIA, dining.getType(), false)
            && soldOutCacheRepository.findById(dining.getPlace()).isEmpty()) {
            eventPublisher.publishEvent(new DiningSoldOutEvent(dining.getId(), dining.getPlace(), dining.getType()));
        }
    }
}
