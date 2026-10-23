package com.ats.candidate.application.usecase;

import com.ats.candidate.application.port.out.ApplicationReadQuery;
import com.ats.candidate.application.port.out.ApplicationReadQuery.BoardCardRow;
import com.ats.candidate.application.response.PipelineBoardResponse;
import com.ats.candidate.application.response.PipelineBoardResponse.Card;
import com.ats.candidate.application.service.CandidateAccessPolicy;
import com.ats.candidate.application.service.CandidateClock;
import com.ats.candidate.domain.aggregate.JobApplication;
import com.ats.candidate.domain.aggregate.PipelineStage;
import com.ats.candidate.domain.repository.PipelineStageRepository;
import com.ats.job.application.contract.JobInfo;
import com.ats.shared.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Candidate Pipeline (Kanban Board) of one job, for its recruiter, its hiring manager or an admin. */
@Service
public class GetPipelineBoardUseCase {

    private final ApplicationReadQuery readQuery;
    private final PipelineStageRepository stageRepository;
    private final CandidateAccessPolicy accessPolicy;
    private final CandidateClock clock;

    public GetPipelineBoardUseCase(ApplicationReadQuery readQuery, PipelineStageRepository stageRepository,
                                   CandidateAccessPolicy accessPolicy, CandidateClock clock) {
        this.readQuery = readQuery;
        this.stageRepository = stageRepository;
        this.accessPolicy = accessPolicy;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PipelineBoardResponse execute(Long jobId, CurrentUser user) {
        JobInfo job = accessPolicy.ensureCanAccessJob(jobId, user);
        List<BoardCardRow> rows = readQuery.findBoardCards(jobId);
        Map<String, List<Card>> cardsByStatus = rows.stream().collect(Collectors.groupingBy(BoardCardRow::status,
                Collectors.mapping(this::toCard, Collectors.toList())));

        List<PipelineBoardResponse.Column> columns = stageRepository.findAll().stream()
                .map((PipelineStage stage) -> {
                    List<Card> cards = cardsByStatus.getOrDefault(stage.getStatus().name(), List.of());
                    return new PipelineBoardResponse.Column(stage.getId(), stage.getStatus().name(), stage.getName(),
                            stage.getColor(), cards.size(), cards);
                })
                .toList();
        return new PipelineBoardResponse(job.id(), job.title(), rows.size(), columns);
    }

    private Card toCard(BoardCardRow row) {
        return new Card(row.applicationId(), JobApplication.code(row.applicationId(), row.appliedAt(), clock.zone()),
                row.candidateId(), row.candidateName(), row.candidateEmail(), row.appliedAt(),
                row.statusChangedAt(), row.cvParseStatus(), row.notesCount(), row.transferredFromId() != null);
    }
}
