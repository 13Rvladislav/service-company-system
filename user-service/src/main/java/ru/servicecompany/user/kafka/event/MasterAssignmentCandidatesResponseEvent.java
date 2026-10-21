package ru.servicecompany.user.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterAssignmentCandidatesResponseEvent {

    private UUID requestId;

    private List<UUID> masterIds;

    private String error;
}