package com.ats.candidate.api.request;

import com.ats.candidate.application.command.ImportCvsCommand;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public final class ImportCvsRequest {

    private ImportCvsRequest() {
    }

    public static ImportCvsCommand toCommand(List<MultipartFile> files, Long jobId) {
        return new ImportCvsCommand(files == null ? List.of() : files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .map(SubmitApplicationRequest::toUploadedFile)
                .toList(), jobId);
    }
}
