package ru.servicecompany.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.servicecompany.user.dto.response.MasterListItemResponse;
import ru.servicecompany.user.entity.MasterProfile;
import ru.servicecompany.user.repository.MasterProfileRepository;

import java.util.List;

@RestController
@RequestMapping("/api/masters")
@RequiredArgsConstructor
public class MasterController {

    private final MasterProfileRepository masterProfileRepository;

    /**
     * Получить список профилей мастеров.
     *
     * Используется диспетчером и администратором
     * при назначении специализаций.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public List<MasterListItemResponse> getAllMasters() {
        return masterProfileRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private MasterListItemResponse toResponse(MasterProfile master) {
        return new MasterListItemResponse(
                master.getId(),
                master.getFirstName(),
                master.getLastName(),
                master.getMiddleName(),
                master.getEmployeeNumber(),
                master.getPhone(),
                master.getStatus() != null
                        ? master.getStatus().name()
                        : null
        );
    }
}