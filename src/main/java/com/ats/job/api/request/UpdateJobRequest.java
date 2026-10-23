package com.ats.job.api.request;

import com.ats.job.application.command.UpdateJobCommand;

public final class UpdateJobRequest {

    private UpdateJobRequest() {
    }

    public static UpdateJobCommand toCommand(Long id, JobFormRequest form) {
        return new UpdateJobCommand(id, form.toCommand());
    }
}
