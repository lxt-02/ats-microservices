package com.ats.candidate.application.command;

import java.util.List;

/** UC-02.3: one or more CV files, optionally attached to a job as new applications. */
public record ImportCvsCommand(List<UploadedFile> files, Long jobId) {
}
