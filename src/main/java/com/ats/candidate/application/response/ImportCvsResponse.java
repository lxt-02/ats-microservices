package com.ats.candidate.application.response;

import java.util.List;

public record ImportCvsResponse(int imported, int failed, List<Result> results) {

    /** Per file: the created candidate/CV (and application when a job was given), or why it was rejected. */
    public record Result(String fileName, boolean success, Long candidateId, Long cvId, Long applicationId,
                         String message) {
    }

    public static ImportCvsResponse of(List<Result> results) {
        int ok = (int) results.stream().filter(Result::success).count();
        return new ImportCvsResponse(ok, results.size() - ok, results);
    }
}
