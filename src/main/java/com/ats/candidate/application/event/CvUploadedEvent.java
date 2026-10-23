package com.ats.candidate.application.event;

/** A CV was stored and waits for parsing (published inside the upload transaction, handled after commit). */
public record CvUploadedEvent(Long cvId) {
}
