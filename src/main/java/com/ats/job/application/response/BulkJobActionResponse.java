package com.ats.job.application.response;

import java.util.List;

public record BulkJobActionResponse(int succeeded, int failed, List<Result> results) {

    public record Result(Long jobId, boolean success, String message) {
    }

    public static BulkJobActionResponse of(List<Result> results) {
        int ok = (int) results.stream().filter(Result::success).count();
        return new BulkJobActionResponse(ok, results.size() - ok, results);
    }
}
