package com.ats.candidate.application.response;

import com.ats.candidate.domain.aggregate.Cv;
import com.ats.candidate.domain.valueobject.ParsedCvData;

import java.time.Instant;

public record CvResponse(
        Long id,
        String fileName,
        String fileType,
        long sizeBytes,
        String parseStatus,
        String parseMessage,
        ParsedCvData parsedData,
        Instant uploadedAt,
        Instant parsedAt
) {

    public static CvResponse from(Cv cv) {
        return new CvResponse(cv.getId(), cv.getFileName(), cv.getFileType().name(), cv.getSizeBytes(),
                cv.getParseStatus().name(), cv.getParseMessage(), cv.getParsedData(), cv.getUploadedAt(),
                cv.getParsedAt());
    }
}
