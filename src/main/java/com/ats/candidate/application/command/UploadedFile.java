package com.ats.candidate.application.command;

/** A file received over HTTP (name as sent by the client, raw content). */
public record UploadedFile(String fileName, byte[] content) {
}
