package com.ats.shared.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Coarse-grained (screen level) authorization for a controller class or method: the caller must hold at least
 * one of the roles, otherwise 403. A method annotation replaces the class annotation. Data-level rules
 * (e.g. "only the recruiter of this job") stay in the use cases.
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {

    String[] value();
}
