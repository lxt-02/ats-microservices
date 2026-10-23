package com.ats.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Validates a "key[,asc|desc]" sort parameter against a list of allowed keys. Null or blank is valid. */
@Documented
@Constraint(validatedBy = AllowedSortKeysValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowedSortKeys {

    String[] value();

    String message() default "Sort is not supported";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
