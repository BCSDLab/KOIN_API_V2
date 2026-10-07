package in.koreatech.koin.domain.dining.service;

import static in.koreatech.koin.global.code.ApiResponseCode.DINING_REPORT_DELIVERY_CONFLICT;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_DINING_REPORT;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_DINING_REPORT_DELIVERY;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.domain.dining.dto.DiningReportDeliveryResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportDeliveryResultRequest;
import in.koreatech.koin.domain.dining.dto.DiningReportDeliveryResultResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportResponse;
import in.koreatech.koin.domain.dining.model.DiningReport;
import in.koreatech.koin.domain.dining.model.DiningReportChange;
import in.koreatech.koin.domain.dining.model.DiningReportDeliveryStatus;
import in.koreatech.koin.domain.dining.repository.DiningReportChangeRepository;
import in.koreatech.koin.domain.dining.repository.DiningReportRepository;
import in.koreatech.koin.domain.dining.repository.DiningReportSequenceRepository;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiningReportDeliveryService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final DiningReportSequenceRepository sequenceRepository;
    private final DiningReportChangeRepository changeRepository;
    private final DiningReportRepository reportRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Optional<DiningReportDeliveryResponse> claim() {
        lockSequence();
        LocalDateTime now = now();
        return changeRepository.findClaimable(now, PageRequest.of(0, 1)).stream().findFirst().map(change -> {
            // Old-version inserts omit task metadata. Read committed state without reversing business lock order.
            String snapshot = change.getReportSnapshot();
            if (snapshot == null) {
                DiningReport report = reportRepository.findById(change.getReportId())
                    .orElseThrow(() -> CustomException.of(NOT_FOUND_DINING_REPORT));
                snapshot = serialize(DiningReportResponse.from(report));
            }
            change.initializeDelivery(snapshot);
            change.issue(now);
            return new DiningReportDeliveryResponse(change.getDeliveryId(), change.getAttemptToken(),
                change.getExpiresAt().atZone(KST).toOffsetDateTime(), snapshot(change.getReportSnapshot()));
        });
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public DiningReportDeliveryResultResponse recordResult(UUID deliveryId, DiningReportDeliveryResultRequest request) {
        lockSequence();
        DiningReportChange change = changeRepository.findByDeliveryId(deliveryId)
            .orElseThrow(() -> CustomException.of(NOT_FOUND_DINING_REPORT_DELIVERY));
        if (!Objects.equals(request.attemptToken(), change.getAttemptToken())) {
            if (changeRepository.findByAttemptToken(request.attemptToken())
                .filter(other -> !deliveryId.equals(other.getDeliveryId())).isPresent()) {
                throw CustomException.of(NOT_FOUND_DINING_REPORT_DELIVERY);
            }
            throw CustomException.of(DINING_REPORT_DELIVERY_CONFLICT);
        }
        if (change.getAcceptedOutcome() != null) {
            if (change.getAcceptedOutcome() != request.outcome()) {
                throw CustomException.of(DINING_REPORT_DELIVERY_CONFLICT);
            }
            return result(change);
        }
        if (change.getDeliveryState() != DiningReportDeliveryStatus.IN_PROGRESS) {
            throw CustomException.of(DINING_REPORT_DELIVERY_CONFLICT);
        }
        // Expiry permits reassignment; the current token can still acknowledge until it is replaced.
        change.accept(request.outcome(), now());
        return result(change);
    }

    private DiningReportDeliveryResultResponse result(DiningReportChange change) {
        return new DiningReportDeliveryResultResponse(change.getDeliveryId(), change.getDeliveryState());
    }

    private void lockSequence() {
        Objects.requireNonNull(sequenceRepository.findForUpdate(), "식단 제보 변경 순번 초기값이 없습니다.");
    }

    private DiningReportResponse snapshot(String value) {
        try {
            return objectMapper.readValue(value, DiningReportResponse.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("저장된 제보 작업 내용을 읽을 수 없습니다.", exception);
        }
    }

    private String serialize(DiningReportResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("제보 작업 내용을 저장할 수 없습니다.", exception);
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock.withZone(KST)).truncatedTo(ChronoUnit.MICROS);
    }
}
