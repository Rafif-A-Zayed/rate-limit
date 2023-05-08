package com.addon.rateLimit.interceptor;

import com.addon.rateLimit.annotation.RateLimit;
import com.addon.rateLimit.cache.APIHitsCache;
import com.addon.rateLimit.exception.ManyRequestException;
import com.addon.rateLimit.exception.RateLimitException;
import com.addon.rateLimit.user.UserIdentifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;


@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitInterceptor {
    public static final String EXCEED_RATE_LIMIT_FOR_THIS_API_CALL = "Exceed rate limit for this API call";
    public static final String FAILED_TO_GET_USER_INFO = "Failed to get user info";

    private final APIHitsCache apiHitsCache;

    private final Environment env;
    private final UserIdentifier userIdentifier;


    @Before(value = "@annotation(com.gotrah.rateLimit.annotation.RateLimit)")
    public void handleRateLimit(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();


        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        if (rateLimit.perApi() && rateLimit.perUser()) {
            throw new RateLimitException("Invalid combination perApi , perUser");
        }


        if (rateLimit.perApi()) {
            handlePerAPI(method, rateLimit);
        }

        if (rateLimit.perUser()) {
            handlePerUser(rateLimit);
        }


    }

    @AfterThrowing( value = "@annotation(com.gotrah.rateLimit.annotation.RateLimit)", throwing = "error" )
    public void errorInterceptor(JoinPoint joinPoint, Exception error) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        if (rateLimit.perApi() && rateLimit.perUser()) {
            throw new RateLimitException("Invalid combination perApi , perUser");
        }

        String key = null;
        if (rateLimit.perApi()) {
           key = method.getName();
        }

        if (rateLimit.perUser()) {
            Object userId = userIdentifier.getUserIdentifier();
            if (null == userId) {
                throw new RateLimitException(FAILED_TO_GET_USER_INFO, HttpStatus.FORBIDDEN);
            }
            key = userId.toString();
        }
       Object value = apiHitsCache.getApiHitCount(key);
        log.info("value {}", value);
        apiHitsCache.decrementApiHitCount(key);
    }

    private void handlePerAPI(Method method, RateLimit rateLimit) {

        String api = method.getName();
        handleFixedWindow(rateLimit, api, null);
    }

    private void handlePerUser(RateLimit rateLimit) {

        Object userId = userIdentifier.getUserIdentifier();
        if (null == userId) {
            throw new RateLimitException(FAILED_TO_GET_USER_INFO, HttpStatus.FORBIDDEN);
        }
        handleFixedWindow(rateLimit, null, userId.toString());
    }


    private void handleFixedWindow(RateLimit rateLimit, String api, String user) {
        String key = api == null ? user : api;

        Object countObj = apiHitsCache.getApiHitCount(key);

        if (null == countObj) {
            long windowTime = getFixedWindowWindow(rateLimit);
            apiHitsCache.addApiHitCount(key, windowTime);
            return;
        }
        int limit = getFixedWindowLimit(rateLimit);
        int count = (Integer) countObj;

        if (count >= limit) {
            throw new ManyRequestException(EXCEED_RATE_LIMIT_FOR_THIS_API_CALL);
        }
        apiHitsCache.incrementApiHitCount(key);
    }

    private int getFixedWindowLimit(RateLimit rateLimit) {
        int limit = rateLimit.fixedWindow().limit();
        String limitExp = rateLimit.fixedWindow().limitExp();
        if (StringUtils.hasLength(limitExp))
            return env.getProperty(limitExp, Integer.class,limit );

        return limit;
    }

    private long getFixedWindowWindow(RateLimit rateLimit) {
        long windowTime = rateLimit.fixedWindow().per();
        String windowTimeExp = rateLimit.fixedWindow().perExp();
        if (StringUtils.hasLength(windowTimeExp))
            return env.getProperty(windowTimeExp, Long.class,windowTime );

        return windowTime;
    }


}
