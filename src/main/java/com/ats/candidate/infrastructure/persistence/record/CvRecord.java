package com.ats.candidate.infrastructure.persistence.record;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Row of table {@code cvs}. {@code parsedData} is JSON. */
@Getter
@Setter
@NoArgsConstructor
public class CvRecord {

    private Long id;
    private Long candidateId;
    private String fileName;
    private String filePath;
    private String fileType;
    private long sizeBytes;
    private String parseStatus;
    private String parsedData;
    private String contentText;
    private String parseMessage;
    private Instant parsedAt;
    private Instant uploadedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
