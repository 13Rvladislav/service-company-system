package ru.servicecompany.requestservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.servicecompany.requestservice.dto.response.analytics.*;
import ru.servicecompany.requestservice.entity.ServiceRequest;
import ru.servicecompany.requestservice.enums.RequestStatus;
import ru.servicecompany.requestservice.repository.ServiceRequestRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ServiceRequestRepository serviceRequestRepository;

    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics(
            LocalDate from,
            LocalDate to,
            UUID zoneId,
            UUID masterId
    ) {
        LocalDate actualTo = to == null ? LocalDate.now() : to;
        LocalDate actualFrom = from == null
                ? actualTo.minusDays(29)
                : from;

        if (actualFrom.isAfter(actualTo)) {
            throw new IllegalArgumentException(
                    "Дата начала периода не может быть позже даты окончания"
            );
        }

        LocalDateTime fromDateTime = actualFrom.atStartOfDay();
        LocalDateTime toDateTime = actualTo.plusDays(1).atStartOfDay();

        List<ServiceRequest> requests = getRequests(
                fromDateTime,
                toDateTime,
                zoneId,
                masterId
        );

        return new AnalyticsResponse(
                actualFrom,
                actualTo,
                buildSummary(requests),
                buildTrend(requests, actualFrom, actualTo),
                buildStatuses(requests),
                buildMasters(requests),
                buildZones(requests),
                buildRecentRequests(requests)
        );
    }

    private List<ServiceRequest> getRequests(
            LocalDateTime from,
            LocalDateTime to,
            UUID zoneId,
            UUID masterId
    ) {
        if (zoneId != null && masterId != null) {
            return serviceRequestRepository
                    .findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThanEqualAndZoneIdAndMasterId(
                            from,
                            to,
                            zoneId,
                            masterId
                    );
        }

        if (zoneId != null) {
            return serviceRequestRepository
                    .findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThanEqualAndZoneId(
                            from,
                            to,
                            zoneId
                    );
        }

        if (masterId != null) {
            return serviceRequestRepository
                    .findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThanEqualAndMasterId(
                            from,
                            to,
                            masterId
                    );
        }

        return serviceRequestRepository
                .findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThanEqual(
                        from,
                        to
                );
    }

    private AnalyticsSummaryResponse buildSummary(
            List<ServiceRequest> requests
    ) {
        long total = requests.size();

        long completed = countByStatus(
                requests,
                RequestStatus.COMPLETED
        );

        long inProgress = countByStatus(
                requests,
                RequestStatus.IN_PROGRESS
        );

        long waiting = countByStatus(
                requests,
                RequestStatus.WAITING_FOR_ASSIGNMENT
        );

        long cancelled = countByStatus(
                requests,
                RequestStatus.CANCELLED
        );

        long submitted = countByStatus(
                requests,
                RequestStatus.REPORT_SUBMITTED
        );

        long revision = countByStatus(
                requests,
                RequestStatus.REVISION_REQUIRED
        );

        double completionRate = total == 0
                ? 0
                : round((double) completed / total * 100);

        return new AnalyticsSummaryResponse(
                total,
                completed,
                inProgress,
                waiting,
                cancelled,
                submitted,
                revision,
                completionRate
        );
    }

    private List<AnalyticsTrendResponse> buildTrend(
            List<ServiceRequest> requests,
            LocalDate from,
            LocalDate to
    ) {
        Map<LocalDate, Long> incoming = requests.stream()
                .filter(request -> request.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        request -> request.getCreatedAt().toLocalDate(),
                        Collectors.counting()
                ));

        Map<LocalDate, Long> completed = requests.stream()
                .filter(request ->
                        request.getStatus() == RequestStatus.COMPLETED
                                && request.getCompletedAt() != null
                )
                .collect(Collectors.groupingBy(
                        request -> request.getCompletedAt().toLocalDate(),
                        Collectors.counting()
                ));

        List<AnalyticsTrendResponse> result = new ArrayList<>();

        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            result.add(new AnalyticsTrendResponse(
                    date,
                    incoming.getOrDefault(date, 0L),
                    completed.getOrDefault(date, 0L)
            ));
        }

        return result;
    }

    private List<AnalyticsStatusResponse> buildStatuses(
            List<ServiceRequest> requests
    ) {
        Map<RequestStatus, Long> counts = requests.stream()
                .collect(Collectors.groupingBy(
                        ServiceRequest::getStatus,
                        Collectors.counting()
                ));

        return java.util.Arrays.stream(RequestStatus.values())
                .map(status -> new AnalyticsStatusResponse(
                        status.name(),
                        counts.getOrDefault(status, 0L)
                ))
                .toList();
    }

    private List<AnalyticsMasterResponse> buildMasters(
            List<ServiceRequest> requests
    ) {
        Map<UUID, List<ServiceRequest>> grouped = requests.stream()
                .filter(request -> request.getMasterId() != null)
                .collect(Collectors.groupingBy(ServiceRequest::getMasterId));

        return grouped.entrySet()
                .stream()
                .map(entry -> {
                    List<ServiceRequest> masterRequests = entry.getValue();

                    long total = masterRequests.size();
                    long completed = countByStatus(
                            masterRequests,
                            RequestStatus.COMPLETED
                    );

                    long inProgress = countByStatus(
                            masterRequests,
                            RequestStatus.IN_PROGRESS
                    );

                    long cancelled = countByStatus(
                            masterRequests,
                            RequestStatus.CANCELLED
                    );

                    return new AnalyticsMasterResponse(
                            entry.getKey(),
                            total,
                            completed,
                            inProgress,
                            cancelled,
                            total == 0
                                    ? 0
                                    : round((double) completed / total * 100)
                    );
                })
                .sorted(Comparator.comparingLong(
                        AnalyticsMasterResponse::total
                ).reversed())
                .toList();
    }

    private List<AnalyticsZoneResponse> buildZones(
            List<ServiceRequest> requests
    ) {
        Map<UUID, List<ServiceRequest>> grouped = requests.stream()
                .filter(request -> request.getZoneId() != null)
                .collect(Collectors.groupingBy(ServiceRequest::getZoneId));

        return grouped.entrySet()
                .stream()
                .map(entry -> {
                    List<ServiceRequest> zoneRequests = entry.getValue();

                    long total = zoneRequests.size();
                    long completed = countByStatus(
                            zoneRequests,
                            RequestStatus.COMPLETED
                    );

                    return new AnalyticsZoneResponse(
                            entry.getKey(),
                            total,
                            completed,
                            countByStatus(
                                    zoneRequests,
                                    RequestStatus.IN_PROGRESS
                            ),
                            countByStatus(
                                    zoneRequests,
                                    RequestStatus.WAITING_FOR_ASSIGNMENT
                            ),
                            total == 0
                                    ? 0
                                    : round((double) completed / total * 100)
                    );
                })
                .sorted(Comparator.comparingLong(
                        AnalyticsZoneResponse::total
                ).reversed())
                .toList();
    }

    private List<AnalyticsRecentRequestResponse> buildRecentRequests(
            List<ServiceRequest> requests
    ) {
        return requests.stream()
                .sorted(Comparator.comparing(
                        ServiceRequest::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .limit(10)
                .map(request -> new AnalyticsRecentRequestResponse(
                        request.getId(),
                        request.getMasterId(),
                        request.getZoneId(),
                        request.getStatus(),
                        request.getCreatedAt(),
                        request.getCompletedAt()
                ))
                .toList();
    }

    private long countByStatus(
            List<ServiceRequest> requests,
            RequestStatus status
    ) {
        return requests.stream()
                .filter(request -> request.getStatus() == status)
                .count();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}