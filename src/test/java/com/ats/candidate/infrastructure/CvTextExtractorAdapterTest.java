package com.ats.candidate.infrastructure;

import com.ats.candidate.application.port.out.CvTextExtractor;
import com.ats.candidate.domain.enums.CvFileType;
import com.ats.candidate.infrastructure.parser.CvTextExtractorAdapter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CvTextExtractorAdapterTest {

    private final CvTextExtractorAdapter extractor = new CvTextExtractorAdapter();

    static byte[] resource(String name) throws IOException {
        try (InputStream in = CvTextExtractorAdapterTest.class.getResourceAsStream("/cv/" + name)) {
            return in.readAllBytes();
        }
    }

    @Test
    void readsPdfText() throws IOException {
        String text = extractor.extractText(CvFileType.PDF, resource("sample-cv.pdf"));
        assertTrue(text.contains("TRAN THI BICH NGOC"), text);
        assertTrue(text.contains("ngoc.tran@example.com"), text);
    }

    @Test
    void readsDocxTextWithParagraphs() throws IOException {
        String text = extractor.extractText(CvFileType.DOCX, resource("sample-cv.docx"));
        assertTrue(text.startsWith("NGUYỄN VĂN AN\n"), text);
        assertTrue(text.contains("KINH NGHIỆM LÀM VIỆC\n"), text);
    }

    @Test
    void corruptedFilesAreReportedAsUnreadable() {
        assertThrows(CvTextExtractor.UnreadableCvException.class,
                () -> extractor.extractText(CvFileType.PDF, "%PDF-garbage".getBytes()));
        assertThrows(CvTextExtractor.UnreadableCvException.class,
                () -> extractor.extractText(CvFileType.DOCX, new byte[]{'P', 'K', 3, 4, 1, 2}));
    }
}
