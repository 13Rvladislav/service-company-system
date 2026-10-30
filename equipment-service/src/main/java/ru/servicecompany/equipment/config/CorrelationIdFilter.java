package ru.servicecompany.equipment.config;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(CorrelationIdFilter.class);

    private static final String HEADER_NAME = "X-Correlation-Id";
    private static final String MDC_KEY = "correlationId";

    private static final Pattern CORRELATION_ID_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._-]{1,128}$");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String correlationId = resolveCorrelationId(request);

        long startTime = System.nanoTime();

        MDC.put(MDC_KEY, correlationId);
        response.setHeader(HEADER_NAME, correlationId);

        try {
            filterChain.doFilter(request, response);

        } catch (IOException | ServletException | RuntimeException exception) {

            log.error(
                    "HTTP request failed: method={}, uri={}, exception={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    exception.getClass().getSimpleName(),
                    exception
            );

            throw exception;

        } finally {

            long durationMs =
                    (System.nanoTime() - startTime) / 1_000_000;

            log.info(
                    "HTTP request completed: method={}, uri={}, status={}, durationMs={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs
            );

            MDC.remove(MDC_KEY);
        }
    }

    private String resolveCorrelationId(HttpServletRequest request) {

        String correlationId = request.getHeader(HEADER_NAME);

        if (correlationId != null
                && CORRELATION_ID_PATTERN.matcher(correlationId).matches()) {
            return correlationId;
        }

        return UUID.randomUUID().toString();
    }
}

