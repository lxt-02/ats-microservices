package com.ats.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class AllowedSortKeysValidator implements ConstraintValidator<AllowedSortKeys, String> {

    private Set<String> allowed;

    @Override
    public void initialize(AllowedSortKeys annotation) {
        allowed = Set.of(annotation.value());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String[] parts = value.split(",", -1);
        if (parts.length > 2 || !allowed.contains(parts[0].trim())) {
            return false;
        }
        return parts.length == 1 || "asc".equalsIgnoreCase(parts[1].trim()) || "desc".equalsIgnoreCase(parts[1].trim());
    }
}
