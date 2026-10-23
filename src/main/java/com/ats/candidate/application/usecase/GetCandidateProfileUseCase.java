package com.ats.candidate.application.usecase;

import com.ats.candidate.application.port.out.CandidateReadQuery;
import com.ats.candidate.application.port.out.CandidateReadQuery.ApplicationRow;
import com.ats.candidate.application.response.CandidateApplicationResponse;
import com.ats.candidate.application.response.CandidateProfileResponse;
import com.ats.candidate.application.response.CvResponse;
import com.ats.candidate.application.response.SkillRef;
import com.ats.candidate.application.service.CandidateAccessPolicy;
import com.ats.candidate.application.service.CandidateClock;
import com.ats.candidate.application.service.CandidateLoader;
import com.ats.candidate.domain.aggregate.Candidate;
import com.ats.candidate.domain.aggregate.JobApplication;
import com.ats.candidate.domain.repository.CvRepository;
import com.ats.job.application.contract.JobDirectory;
import com.ats.job.application.contract.JobInfo;
import com.ats.job.application.contract.SkillCatalog;
import com.ats.shared.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** UC-02.6 / UC-03.2 View Candidate Profile. */
@Service
public class GetCandidateProfileUseCase {

    private final CandidateLoader candidateLoader;
    private final CandidateAccessPolicy accessPolicy;
    private final CandidateReadQuery candidateReadQuery;
    private final CvRepository cvRepository;
    private final SkillCatalog skillCatalog;
    private final JobDirectory jobDirectory;
    private final CandidateClock clock;

    public GetCandidateProfileUseCase(CandidateLoader candidateLoader, CandidateAccessPolicy accessPolicy,
                                      CandidateReadQuery candidateReadQuery, CvRepository cvRepository,
                                      SkillCatalog skillCatalog, JobDirectory jobDirectory, CandidateClock clock) {
        this.candidateLoader = candidateLoader;
        this.accessPolicy = accessPolicy;
        this.candidateReadQuery = candidateReadQuery;
        this.cvRepository = cvRepository;
        this.skillCatalog = skillCatalog;
        this.jobDirectory = jobDirectory;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public CandidateProfileResponse execute(Long candidateId, CurrentUser user) {
        accessPolicy.ensureCanView(candidateId, user);
        Candidate candidate = candidateLoader.load(candidateId);

        List<SkillRef> skills = skillCatalog.findByIds(candidate.getSkillIds()).values().stream()
                .map(skill -> new SkillRef(skill.id(), skill.name()))
                .sorted(Comparator.comparing(SkillRef::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
        List<CvResponse> cvs = cvRepository.findByCandidateId(candidateId).stream().map(CvResponse::from).toList();
        List<ApplicationRow> rows = candidateReadQuery.findApplicationsByCandidateId(candidateId);
        Map<Long, JobInfo> jobs = jobDirectory.findByIds(rows.stream().map(ApplicationRow::jobId).toList());
        List<CandidateApplicationResponse> applications = rows.stream()
                .map(row -> new CandidateApplicationResponse(row.id(),
                        JobApplication.code(row.id(), row.appliedAt(), clock.zone()), row.jobId(),
                        jobs.containsKey(row.jobId()) ? jobs.get(row.jobId()).title() : null,
                        row.status(), row.source(), row.appliedAt()))
                .toList();

        return new CandidateProfileResponse(candidate.getId(), candidate.getFullName(), candidate.getEmail(),
                candidate.getPhone(), candidate.getStatus().name(), candidate.getSource(),
                candidate.getUtm().source(), candidate.getUtm().medium(), candidate.getUtm().campaign(),
                candidate.isDuplicate(), candidate.getDuplicateOfId(), skills, cvs, applications,
                candidate.getCreatedAt(), candidate.getUpdatedAt(), candidate.getCreatedBy());
    }
}
