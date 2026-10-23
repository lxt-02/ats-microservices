package com.ats.candidate.infrastructure.storage;

import com.ats.candidate.application.port.out.CvFileStorage;
import com.ats.candidate.infrastructure.configuration.CvStorageProperties;
import com.ats.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

/**
 * Keeps CV files on the local disk under {@code ats.storage.cv-directory}. Keys are generated
 * ({@code yyyy/MM/<uuid>.<ext>}), never derived from the uploaded file name.
 */
@Component
public class LocalCvFileStorage implements CvFileStorage {

    private static final Logger log = LoggerFactory.getLogger(LocalCvFileStorage.class);

    private final Path root;

    public LocalCvFileStorage(CvStorageProperties properties) {
        this.root = Path.of(properties.getCvDirectory()).toAbsolutePath().normalize();
    }

    @Override
    public String store(byte[] content, String fileName) {
        LocalDate today = LocalDate.now();
        String key = "%d/%02d/%s.%s".formatted(today.getYear(), today.getMonthValue(), UUID.randomUUID(),
                extension(fileName));
        Path target = resolve(key);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            return key;
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not store CV file", ex);
        }
    }

    @Override
    public byte[] load(String key) {
        try {
            return Files.readAllBytes(resolve(key));
        } catch (NoSuchFileException ex) {
            throw new ResourceNotFoundException("CV file is missing from storage");
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not read CV file", ex);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException ex) {
            log.warn("Could not delete CV file {}", key, ex);
        }
    }

    private Path resolve(String key) {
        Path path = root.resolve(key).normalize();
        if (!path.startsWith(root)) {
            throw new IllegalArgumentException("Invalid storage key");
        }
        return path;
    }

    private static String extension(String fileName) {
        String lower = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        return lower.endsWith(".docx") ? "docx" : "pdf";
    }
}
