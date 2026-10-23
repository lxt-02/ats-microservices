package com.ats.candidate.infrastructure.parser;

import com.ats.candidate.application.port.out.CvTextExtractor;
import com.ats.candidate.domain.enums.CvFileType;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * PDF text through PDFBox; DOCX text read directly from {@code word/document.xml} (paragraphs, tabs, line
 * breaks), with DTDs disabled and a size cap against zip bombs.
 */
@Component
public class CvTextExtractorAdapter implements CvTextExtractor {

    private static final String WORD_NS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
    private static final long MAX_XML_BYTES = 20L * 1024 * 1024;
    private static final int MAX_PDF_PAGES = 30;

    @Override
    public String extractText(CvFileType type, byte[] content) {
        return switch (type) {
            case PDF -> pdf(content);
            case DOCX -> docx(content);
        };
    }

    private static String pdf(byte[] content) {
        try (PDDocument document = Loader.loadPDF(content)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setEndPage(Math.min(document.getNumberOfPages(), MAX_PDF_PAGES));
            return stripper.getText(document);
        } catch (IOException | RuntimeException ex) {
            throw new UnreadableCvException("PDF could not be read", ex);
        }
    }

    private static String docx(byte[] content) {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(content))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if ("word/document.xml".equals(entry.getName())) {
                    return documentText(readCapped(zip));
                }
            }
            throw new UnreadableCvException("DOCX has no word/document.xml", null);
        } catch (IOException | XMLStreamException ex) {
            throw new UnreadableCvException("DOCX could not be read", ex);
        }
    }

    private static byte[] readCapped(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        long total = 0;
        int read;
        while ((read = in.read(buffer)) != -1) {
            total += read;
            if (total > MAX_XML_BYTES) {
                throw new IOException("DOCX content is too large");
            }
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    private static String documentText(byte[] xml) throws XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(xml));
        StringBuilder text = new StringBuilder();
        boolean inText = false;
        try {
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT && WORD_NS.equals(reader.getNamespaceURI())) {
                    switch (reader.getLocalName()) {
                        case "t" -> inText = true;
                        case "tab" -> text.append('\t');
                        case "br", "cr" -> text.append('\n');
                        default -> {
                        }
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT && WORD_NS.equals(reader.getNamespaceURI())) {
                    switch (reader.getLocalName()) {
                        case "t" -> inText = false;
                        case "p" -> text.append('\n');
                        case "tc" -> text.append('\t');
                        default -> {
                        }
                    }
                } else if ((event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA) && inText) {
                    text.append(reader.getText());
                }
            }
        } finally {
            reader.close();
        }
        return text.toString();
    }
}
