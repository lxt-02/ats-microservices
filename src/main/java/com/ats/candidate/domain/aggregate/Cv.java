package com.ats.candidate.domain.aggregate;

import com.ats.candidate.domain.enums.CvFileType;
import com.ats.candidate.domain.enums.CvParseStatus;
import com.ats.candidate.domain.valueobject.CvFile;
import com.ats.candidate.domain.valueobject.ParsedCvData;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** An uploaded CV file and its parsing result. The file itself is in the CV storage under {@code storageKey}. */
public class Cv {

    private final Long id;
    private final Long candidateId;
    private final String fileName;
    private final String storageKey;
    private final CvFileType fileType;
    private final long sizeBytes;
    private CvParseStatus parseStatus;
    private ParsedCvData parsedData;
    private String contentText;
    private String parseMessage;
    private Instant parsedAt;
    private final Instant uploadedAt;
    private final String createdBy;
    private Instant updatedAt;
    private String updatedBy;

    private Cv(Long id, Long candidateId, String fileName, String storageKey, CvFileType fileType, long sizeBytes,
               CvParseStatus parseStatus, ParsedCvData parsedData, String contentText, String parseMessage,
               Instant parsedAt, Instant uploadedAt, String createdBy, Instant updatedAt, String updatedBy) {
        this.id = id;
        this.candidateId = Objects.requireNonNull(candidateId, "Candidate is required");
        this.fileName = Objects.requireNonNull(fileName, "File name is required");
        this.storageKey = Objects.requireNonNull(storageKey, "Storage key is required");
        this.fileType = Objects.requireNonNull(fileType, "File type is required");
        this.sizeBytes = sizeBytes;
        this.parseStatus = Objects.requireNonNull(parseStatus, "Parse status is required");
        this.parsedData = parsedData;
        this.contentText = contentText;
        this.parseMessage = parseMessage;
        this.parsedAt = parsedAt;
        this.uploadedAt = uploadedAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    /** A validated upload, queued for parsing. */
    public static Cv upload(Long candidateId, CvFile file, String storageKey, String actor, Instant now) {
        return new Cv(null, candidateId, file.fileName(), storageKey, file.type(), file.sizeBytes(),
                CvParseStatus.PENDING, null, null, null, null, now, actor, now, actor);
    }

    public static Cv restore(Long id, Long candidateId, String fileName, String storageKey, CvFileType fileType,
                             long sizeBytes, CvParseStatus parseStatus, ParsedCvData parsedData, String contentText,
                             String parseMessage, Instant parsedAt, Instant uploadedAt, String createdBy,
                             Instant updatedAt, String updatedBy) {
        return new Cv(id, candidateId, fileName, storageKey, fileType, sizeBytes, parseStatus, parsedData,
                contentText, parseMessage, parsedAt, uploadedAt, createdBy, updatedAt, updatedBy);
    }

    /**
     * Records the parser output: PARSED when every main field was found (MSG-CV-001), PARTIAL otherwise
     * (MSG-CV-002, needs manual review).
     */
    public void completeParsing(String text, ParsedCvData data, List<String> missingFields, Instant now) {
        contentText = text;
        parsedData = data;
        parsedAt = now;
        if (missingFields.isEmpty()) {
            parseStatus = CvParseStatus.PARSED;
            parseMessage = "CV data has been parsed successfully. " + extractedFieldCount(data) + " fields extracted.";
        } else {
            parseStatus = CvParseStatus.PARTIAL;
            parseMessage = "CV was partially parsed. The following fields could not be extracted: "
                    + String.join(", ", missingFields) + ". Please review and complete manually.";
        }
        touch("cv-parser", now);
    }

    /** MSG-CV-003. */
    public void failParsing(Instant now) {
        parseStatus = CvParseStatus.FAILED;
        parseMessage = "Unable to parse the uploaded CV. Please ensure the file is not corrupted and try again.";
        parsedAt = now;
        touch("cv-parser", now);
    }

    /** Queues the CV for another parsing attempt. */
    public void requestReparse(String actor, Instant now) {
        parseStatus = CvParseStatus.PENDING;
        parseMessage = null;
        touch(actor, now);
    }

    public boolean isPendingParse() {
        return parseStatus == CvParseStatus.PENDING;
    }

    private void touch(String actor, Instant now) {
        updatedAt = now;
        updatedBy = actor;
    }

    private static int extractedFieldCount(ParsedCvData data) {
        int count = 0;
        count += data.fullName() == null ? 0 : 1;
        count += data.email() == null ? 0 : 1;
        count += data.phone() == null ? 0 : 1;
        count += data.skills().isEmpty() ? 0 : 1;
        count += data.education().isEmpty() ? 0 : 1;
        count += data.experience().isEmpty() ? 0 : 1;
        return count;
    }

    public Long getId() {
        return id;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public CvFileType getFileType() {
        return fileType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public CvParseStatus getParseStatus() {
        return parseStatus;
    }

    public ParsedCvData getParsedData() {
        return parsedData;
    }

    public String getContentText() {
        return contentText;
    }

    public String getParseMessage() {
        return parseMessage;
    }

    public Instant getParsedAt() {
        return parsedAt;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }
}
