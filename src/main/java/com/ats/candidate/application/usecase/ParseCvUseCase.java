package com.ats.candidate.application.usecase;

import com.ats.candidate.application.port.out.CvFileStorage;
import com.ats.candidate.application.port.out.CvTextExtractor;
import com.ats.candidate.application.service.CandidateClock;
import com.ats.candidate.domain.aggregate.Candidate;
import com.ats.candidate.domain.aggregate.Cv;
import com.ats.candidate.domain.repository.CandidateRepository;
import com.ats.candidate.domain.repository.CvRepository;
import com.ats.candidate.domain.service.CvDataExtractor;
import com.ats.job.application.contract.SkillCatalog;
import com.ats.job.application.contract.SkillInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * UC-02.4 Auto-Parse CV Data: extracts the text, recognises name, email, phone, skills, education and
 * experience, then completes the candidate profile (empty fields only) and flags probable duplicates.
 */
@Service
public class ParseCvUseCase {

    private static final Logger log = LoggerFactory.getLogger(ParseCvUseCase.class);
    private static final String ACTOR = "cv-parser";
    private static final int MAX_TEXT_LENGTH = 100_000;

    private final CvRepository cvRepository;
    private final CandidateRepository candidateRepository;
    private final CvFileStorage storage;
    private final CvTextExtractor textExtractor;
    private final SkillCatalog skillCatalog;
    private final CandidateClock clock;
    private final CvDataExtractor dataExtractor = new CvDataExtractor();

    public ParseCvUseCase(CvRepository cvRepository, CandidateRepository candidateRepository,
                          CvFileStorage storage, CvTextExtractor textExtractor, SkillCatalog skillCatalog,
                          CandidateClock clock) {
        this.cvRepository = cvRepository;
        this.candidateRepository = candidateRepository;
        this.storage = storage;
        this.textExtractor = textExtractor;
        this.skillCatalog = skillCatalog;
        this.clock = clock;
    }

    @Transactional
    public void execute(Long cvId) {
        Cv cv = cvRepository.findById(cvId).orElse(null);
        if (cv == null || !cv.isPendingParse()) {
            return;
        }
        Instant now = clock.now();
        String text;
        try {
            text = textExtractor.extractText(cv.getFileType(), storage.load(cv.getStorageKey()));
        } catch (CvTextExtractor.UnreadableCvException ex) {
            log.info("CV {} could not be read: {}", cvId, ex.getMessage());
            text = null;
        }
        if (text == null || text.isBlank()) {
            cv.failParsing(now);
            cvRepository.save(cv);
            return;
        }
        if (text.length() > MAX_TEXT_LENGTH) {
            text = text.substring(0, MAX_TEXT_LENGTH);
        }

        Map<String, Long> skillIdsByName = skillCatalog.findAll().stream()
                .collect(Collectors.toMap(SkillInfo::name, SkillInfo::id, (first, second) -> first));
        CvDataExtractor.Result result = dataExtractor.extract(text, skillIdsByName.keySet());
        cv.completeParsing(text, result.data(), result.missingFields(), now);
        cvRepository.save(cv);

        Candidate candidate = candidateRepository.findById(cv.getCandidateId()).orElse(null);
        if (candidate == null) {
            return;
        }
        Set<Long> recognised = result.data().skills().stream()
                .map(skillIdsByName::get)
                .collect(Collectors.toSet());
        candidate.applyParsedCv(result.data(), recognised, ACTOR, now);
        if (!candidate.isDuplicate()) {
            candidateRepository.findOtherIdByEmailOrPhone(candidate.getId(),
                            emailOrNull(result.data().email()), result.data().phone())
                    .ifPresent(otherId -> candidate.markDuplicateOf(otherId, ACTOR, now));
        }
        candidateRepository.save(candidate);
    }

    private static String emailOrNull(String email) {
        return email == null ? null : email.toLowerCase(Locale.ROOT);
    }
}
