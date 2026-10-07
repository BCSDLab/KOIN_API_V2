package in.koreatech.koin.domain.dining.service;

import static in.koreatech.koin.domain.dining.model.DiningReportDeliveryFailureReason.RATE_LIMITED;
import static in.koreatech.koin.domain.dining.model.DiningReportDeliveryMode.SEND;
import static in.koreatech.koin.domain.dining.model.DiningReportDeliveryMode.VERIFY;
import static in.koreatech.koin.domain.dining.model.DiningReportDeliveryOutcome.NOT_APPLIED;
import static in.koreatech.koin.domain.dining.model.DiningReportDeliveryOutcome.SUCCEEDED;
import static in.koreatech.koin.domain.dining.model.DiningReportDeliveryOutcome.UNCERTAIN;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REQUEST_BODY;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_DINING_REPORT_DELIVERY;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import in.koreatech.koin.domain.dining.config.DiningReportDeliveryProperties;
import in.koreatech.koin.domain.dining.dto.DiningReportDeliveryResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportDeliveryResultRequest;
import in.koreatech.koin.domain.dining.dto.DiningReportDeliveryResultResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportResponse;
import in.koreatech.koin.domain.dining.model.DiningReport;
import in.koreatech.koin.domain.dining.model.DiningReportChange;
import in.koreatech.koin.domain.dining.model.DiningReportDelivery;
import in.koreatech.koin.domain.dining.model.DiningReportDeliveryAttempt;
import in.koreatech.koin.domain.dining.model.DiningReportDeliveryMode;
import in.koreatech.koin.domain.dining.model.DiningReportDeliveryOperation;
import in.koreatech.koin.domain.dining.model.DiningReportDeliveryStatus;
import in.koreatech.koin.domain.dining.model.DiningReportDeliveryTarget;
import in.koreatech.koin.domain.dining.model.DiningReportSequence;
import in.koreatech.koin.domain.dining.repository.DiningReportDeliveryAttemptRepository;
import in.koreatech.koin.domain.dining.repository.DiningReportDeliveryRepository;
import in.koreatech.koin.domain.dining.repository.DiningReportDeliveryTargetRepository;
import in.koreatech.koin.domain.dining.repository.DiningReportSequenceRepository;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiningReportDeliveryService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int CANDIDATE_BATCH_SIZE = 100;
    private static final long VERIFY_RETRY_SECONDS = 300;
    private static final LocalDateTime LAST_DATABASE_TIME = LocalDateTime.of(9999, 12, 31, 23, 59, 59, 999_999_000);

    private final DiningReportSequenceRepository sequenceRepository;
    private final DiningReportDeliveryTargetRepository targetRepository;
    private final DiningReportDeliveryRepository deliveryRepository;
    private final DiningReportDeliveryAttemptRepository attemptRepository;
    private final DiningReportDeliveryProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public record Result(DiningReportDeliveryResultResponse response, boolean conflict) { }

    @Transactional(propagation = Propagation.MANDATORY)
    public void recordChange(DiningReport report, long sequence, DiningReportChange.EventType eventType,
        LocalDateTime now) {
        // The caller already holds the sequence lock as its final business lock.
        String snapshot = serialize(DiningReportResponse.from(report));
        DiningReportDeliveryTarget target = targetRepository.findById(report.getId()).orElse(null);
        if (target == null) {
            target = targetRepository.save(DiningReportDeliveryTarget.create(report.getId(), sequence, snapshot,
                eventType != DiningReportChange.EventType.CREATED, now));
        } else {
            target.recordDesired(sequence, snapshot, now);
        }
        captureRouting(target, now);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Optional<DiningReportDeliveryResponse> claim() {
        DiningReportSequence gate = lockSequence();
        LocalDateTime now = now();
        int afterReportId = 0;
        while (true) {
            List<DiningReportDeliveryTarget> candidates = targetRepository.findCandidates(afterReportId,
                PageRequest.of(0, CANDIDATE_BATCH_SIZE));
            if (candidates.isEmpty()) {
                return Optional.empty();
            }
            for (DiningReportDeliveryTarget target : candidates) {
                afterReportId = target.getReportId();
                captureRouting(target, now);
                if (!target.hasRouting()) {
                    continue;
                }
                DiningReportDelivery delivery = activeDelivery(target);
                if (delivery != null) {
                    expireActiveAttempt(target, delivery, now);
                    if (delivery.getActiveAttemptToken() != null || delivery.getNextAttemptAt() == null
                        || now.isBefore(delivery.getNextAttemptAt())) {
                        continue;
                    }
                }
                DiningReportDeliveryMode mode = delivery == null
                    || delivery.getStatus() == DiningReportDeliveryStatus.QUEUED ? SEND : VERIFY;
                // The integration-wide write cooldown never prevents read-only verification.
                if (mode == SEND && gate.isDeliveryCoolingDown(now)) {
                    continue;
                }
                if (delivery == null) {
                    delivery = deliveryRepository.save(DiningReportDelivery.create(target, now));
                    target.activate(delivery.getId(), now);
                }
                DiningReportDeliveryAttempt attempt = attemptRepository.save(
                    DiningReportDeliveryAttempt.issue(delivery, mode, now));
                delivery.issue(attempt, now);
                return Optional.of(response(delivery, attempt));
            }
        }
    }

    // The HTTP lane throws 409 only after this independent transaction has committed the hold and evidence.
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public Result recordResult(UUID deliveryId, DiningReportDeliveryResultRequest request) {
        DiningReportSequence gate = lockSequence();
        DiningReportDelivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> CustomException.of(NOT_FOUND_DINING_REPORT_DELIVERY));
        DiningReportDeliveryAttempt attempt = attemptRepository.findById(request.attemptToken())
            .filter(found -> deliveryId.equals(found.getDeliveryId()))
            .orElseThrow(() -> CustomException.of(NOT_FOUND_DINING_REPORT_DELIVERY));
        if (attempt.getMode() == VERIFY && request.outcome() == NOT_APPLIED) {
            throw CustomException.of(INVALID_REQUEST_BODY);
        }
        DiningReportDeliveryTarget target = targetRepository.findById(delivery.getReportId()).orElseThrow();
        LocalDateTime now = now();
        expireActiveAttempt(target, delivery, now);
        String result = serialize(request);
        recordEvidence(attempt, result, now);

        // Replayed uncertainty and stale VERIFY failures cannot undo later definite evidence.
        if (attempt.hasDefiniteOutcome()) {
            if (request.outcome() == UNCERTAIN || sameResult(attempt.getAcceptedResult(), result)) {
                return result(target, delivery, false);
            }
            return conflict(target, delivery, now);
        }
        if (request.outcome() == UNCERTAIN) {
            if (attempt.getAcceptedOutcome() != UNCERTAIN) {
                attempt.accept(UNCERTAIN, result, now);
                if (isCurrentAttempt(target, delivery, attempt)) {
                    if (attempt.getMode() == SEND) {
                        delivery.awaitVerification(now);
                    } else {
                        delivery.needsAttention(now.plusSeconds(VERIFY_RETRY_SECONDS), now);
                    }
                }
            }
            return result(target, delivery, false);
        }
        if (request.outcome() == SUCCEEDED) {
            return recordSuccess(target, delivery, attempt, request, result, now);
        }
        // NOT_APPLIED is definitive only for this SEND, never evidence obtained by searching Slack.
        if (delivery.getConfirmedMessageTs() != null) {
            return conflict(target, delivery, now);
        }
        attempt.accept(NOT_APPLIED, result, now);
        invalidateVerifications(delivery, attempt.getToken(), now);
        LocalDateTime nextAttemptAt = now;
        if (request.reason() == RATE_LIMITED) {
            nextAttemptAt = retryAt(now, request.retryAfterSeconds());
            // Accepted rate-limit evidence applies to the integration even when this report remains held.
            gate.extendDeliveryCooldown(nextAttemptAt);
        }
        if (!attempt.getToken().equals(delivery.getSendAttemptToken()) || target.getHoldReason() != null) {
            return result(target, delivery, false);
        }
        switch (request.reason()) {
            case NOT_SENT, RATE_LIMITED -> delivery.queue(nextAttemptAt, now);
            case REJECTED -> {
                target.hold(DiningReportDeliveryTarget.HoldReason.REJECTED, now);
                delivery.needsAttention(null, now);
            }
        }
        return result(target, delivery, false);
    }

    private Result recordSuccess(DiningReportDeliveryTarget target, DiningReportDelivery delivery,
        DiningReportDeliveryAttempt attempt, DiningReportDeliveryResultRequest request, String result,
        LocalDateTime now) {
        if (!matchesTarget(target, delivery, request.messageRef())) {
            return conflict(target, delivery, now);
        }
        if (attempt.getMode() == VERIFY) {
            DiningReportDeliveryAttempt send = attemptRepository.findById(attempt.getSendAttemptToken())
                .orElseThrow();
            if (send.getAcceptedOutcome() == NOT_APPLIED) {
                return conflict(target, delivery, now);
            }
        }
        attempt.accept(SUCCEEDED, result, now);
        invalidateVerifications(delivery, attempt.getSendAttemptToken(), now);
        if (target.getHoldReason() == null) {
            delivery.succeed(request.messageRef().messageTs(), now);
            target.confirm(delivery, request.messageRef().messageTs(), now);
        }
        return result(target, delivery, false);
    }

    private boolean matchesTarget(DiningReportDeliveryTarget target, DiningReportDelivery delivery,
        DiningReportDeliveryResultRequest.MessageReference reference) {
        if (!Objects.equals(delivery.getWorkspaceId(), target.getWorkspaceId())
            || !Objects.equals(delivery.getChannelId(), target.getChannelId())
            || !Objects.equals(reference.channelId(), delivery.getChannelId())) {
            return false;
        }
        if (delivery.getOperation() == DiningReportDeliveryOperation.UPDATE
            && (!Objects.equals(reference.messageTs(), delivery.getTargetMessageTs())
                || !Objects.equals(reference.messageTs(), target.getMessageTs()))) {
            return false;
        }
        return (target.getMessageTs() == null || Objects.equals(reference.messageTs(), target.getMessageTs()))
            && (delivery.getConfirmedMessageTs() == null
                || Objects.equals(reference.messageTs(), delivery.getConfirmedMessageTs()));
    }

    private void expireActiveAttempt(DiningReportDeliveryTarget target, DiningReportDelivery delivery,
        LocalDateTime now) {
        if (target.getHoldReason() != null || delivery.getActiveAttemptToken() == null) {
            return;
        }
        DiningReportDeliveryAttempt attempt = attemptRepository.findById(delivery.getActiveAttemptToken())
            .orElseThrow();
        if (now.isBefore(attempt.getExpiresAt())) {
            return;
        }
        attempt.expire(now);
        if (attempt.getMode() == SEND) {
            delivery.awaitVerification(now);
        } else {
            delivery.needsAttention(attempt.getExpiresAt().plusSeconds(VERIFY_RETRY_SECONDS), now);
        }
    }

    private boolean isCurrentAttempt(DiningReportDeliveryTarget target, DiningReportDelivery delivery,
        DiningReportDeliveryAttempt attempt) {
        return target.getHoldReason() == null && attempt.getInvalidatedAt() == null
            && attempt.getToken().equals(delivery.getActiveAttemptToken());
    }

    private Result conflict(DiningReportDeliveryTarget target, DiningReportDelivery delivery, LocalDateTime now) {
        target.hold(DiningReportDeliveryTarget.HoldReason.CONFLICT, now);
        invalidateVerifications(delivery, delivery.getSendAttemptToken(), now);
        // A late contradiction can arrive while a newer immutable delivery is active for this report.
        DiningReportDelivery active = activeDelivery(target);
        if (active != null && !active.getId().equals(delivery.getId())) {
            invalidateVerifications(active, active.getSendAttemptToken(), now);
            if (active.getActiveAttemptToken() != null) {
                attemptRepository.findById(active.getActiveAttemptToken()).orElseThrow().invalidate(now);
            }
            active.needsAttention(null, now);
        }
        if (delivery.getActiveAttemptToken() != null) {
            attemptRepository.findById(delivery.getActiveAttemptToken()).orElseThrow().invalidate(now);
        }
        delivery.needsAttention(null, now);
        return result(target, delivery, true);
    }

    private void invalidateVerifications(DiningReportDelivery delivery, UUID sendToken, LocalDateTime now) {
        if (sendToken != null) {
            attemptRepository.findByDeliveryIdAndSendAttemptTokenAndMode(delivery.getId(), sendToken, VERIFY)
                .forEach(attempt -> attempt.invalidate(now));
        }
    }

    private Result result(DiningReportDeliveryTarget target, DiningReportDelivery delivery, boolean conflict) {
        DiningReportDeliveryStatus state = target.getHoldReason() == null
            ? delivery.getStatus() : DiningReportDeliveryStatus.NEEDS_ATTENTION;
        return new Result(new DiningReportDeliveryResultResponse(delivery.getId(), state), conflict);
    }

    private DiningReportDelivery activeDelivery(DiningReportDeliveryTarget target) {
        return target.getActiveDeliveryId() == null ? null
            : deliveryRepository.findById(target.getActiveDeliveryId()).orElseThrow();
    }

    private void captureRouting(DiningReportDeliveryTarget target, LocalDateTime now) {
        if (properties.isConfigured()) {
            target.captureRouting(properties.getWorkspaceId(), properties.getChannelId(), now);
        }
    }

    private DiningReportSequence lockSequence() {
        return Objects.requireNonNull(sequenceRepository.findForUpdate(), "식단 제보 변경 순번 초기값이 없습니다.");
    }

    private DiningReportDeliveryResponse response(DiningReportDelivery delivery, DiningReportDeliveryAttempt attempt) {
        return new DiningReportDeliveryResponse(delivery.getId(), attempt.getToken(), attempt.getMode(),
            delivery.getOperation(), attempt.getExpiresAt().atZone(KST).toOffsetDateTime(),
            new DiningReportDeliveryResponse.Target(delivery.getWorkspaceId(), delivery.getChannelId(),
                delivery.getTargetMessageTs()), snapshot(delivery.getReportSnapshot()));
    }

    private DiningReportResponse snapshot(String value) {
        try {
            return objectMapper.readValue(value, DiningReportResponse.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("저장된 제보 전송 내용을 읽을 수 없습니다.", exception);
        }
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("제보 전송 내용을 저장할 수 없습니다.", exception);
        }
    }

    private boolean sameResult(String previous, String result) {
        return readTree(previous).equals(readTree(result));
    }

    private void recordEvidence(DiningReportDeliveryAttempt attempt, String result, LocalDateTime now) {
        ArrayNode history = (ArrayNode)readTree(attempt.getEvidenceHistory());
        JsonNode evidence = readTree(result);
        for (JsonNode receipt : history) {
            if (receipt.get("result").equals(evidence)) {
                return;
            }
        }
        ObjectNode receipt = objectMapper.createObjectNode();
        receipt.put("received_at", now.toString());
        receipt.set("result", evidence);
        history.add(receipt);
        attempt.recordEvidence(result, serialize(history));
    }

    private JsonNode readTree(String value) {
        try {
            return objectMapper.readTree(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("저장된 제보 전송 결과를 읽을 수 없습니다.", exception);
        }
    }

    private LocalDateTime retryAt(LocalDateTime now, long seconds) {
        // The contract sets no upper bound on Retry-After; very large delays stay held at MySQL's last date.
        try {
            LocalDateTime until = now.plusSeconds(seconds);
            return until.isAfter(LAST_DATABASE_TIME) ? LAST_DATABASE_TIME : until;
        } catch (DateTimeException | ArithmeticException exception) {
            return LAST_DATABASE_TIME;
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock.withZone(KST)).truncatedTo(ChronoUnit.MICROS);
    }
}
