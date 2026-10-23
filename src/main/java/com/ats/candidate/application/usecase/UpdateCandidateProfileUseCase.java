package com.ats.candidate.application.usecase;

import com.ats.candidate.application.command.UpdateCandidateProfileCommand;
import com.ats.candidate.application.response.CandidateProfileResponse;
import com.ats.candidate.application.service.CandidateAccessPolicy;
import com.ats.candidate.application.service.CandidateClock;
import com.ats.candidate.application.service.CandidateLoader;
import com.ats.candidate.domain.aggregate.Candidate;
import com.ats.candidate.domain.repository.CandidateRepository;
import com.ats.job.application.contract.SkillCatalog;
import com.ats.shared.exception.ConflictException;
import com.ats.shared.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Manual completion of a profile, e.g. after a partial parse (MSG-CV-002). */
@Service
public class UpdateCandidateProfileUseCase {

    private final CandidateLoader candidateLoader;
    private final CandidateAccessPolicy accessPolicy;
    private final CandidateRepository candidateRepository;
    private final SkillCatalog skillCatalog;
    private final GetCandidateProfileUseCase getProfile;
    private final CandidateClock clock;

    public UpdateCandidateProfileUseCase(CandidateLoader candidateLoader, CandidateAccessPolicy accessPolicy,
                                         CandidateRepository candidateRepository, SkillCatalog skillCatalog,
                                         GetCandidateProfileUseCase getProfile, CandidateClock clock) {
        this.candidateLoader = candidateLoader;
        this.accessPolicy = accessPolicy;
        this.candidateRepository = candidateRepository;
        this.skillCatalog = skillCatalog;
        this.getProfile = getProfile;
        this.clock = clock;
    }

    @Transactional
    public CandidateProfileResponse execute(UpdateCandidateProfileCommand command, CurrentUser user) {
        accessPolicy.ensureCanView(command.candidateId(), user);
        Candidate candidate = candidateLoader.load(command.candidateId());

        Set<Long> skillIds = command.skillIds() == null ? Set.of() : command.skillIds();
        Set<Long> unknown = new HashSet<>(skillIds);
        unknown.removeAll(skillCatalog.findExistingIds(skillIds));
        if (!unknown.isEmpty()) {
            throw new IllegalArgumentException("Unknown skill ids: " + unknown);
        }
        if (command.email() != null && !command.email().isBlank()) {
            String email = command.email().trim().toLowerCase(Locale.ROOT);
            candidateRepository.findByEmail(email)
                    .filter(other -> !other.getId().equals(candidate.getId()))
                    .ifPresent(other -> {
                        throw new ConflictException("Another candidate already uses this email: " + email);
                    });
        }
        candidate.editProfile(command.fullName(), command.email(), command.phone(), skillIds, user.actor(),
                clock.now());
        candidateRepository.save(candidate);
        return getProfile.execute(candidate.getId(), user);
    }
}
