package com.woopaca.likeknu.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiLoggingFilter extends OncePerRequestFilter {

    private static final Logger apiLogger = LoggerFactory.getLogger("api.request");

    private final ObjectMapper objectMapper;

    public ApiLoggingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api") && !request.getRequestURI().startsWith("/univ-club")) {
            filterChain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            MDC.put("device_id", requestWrapper.getHeader("Device-Id"));
            MDC.put("trace_id", UUID.randomUUID().toString());
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            Map<String, Object> logData = new HashMap<>();
            logData.put("timestamp", LocalDateTime.now().toString());
            logData.put("method", request.getMethod());
            logData.put("uri", request.getRequestURI());
            String queryString = request.getQueryString();
            if (StringUtils.hasText(queryString)) {
                logData.put("queryString", URLDecoder.decode(queryString, StandardCharsets.UTF_8));
            }
            logData.put("duration", duration);
            logData.put("status", responseWrapper.getStatus());
            logData.put("userAgent", request.getHeader("User-Agent"));
            logData.put("deviceId", request.getHeader("Device-Id"));

            String requestBody = new String(requestWrapper.getContentAsByteArray());
            if (StringUtils.hasText(requestBody)) {
                logData.put("requestBody", requestBody);
            }

            apiLogger.info(objectMapper.writeValueAsString(logData));
            responseWrapper.copyBodyToResponse();
            MDC.clear();
        }
    }
}
