package com.ats.candidate.domain.valueobject;

import com.ats.candidate.domain.enums.CvFileType;
import com.ats.candidate.domain.exception.CandidateRuleViolation;

import java.util.Locale;

/**
 * An uploaded CV that passed BRULE-08: PDF or DOCX (checked on the extension and the file signature, so a
 * renamed image is rejected), not empty, at most 5 MB.
 */
public record CvFile(String fileName, CvFileType type, long sizeBytes) {

    public static final long MAX_SIZE_BYTES = 5L * 1024 * 1024;

    public static CvFile validate(String originalFileName, byte[] content) {
        if (content == null || content.length == 0) {
            throw CandidateRuleViolation.cvRequired();
        }
        if (content.length > MAX_SIZE_BYTES) {
            throw CandidateRuleViolation.cvTooLarge();
        }
        String fileName = safeFileName(originalFileName);
        String lower = fileName.toLowerCase(Locale.ROOT);
        CvFileType type;
        if (lower.endsWith(".pdf") && startsWith(content, new byte[]{'%', 'P', 'D', 'F', '-'})) {
            type = CvFileType.PDF;
        } else if (lower.endsWith(".docx") && startsWith(content, new byte[]{'P', 'K', 3, 4})) {
            type = CvFileType.DOCX;
        } else {
            throw CandidateRuleViolation.unsupportedCvFormat();
        }
        return new CvFile(fileName, type, content.length);
    }

    /** Last path segment, without control characters, at most 255 characters. */
    private static String safeFileName(String name) {
        if (name == null || name.isBlank()) {
            return "cv";
        }
        String base = name.replace('\\', '/');
        base = base.substring(base.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}\"]", "").trim();
        if (base.isEmpty()) {
            return "cv";
        }
        return base.length() > 255 ? base.substring(base.length() - 255) : base;
    }

    private static boolean startsWith(byte[] content, byte[] signature) {
        if (content.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (content[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }
}
