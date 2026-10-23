package com.ats.candidate.domain;

import com.ats.candidate.domain.enums.CvFileType;
import com.ats.candidate.domain.valueobject.CvFile;
import com.ats.shared.exception.RuleViolationException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CvFileTest {

    private static final byte[] PDF = "%PDF-1.4 rest".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] DOCX = {'P', 'K', 3, 4, 0, 0};

    private static String rule(String name, byte[] content) {
        return assertThrows(RuleViolationException.class, () -> CvFile.validate(name, content)).getRule();
    }

    @Test
    void acceptsPdfAndDocxCheckedOnTheirSignature() {
        assertEquals(CvFileType.PDF, CvFile.validate("cv.PDF", PDF).type());
        assertEquals(CvFileType.DOCX, CvFile.validate("C:\\Users\\me\\cv.docx", DOCX).type());
        assertEquals("cv.docx", CvFile.validate("C:\\Users\\me\\cv.docx", DOCX).fileName());
    }

    @Test
    void rejectsOtherFormatsRenamedFilesEmptyAndLargeFiles() {
        assertEquals("CV_R1", rule("photo.jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, 1}));
        assertEquals("CV_R1", rule("photo.pdf", new byte[]{(byte) 0xFF, (byte) 0xD8, 1}));
        assertEquals("CV_R1", rule("cv.doc", DOCX));
        assertEquals("CV_R3", rule("cv.pdf", new byte[0]));
        byte[] big = new byte[(int) CvFile.MAX_SIZE_BYTES + 1];
        System.arraycopy(PDF, 0, big, 0, PDF.length);
        assertEquals("CV_R2", rule("cv.pdf", big));
    }
}
