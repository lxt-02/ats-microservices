package com.ats.candidate.infrastructure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** {@code ats.storage.cv-directory}: folder where uploaded CV files are kept. */
@Component
@ConfigurationProperties(prefix = "ats.storage")
public class CvStorageProperties {

    private String cvDirectory = "data/cvs";

    public String getCvDirectory() {
        return cvDirectory;
    }

    public void setCvDirectory(String cvDirectory) {
        if (cvDirectory != null && !cvDirectory.isBlank()) {
            this.cvDirectory = cvDirectory.trim();
        }
    }
}
