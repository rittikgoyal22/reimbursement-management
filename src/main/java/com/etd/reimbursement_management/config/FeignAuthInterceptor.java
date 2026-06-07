package com.etd.reimbursement_management.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

// Forwards the incoming request's Bearer token to all outgoing Feign calls.
// Without this, Feign calls to travel-planner and account-management arrive
// without an Authorization header and are rejected with 403.
@Component
public class FeignAuthInterceptor implements RequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(FeignAuthInterceptor.class);

    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                requestTemplate.header("Authorization", authHeader);
            }
        } else {
            logger.warn("FeignAuthInterceptor :: No active request context — Authorization header not forwarded");
        }
    }
}
