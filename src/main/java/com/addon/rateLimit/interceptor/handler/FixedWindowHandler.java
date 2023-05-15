package com.addon.rateLimit.interceptor.handler;


import com.addon.rateLimit.cache.APIHitsCache;
import com.addon.rateLimit.config.RateLimitProp;
import com.addon.rateLimit.config.RateLimitProp.Client;
import com.addon.rateLimit.enums.Algorithm;
import com.addon.rateLimit.exception.ManyRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FixedWindowHandler implements AlgorithmHandler {
    private final RateLimitProp rateLimitProp;
    private final APIHitsCache apiHitsCache;


    private static final long per = 60L;
    private static final int limit = 10;

    public void handle(Client client, String api, String user) {

        // check key already exist
        String key = (user == null) ? api : user;
        Object countObj = apiHitsCache.getApiHitCount(key);

        if (null == countObj) {
            long windowTime = getWindowTime(client);
            apiHitsCache.addApiHitCount(key, windowTime);
            return;
        }

        int limit = getFixedWindowLimit(client);
        int count = (Integer) countObj;
        if (count >= limit) {
            throw new ManyRequestException("Exceed rate limit for this API call");
        }
        apiHitsCache.incrementApiHitCount(key);
    }

    @Override
    public void handleRollback(Client client, String api, String user) {
        String key = (user == null) ? api : user;
        apiHitsCache.decrementApiHitCount(key);
    }

    @Override
    public Algorithm getAlgorithm() {
        return Algorithm.Fixed_Window;
    }


    private int getFixedWindowLimit(Client client) {

        int limitValue = (null != rateLimitProp.getDefaults()) ?
                (null != rateLimitProp.getDefaults().getFixedWindow() ?
                        rateLimitProp.getDefaults().getFixedWindow().getLimit() : limit)
                : limit;

        if (null != client && null != client.getFixedWindow()) {
            return (client.getFixedWindow().getLimit() > 0L) ? client.getFixedWindow().getLimit() : limitValue;
        }
        return limitValue;
    }

    private long getWindowTime(Client client) {
        long windowTime = (null != rateLimitProp.getDefaults()) ?
                (null != rateLimitProp.getDefaults().getFixedWindow() ?
                        rateLimitProp.getDefaults().getFixedWindow().getPer() : per)
                : per;
        if (null != client && null != client.getFixedWindow()) {
            return (client.getFixedWindow().getPer() > 0L) ? client.getFixedWindow().getPer() : windowTime;
        }
        return windowTime;
    }


}
