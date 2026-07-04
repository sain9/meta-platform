package com.hussain.definition_api.shared.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("@annotation(PerformanceLogger)")
    public Object logPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();

        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            log.info("Performance: {} executed in {} ms", methodName, (endTime - startTime));
            return result;
        } catch (Throwable throwable) {
            long endTime = System.currentTimeMillis();
            log.error("Performance: {} failed after {} ms with error: {}",
                    methodName, (endTime - startTime), throwable.getMessage());
            throw throwable;
        }
    }
}