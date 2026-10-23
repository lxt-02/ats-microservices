package com.ats.candidate.domain;

import com.ats.candidate.domain.service.CvDataExtractor;
import com.ats.candidate.domain.valueobject.ParsedCvData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CvDataExtractorTest {

    private static final Set<String> SKILLS = Set.of("Java", "JavaScript", "Spring Boot", "Go", "SQL", "C#", ".NET",
            "Node.js", "Docker", "Project Management");
    private final CvDataExtractor extractor = new CvDataExtractor();

    @Test
    void englishCv() {
        String text = """
                CURRICULUM VITAE
                JOHN MICHAEL SMITH
                john.smith@Example.com | +84 912 345 678
                WORK EXPERIENCE
                Backend Developer, ABC (2018 - 2022)
                Java and Spring Boot microservices, SQL tuning, Docker
                EDUCATION:
                BSc Computer Science
                SKILLS
                C#, .NET, Node.js
                I like to go hiking.
                """;
        CvDataExtractor.Result result = extractor.extract(text, SKILLS);
        ParsedCvData data = result.data();

        assertEquals("John Michael Smith", data.fullName());
        assertEquals("john.smith@example.com", data.email());
        assertEquals("+84 912 345 678", data.phone());
        assertEquals(Set.of("Java", "Spring Boot", "SQL", "Docker", "C#", ".NET", "Node.js"), Set.copyOf(data.skills()));
        assertEquals(List.of("Backend Developer, ABC (2018 - 2022)",
                "Java and Spring Boot microservices, SQL tuning, Docker"), data.experience());
        assertEquals(List.of("BSc Computer Science"), data.education());
        assertTrue(result.missingFields().isEmpty());
    }

    @Test
    void vietnameseCvWithLabels() {
        String text = """
                Họ và tên: Nguyễn Thị Bích Ngọc
                Email: ngoc@example.vn
                Điện thoại: 0987.654.321
                KINH NGHIỆM LÀM VIỆC
                Lập trình viên Java tại Công ty X
                HỌC VẤN
                Đại học Bách khoa
                """;
        ParsedCvData data = extractor.extract(text, SKILLS).data();
        assertEquals("Nguyễn Thị Bích Ngọc", data.fullName());
        assertEquals("0987.654.321", data.phone());
        assertEquals(List.of("Lập trình viên Java tại Công ty X"), data.experience());
        assertEquals(List.of("Đại học Bách khoa"), data.education());
        assertEquals(List.of("Java"), data.skills());
    }

    @Test
    void reportsMissingFieldsAndIgnoresYearRangesAsPhones() {
        CvDataExtractor.Result result = extractor.extract("Worked 2018 - 2022 on things\nNo contact", SKILLS);
        assertNull(result.data().phone());
        assertNull(result.data().email());
        assertFalse(result.data().skills().contains("Go"));
        assertEquals(List.of("fullName", "email", "phone", "skills"), result.missingFields());
    }
}
