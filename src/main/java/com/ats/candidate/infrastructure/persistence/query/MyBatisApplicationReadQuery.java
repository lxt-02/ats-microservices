package com.ats.candidate.infrastructure.persistence.query;

import com.ats.candidate.application.port.out.ApplicationReadQuery;
import com.ats.candidate.infrastructure.persistence.mapper.ApplicationReadMapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public class MyBatisApplicationReadQuery implements ApplicationReadQuery {

    private final ApplicationReadMapper mapper;

    public MyBatisApplicationReadQuery(ApplicationReadMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<BoardCardRow> findBoardCards(Long jobId) {
        return mapper.findBoardCards(jobId).stream()
                .map(view -> new BoardCardRow(view.getApplicationId(), view.getCandidateId(), view.getCandidateName(),
                        view.getCandidateEmail(), view.getStatus(), view.getAppliedAt(), view.getStatusChangedAt(),
                        view.getCvParseStatus(), view.getNotesCount(), view.getTransferredFromId()))
                .toList();
    }

    @Override
    public List<AssignmentRow> findApprovedAssignments(Collection<Long> jobIds) {
        return mapper.findApprovedAssignments(jobIds == null || jobIds.isEmpty() ? null : jobIds).stream()
                .map(view -> new AssignmentRow(view.getApplicationId(), view.getJobId(), view.getCandidateId(),
                        view.getCandidateName(), view.getStatus(), view.getAppliedAt(),
                        view.getAssignedDepartmentId(), view.getDecidedBy(), view.getDecidedAt(),
                        view.getDecisionComment()))
                .toList();
    }
}
