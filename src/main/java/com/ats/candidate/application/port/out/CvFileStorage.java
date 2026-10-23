package com.ats.candidate.application.port.out;

/** Where CV files are kept (local disk today, object storage later). */
public interface CvFileStorage {

    /** Stores the file and returns its key. */
    String store(byte[] content, String fileName);

    byte[] load(String key);

    void delete(String key);
}
