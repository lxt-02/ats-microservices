package com.ats.candidate.infrastructure.persistence.converter;

import com.ats.candidate.domain.aggregate.Cv;
import com.ats.candidate.domain.enums.CvFileType;
import com.ats.candidate.domain.enums.CvParseStatus;
import com.ats.candidate.domain.valueobject.ParsedCvData;
import com.ats.candidate.infrastructure.persistence.record.CvRecord;
import tools.jackson.databind.json.JsonMapper;

/** Record &lt;-&gt; aggregate; parsed data is stored as JSON. */
public final class CvPersistenceConverter {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private CvPersistenceConverter() {
    }

    public static CvRecord toRecord(Cv cv) {
        CvRecord record = new CvRecord();
        record.setId(cv.getId());
        record.setCandidateId(cv.getCandidateId());
        record.setFileName(cv.getFileName());
        record.setFilePath(cv.getStorageKey());
        record.setFileType(cv.getFileType().name());
        record.setSizeBytes(cv.getSizeBytes());
        record.setParseStatus(cv.getParseStatus().name());
        record.setParsedData(cv.getParsedData() == null ? null : JSON.writeValueAsString(cv.getParsedData()));
        record.setContentText(cv.getContentText());
        record.setParseMessage(cv.getParseMessage());
        record.setParsedAt(cv.getParsedAt());
        record.setUploadedAt(cv.getUploadedAt());
        record.setCreatedAt(cv.getUploadedAt());
        record.setUpdatedAt(cv.getUpdatedAt());
        record.setCreatedBy(cv.getCreatedBy());
        record.setUpdatedBy(cv.getUpdatedBy());
        return record;
    }

    public static Cv toAggregate(CvRecord record) {
        ParsedCvData data = record.getParsedData() == null ? null
                : JSON.readValue(record.getParsedData(), ParsedCvData.class);
        return Cv.restore(record.getId(), record.getCandidateId(), record.getFileName(), record.getFilePath(),
                CvFileType.valueOf(record.getFileType()), record.getSizeBytes(),
                CvParseStatus.valueOf(record.getParseStatus()), data, record.getContentText(),
                record.getParseMessage(), record.getParsedAt(), record.getUploadedAt(), record.getCreatedBy(),
                record.getUpdatedAt(), record.getUpdatedBy());
    }
}
