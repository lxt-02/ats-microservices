package com.ats.shared.web;

import com.ats.shared.exception.AuthenticationFailedException;
import com.ats.shared.security.CurrentUser;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/** Lets controllers declare a {@link CurrentUser} parameter; fails with 401 if the request is anonymous. */
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return CurrentUser.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Object value = webRequest.getAttribute(CurrentUser.REQUEST_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        if (value instanceof CurrentUser currentUser) {
            return currentUser;
        }
        throw new AuthenticationFailedException("Authentication is required");
    }
}
