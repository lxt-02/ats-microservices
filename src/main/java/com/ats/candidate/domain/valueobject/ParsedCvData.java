package com.ats.candidate.domain.valueobject;

import java.util.List;

/** Structured data extracted from a CV (Full Name, Email, Phone, Skills, Education, Work Experience). */
public record ParsedCvData(
        String fullName,
        String email,
        String phone,
        List<String> skills,
        List<String> education,
        List<String> experience
) {

    public ParsedCvData {
        skills = skills == null ? List.of() : List.copyOf(skills);
        education = education == null ? List.of() : List.copyOf(education);
        experience = experience == null ? List.of() : List.copyOf(experience);
    }

    public static ParsedCvData empty() {
        return new ParsedCvData(null, null, null, List.of(), List.of(), List.of());
    }
}
