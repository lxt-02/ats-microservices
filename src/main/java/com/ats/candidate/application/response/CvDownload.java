package com.ats.candidate.application.response;

/** Original CV file. */
public record CvDownload(String fileName, String fileType, byte[] content) {
}
