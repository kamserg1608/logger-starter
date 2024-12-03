package org.example.t1.http.logger.loggerstarter.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.example.t1.http.logger.loggerstarter.model.LogLevel;
import org.example.t1.http.logger.loggerstarter.properties.HttpRequestLoggerProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Aspect
@Slf4j
@RequiredArgsConstructor
@Component
public class HttpRequestResponseLoggingAspect {

    private final HttpRequestLoggerProperties properties;
    private final ObjectMapper objectMapper;

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restController() {
    }

    @Around("restController()")
    public Object logExecutionTime(ProceedingJoinPoint pjp) {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);

        long startTime = System.currentTimeMillis();
        Object result = null;
        log.info("Advice_Around_Starter -- Start execution method: {}", pjp.getSignature().toShortString());
        try {
            result = pjp.proceed();
        } catch (Throwable e) {
            log.error("Advice_Around_Starter method: {}", pjp.getSignature().toShortString());
        }
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        log.info("Advice_Around_Starter -- Stop execution method: {}", pjp.getSignature() + " executed in " + executionTime + " ms");

        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        if (result instanceof ResponseEntity) {
            try {
                wrappedResponse.copyBodyToResponse();
            } catch (IOException e) {
                log.error("IOException: ", e);
            }
        }

        logRequestAndResponse(wrappedRequest, wrappedResponse, pjp, endTime - startTime, result);

        return result;
    }

    private void logRequestAndResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, ProceedingJoinPoint joinPoint, long duration, Object result) {
        LogLevel level = properties.getLogLevel();

        if (level == LogLevel.NONE) {
            return;
        }

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("uri", request.getRequestURI());
        requestMap.put("method", request.getMethod());
        requestMap.put("headers", getHeaders(request));
        if (level == LogLevel.DEBUG || level == LogLevel.TRACE) {
            requestMap.put("queryString", request.getQueryString());
            requestMap.put("parameters", request.getParameterMap());
        }
        if (level == LogLevel.TRACE) {
            requestMap.put("body", getRequestBody(request));
        }

        Map<String, Object> responseMap = new HashMap<>();
        if (result instanceof ResponseEntity) {
            responseMap.put("status", ((ResponseEntity<?>) result).getStatusCode().value());
        } else {
            responseMap.put("status", response.getStatus());
        }
        responseMap.put("headers", getHeaders(response));
        if (level == LogLevel.TRACE) {
            responseMap.put("body", getResponseBody(response));
        }

        String message = String.format("Method: %s, Request: %s, Response: %s, Duration: %dms",
                joinPoint.getSignature().toShortString(),
                convertMapToJson(requestMap),
                convertMapToJson(responseMap),
                duration);

        switch (level) {
            case TRACE -> log.trace(message);
            case DEBUG -> log.debug(message);
            case INFO -> log.info(message);
        }
    }

    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Collections.list(request.getHeaderNames()).forEach(headerName -> headers.put(headerName, request.getHeader(headerName)));
        return headers;
    }

    private Map<String, String> getHeaders(HttpServletResponse response) {
        Map<String, String> headers = new HashMap<>();
        response.getHeaderNames().forEach(headerName -> headers.put(headerName, response.getHeader(headerName)));
        return headers;
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            try {
                return new String(content, request.getCharacterEncoding());
            } catch (Exception e) {
                return "Body is empty";
            }
        }
        return "";
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            try {
                return new String(content, response.getCharacterEncoding());
            } catch (Exception e) {
                return "Body is empty";
            }
        }
        return "";
    }

    private String convertMapToJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.warn("Error converting map to JSON", e);
            return "{}";
        }
    }
}
