package com.ats.shared.web;

import com.ats.shared.exception.AuthenticationFailedException;
import com.ats.shared.security.CurrentUser;
import com.ats.shared.security.RequiresRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/** Enforces {@link RequiresRole} before the controller method runs. */
public class RequiresRoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }
        RequiresRole rule = AnnotatedElementUtils.findMergedAnnotation(method.getMethod(), RequiresRole.class);
        if (rule == null) {
            rule = AnnotatedElementUtils.findMergedAnnotation(method.getBeanType(), RequiresRole.class);
        }
        if (rule == null) {
            return true;
        }
        if (!(request.getAttribute(CurrentUser.REQUEST_ATTRIBUTE) instanceof CurrentUser currentUser)) {
            throw new AuthenticationFailedException("Authentication is required");
        }
        if (Arrays.stream(rule.value()).noneMatch(currentUser::hasRole)) {
            throw new AccessDeniedException("You do not have permission to perform this action");
        }
        return true;
    }
}
