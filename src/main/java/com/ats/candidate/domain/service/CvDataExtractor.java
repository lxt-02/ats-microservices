package com.ats.candidate.domain.service;

import com.ats.candidate.domain.valueobject.ParsedCvData;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rule-based extraction of structured data from the plain text of a CV (English or Vietnamese headings).
 * Pure Java, no I/O: text extraction from PDF/DOCX happens in the infrastructure layer.
 */
public class CvDataExtractor {

    /** Main fields; a CV missing any of them is only partially parsed. */
    public static final List<String> MAIN_FIELDS = List.of("fullName", "email", "phone", "skills");

    private static final int MAX_SECTION_LINES = 30;
    private static final int MAX_LINE_LENGTH = 300;

    private static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern PHONE = Pattern.compile("(?<![\\d+])(\\+?\\d[\\d .()-]{7,18}\\d)(?!\\d)");
    private static final Pattern NAME_LABEL = Pattern.compile(
            "^(?:full\\s*name|name|ho\\s*(?:va\\s*)?ten)\\s*[:\\-]\\s*(.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern NAME_CHARS = Pattern.compile("^[\\p{L}][\\p{L}'.\\- ]{1,59}$");

    private enum Section { EDUCATION, EXPERIENCE, OTHER }

    /** Headings, compared without accents and in upper case. */
    private static final Map<String, Section> HEADINGS = new LinkedHashMap<>();

    static {
        for (String heading : List.of("EDUCATION", "ACADEMIC BACKGROUND", "ACADEMIC", "QUALIFICATIONS",
                "HOC VAN", "TRINH DO HOC VAN", "QUA TRINH HOC TAP")) {
            HEADINGS.put(heading, Section.EDUCATION);
        }
        for (String heading : List.of("EXPERIENCE", "WORK EXPERIENCE", "PROFESSIONAL EXPERIENCE", "EMPLOYMENT",
                "EMPLOYMENT HISTORY", "WORK HISTORY", "KINH NGHIEM", "KINH NGHIEM LAM VIEC",
                "QUA TRINH LAM VIEC")) {
            HEADINGS.put(heading, Section.EXPERIENCE);
        }
        for (String heading : List.of("SKILLS", "TECHNICAL SKILLS", "KY NANG", "PROJECTS", "DU AN",
                "CERTIFICATIONS", "CERTIFICATES", "CHUNG CHI", "OBJECTIVE", "CAREER OBJECTIVE", "MUC TIEU",
                "MUC TIEU NGHE NGHIEP", "SUMMARY", "PROFILE", "ABOUT ME", "GIOI THIEU", "LANGUAGES", "NGOAI NGU",
                "REFERENCES", "NGUOI THAM CHIEU", "HOBBIES", "INTERESTS", "SO THICH", "ACTIVITIES", "HOAT DONG",
                "AWARDS", "GIAI THUONG", "CONTACT", "LIEN HE", "PERSONAL INFORMATION", "THONG TIN CA NHAN")) {
            HEADINGS.put(heading, Section.OTHER);
        }
    }

    private static final Set<String> NOT_A_NAME = Set.of("CURRICULUM VITAE", "RESUME", "CV", "SO YEU LY LICH",
            "PROFILE", "HO SO");

    /**
     * @param text   plain text of the CV
     * @param skills skill names to look for (from the skill master data)
     */
    public Result extract(String text, Set<String> skills) {
        List<String> lines = text.lines()
                .map(line -> line.replace(' ', ' ').strip())
                .filter(line -> !line.isEmpty())
                .toList();

        String email = firstMatch(EMAIL, text);
        String phone = findPhone(text);
        String fullName = findName(lines);
        List<String> foundSkills = findSkills(text, skills);
        Map<Section, List<String>> sections = sections(lines);

        ParsedCvData data = new ParsedCvData(fullName, email == null ? null : email.toLowerCase(Locale.ROOT), phone,
                foundSkills, sections.getOrDefault(Section.EDUCATION, List.of()),
                sections.getOrDefault(Section.EXPERIENCE, List.of()));

        List<String> missing = new ArrayList<>();
        if (fullName == null) {
            missing.add("fullName");
        }
        if (email == null) {
            missing.add("email");
        }
        if (phone == null) {
            missing.add("phone");
        }
        if (foundSkills.isEmpty()) {
            missing.add("skills");
        }
        return new Result(data, missing);
    }

    public record Result(ParsedCvData data, List<String> missingFields) {
    }

    private static String firstMatch(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    private static String findPhone(String text) {
        Matcher matcher = PHONE.matcher(text);
        while (matcher.find()) {
            String candidate = matcher.group(1).trim();
            String digits = candidate.replaceAll("\\D", "");
            // 9-15 digits, and not a date range or year span like "2018 - 2022"
            if (digits.length() >= 9 && digits.length() <= 15 && !candidate.matches(".*\\b(19|20)\\d{2}\\s*-\\s*(19|20)\\d{2}.*")) {
                return candidate.replaceAll("\\s{2,}", " ");
            }
        }
        return null;
    }

    private static String findName(List<String> lines) {
        for (String line : lines.subList(0, Math.min(lines.size(), 40))) {
            Matcher label = NAME_LABEL.matcher(stripAccents(line));
            if (label.matches()) {
                String value = line.substring(line.length() - label.group(1).length()).trim();
                if (looksLikeName(value)) {
                    return value;
                }
            }
        }
        for (String line : lines.subList(0, Math.min(lines.size(), 8))) {
            if (looksLikeName(line) && !NOT_A_NAME.contains(normalizeHeading(line))
                    && !HEADINGS.containsKey(normalizeHeading(line))) {
                return toDisplayCase(line);
            }
        }
        return null;
    }

    private static boolean looksLikeName(String line) {
        if (!NAME_CHARS.matcher(line).matches()) {
            return false;
        }
        String[] words = line.trim().split("\\s+");
        if (words.length < 2 || words.length > 6) {
            return false;
        }
        for (String word : words) {
            if (!Character.isUpperCase(word.codePointAt(0))) {
                return false;
            }
        }
        return true;
    }

    /** "NGUYEN VAN A" -> "Nguyen Van A"; mixed-case names are kept. */
    private static String toDisplayCase(String line) {
        if (!line.equals(line.toUpperCase(Locale.ROOT))) {
            return line.trim();
        }
        StringBuilder out = new StringBuilder();
        for (String word : line.trim().split("\\s+")) {
            if (!out.isEmpty()) {
                out.append(' ');
            }
            out.append(word.substring(0, 1)).append(word.substring(1).toLowerCase(Locale.ROOT));
        }
        return out.toString();
    }

    /**
     * Skill names are matched as whole words. Short names (up to 3 characters, e.g. "Go", "SQL", "C#") must
     * match case-sensitively to avoid hits on ordinary words.
     */
    private static List<String> findSkills(String text, Set<String> skills) {
        Set<String> found = new LinkedHashSet<>();
        for (String skill : skills) {
            if (skill == null || skill.isBlank()) {
                continue;
            }
            int flags = skill.length() <= 3 ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
            Pattern pattern = Pattern.compile(
                    "(?<![\\p{L}\\p{N}.#+])" + Pattern.quote(skill.trim()) + "(?![\\p{L}\\p{N}#+]|\\.[\\p{L}\\p{N}])",
                    flags);
            if (pattern.matcher(text).find()) {
                found.add(skill.trim());
            }
        }
        return List.copyOf(found);
    }

    private static Map<Section, List<String>> sections(List<String> lines) {
        Map<Section, List<String>> sections = new LinkedHashMap<>();
        Section current = null;
        for (String line : lines) {
            Section heading = HEADINGS.get(normalizeHeading(line));
            if (heading != null) {
                current = heading;
                continue;
            }
            if (current == Section.EDUCATION || current == Section.EXPERIENCE) {
                List<String> content = sections.computeIfAbsent(current, key -> new ArrayList<>());
                if (content.size() < MAX_SECTION_LINES) {
                    content.add(line.length() > MAX_LINE_LENGTH ? line.substring(0, MAX_LINE_LENGTH) : line);
                }
            }
        }
        return sections;
    }

    private static String normalizeHeading(String line) {
        String cleaned = stripAccents(line).replaceAll("[:\\-–_|•*#]+$", "").replaceAll("^[\\-–_|•*#\\s\\d.]+", "")
                .trim().replaceAll("\\s+", " ");
        return cleaned.toUpperCase(Locale.ROOT);
    }

    private static String stripAccents(String value) {
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return decomposed.replace('đ', 'd').replace('Đ', 'D');
    }
}
