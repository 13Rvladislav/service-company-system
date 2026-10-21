package ru.servicecompany.user.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterAssignmentCandidatesRequestEvent {

    private UUID requestId;

    private UUID zoneId;

    private UUID equipmentTypeId;
}