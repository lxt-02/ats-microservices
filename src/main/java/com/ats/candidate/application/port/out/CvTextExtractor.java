package com.ats.candidate.application.port.out;

import com.ats.candidate.domain.enums.CvFileType;

/** Plain text of a CV file. */
public interface CvTextExtractor {

    /** @throws UnreadableCvException when the file is corrupted, encrypted or not what its type says */
    String extractText(CvFileType type, byte[] content);

    class UnreadableCvException extends RuntimeException {

        public UnreadableCvException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
